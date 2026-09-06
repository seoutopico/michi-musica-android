package com.ainalluna.michimusica

import com.ainalluna.michimusica.podcasts.*
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class PodcastStoreTest {
    private fun fixture() = PodcastState(listOf(PodcastShow("https://example.org/feed", "Café 🐈", "Aina", "", listOf(
        PodcastEpisode("one", "Uno", "Descripción", "https://example.org/a.mp3", "audio/mpeg", 12, 300, isNew = true)
    ), 77, seen = setOf("one", "old"))), listOf(
        PodcastDownload("key", "https://example.org/feed", "one", "Uno", "Café", "https://example.org/a.mp3", "audio/mpeg", "content://folder", "content://file", "done")
    ), false, true)
    private fun bytes(state: PodcastState) = ByteArrayOutputStream().also { PodcastStore.write(state, it) }.toByteArray()
    @Test fun preservesFollowingNewsReceiptsAndPreferences() {
        val original = fixture()
        assertEquals(original, PodcastStore.read(ByteArrayInputStream(bytes(original))))
    }
    @Test fun emptyIsAValidState() { assertEquals(PodcastState(), PodcastStore.read(ByteArrayInputStream(bytes(PodcastState())))) }
    @Test fun truncatedSnapshotIsNotTreatedAsEmpty() {
        val content = bytes(fixture())
        assertTrue(runCatching { PodcastStore.read(ByteArrayInputStream(content.copyOf(content.size - 10))) }.isFailure)
    }
    @Test fun wrongVersionAndTrailingBytesAreRejected() {
        val wrong = bytes(fixture()); wrong[3] = 99
        assertTrue(runCatching { PodcastStore.read(ByteArrayInputStream(wrong)) }.isFailure)
        assertTrue(runCatching { PodcastStore.read(ByteArrayInputStream(bytes(fixture()) + byteArrayOf(1))) }.isFailure)
    }
}
