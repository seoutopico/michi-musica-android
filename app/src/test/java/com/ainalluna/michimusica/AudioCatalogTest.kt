package com.ainalluna.michimusica

import android.content.SharedPreferences
import com.ainalluna.michimusica.library.AudioCatalog
import com.ainalluna.michimusica.library.AudioSection
import com.ainalluna.michimusica.library.episodeResumePosition
import java.lang.reflect.Proxy
import org.junit.Assert.*
import org.junit.Test

class AudioCatalogTest {
    @Test fun unclassifiedAudioRemainsMusicAndChoiceDefaultsToMusic() {
        val catalog = AudioCatalog(memoryPreferences())
        assertFalse(catalog.isPodcast("existing.mp3"))
        assertEquals(AudioSection.MUSIC, catalog.lastDownloadSection)
    }
    @Test fun episodesKeepIndependentPositionsAcrossCatalogInstances() {
        val prefs = memoryPreferences()
        val catalog = AudioCatalog(prefs)
        catalog.classify("episode-a", AudioSection.PODCASTS)
        catalog.classify("episode-b", AudioSection.PODCASTS)
        catalog.savePosition("episode-a", 125_000)
        catalog.savePosition("episode-b", 480_000)
        val reopened = AudioCatalog(prefs)
        assertEquals(125_000L, reopened.position("episode-a"))
        assertEquals(480_000L, reopened.position("episode-b"))
        assertEquals(setOf("episode-a", "episode-b"), reopened.podcastIds())
    }
    @Test fun reclassificationDoesNotChangeAnotherEpisodeOrForgetProgress() {
        val catalog = AudioCatalog(memoryPreferences())
        catalog.classify("a", AudioSection.PODCASTS)
        catalog.classify("b", AudioSection.PODCASTS)
        catalog.savePosition("a", 42_000)
        catalog.classify("a", AudioSection.MUSIC)
        catalog.savePosition("a", 1_000) // Music playback must not replace podcast progress.
        assertEquals(setOf("b"), catalog.podcastIds())
        catalog.classify("a", AudioSection.PODCASTS)
        assertEquals(42_000L, catalog.position("a"))
    }
    @Test fun deletingOneEpisodeRemovesOnlyItsCatalogData() {
        val catalog = AudioCatalog(memoryPreferences())
        for (id in listOf("a", "b")) { catalog.classify(id, AudioSection.PODCASTS); catalog.savePosition(id, 30_000) }
        catalog.forget("a")
        assertFalse(catalog.isPodcast("a"))
        assertEquals(0L, catalog.position("a"))
        assertTrue(catalog.isPodcast("b"))
        assertEquals(30_000L, catalog.position("b"))
    }
    @Test fun downloadChoicePersistsWithoutClassifyingExistingAudio() {
        val prefs = memoryPreferences()
        AudioCatalog(prefs).lastDownloadSection = AudioSection.PODCASTS
        val reopened = AudioCatalog(prefs)
        assertEquals(AudioSection.PODCASTS, reopened.lastDownloadSection)
        assertTrue(reopened.podcastIds().isEmpty())
    }
    @Test fun completedEpisodesRestartButUnfinishedOnesResume() {
        assertEquals(0L, episodeResumePosition(180_000, 180_000))
        assertEquals(0L, episodeResumePosition(190_000, 180_000))
        assertEquals(125_000L, episodeResumePosition(125_000, 180_000))
        assertEquals(125_000L, episodeResumePosition(125_000, 0))
        assertEquals(0L, episodeResumePosition(-1, 180_000))
    }

    /** In-memory Android preference contract; exercises the real catalog across reopenings. */
    private fun memoryPreferences(): SharedPreferences {
        val values = mutableMapOf<String, Any?>()
        return Proxy.newProxyInstance(SharedPreferences::class.java.classLoader, arrayOf(SharedPreferences::class.java)) { _, method, args ->
            when (method.name) {
                "getStringSet", "getLong", "getBoolean" -> values[args!![0]] ?: args[1]
                "edit" -> {
                    val edits = mutableMapOf<String, Any?>()
                    Proxy.newProxyInstance(SharedPreferences.Editor::class.java.classLoader, arrayOf(SharedPreferences.Editor::class.java)) { proxy, edit, params ->
                        when (edit.name) {
                            "putStringSet", "putLong", "putBoolean" -> { edits[params!![0] as String] = params[1]; proxy }
                            "remove" -> { edits[params!![0] as String] = null; proxy }
                            "apply", "commit" -> { edits.forEach { (key, value) -> if (value == null) values.remove(key) else values[key] = value }; true }
                            else -> error(edit.name)
                        }
                    }
                }
                else -> error(method.name)
            }
        } as SharedPreferences
    }
}
