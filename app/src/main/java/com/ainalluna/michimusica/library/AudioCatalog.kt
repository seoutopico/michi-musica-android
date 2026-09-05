package com.ainalluna.michimusica.library

import android.content.Context
import androidx.core.content.edit

enum class AudioSection(val label: String) { MUSIC("Música"), PODCASTS("Podcasts") }

internal fun episodeResumePosition(position: Long, duration: Long): Long =
    if (position < 0 || (duration > 0 && position >= duration)) 0 else position

/** A queue removal may select an unfinished episode at zero; preserve explicit seek positions. */
internal fun episodeTransitionPosition(saved: Long, duration: Long, requested: Long): Long? =
    episodeResumePosition(saved, duration).takeIf { requested == 0L && it > 0L }

/** Local catalog only: no tags, filenames or Markdown documents are changed. */
class AudioCatalog internal constructor(val preferences: android.content.SharedPreferences) {
    constructor(context: Context) : this(context.getSharedPreferences("audio_catalog", Context.MODE_PRIVATE))
    private companion object { val classificationLock = Any() }
    fun podcastIds(): Set<String> = preferences.getStringSet("podcasts", emptySet()).orEmpty().toSet()
    fun isPodcast(id: String): Boolean = id in podcastIds()
    fun classify(id: String, section: AudioSection) = synchronized(classificationLock) {
        val ids = podcastIds().toMutableSet()
        if (section == AudioSection.PODCASTS) ids.add(id) else ids.remove(id)
        preferences.edit { putStringSet("podcasts", ids) }
    }
    fun position(id: String): Long = preferences.getLong("position:$id", 0)
    fun savePosition(id: String, position: Long) {
        if (isPodcast(id)) preferences.edit { putLong("position:$id", position.coerceAtLeast(0)) }
    }
    fun forget(id: String) {
        classify(id, AudioSection.MUSIC)
        preferences.edit { remove("position:$id") }
    }
    var lastDownloadSection: AudioSection
        get() = if (preferences.getBoolean("download_podcast", false)) AudioSection.PODCASTS else AudioSection.MUSIC
        set(value) { preferences.edit { putBoolean("download_podcast", value == AudioSection.PODCASTS) } }
}
