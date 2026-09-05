package com.ainalluna.michimusica

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackUxStateTest {
    @Test
    fun openingLibraryCannotClearAnActiveServiceQueueBeforeScanCompletes() {
        assertFalse(canSynchronizeLibrary(loaded = false, loading = false, sourceMatches = true))
        assertFalse(canSynchronizeLibrary(loaded = false, loading = true, sourceMatches = true))
        assertFalse(canSynchronizeLibrary(loaded = true, loading = true, sourceMatches = true))
        assertFalse(canSynchronizeLibrary(loaded = true, loading = false, sourceMatches = false))
        assertTrue(canSynchronizeLibrary(loaded = true, loading = false, sourceMatches = true))
    }
    @Test
    fun savedSelectionCanResumeAtZeroOrBeforeFiveSeconds() {
        assertEquals(0L, resumePosition(0L, 180_000L))
        assertEquals(2_500L, resumePosition(2_500L, 180_000L))
        assertEquals(84_000L, resumePosition(84_000L, 180_000L))
    }

    @Test
    fun invalidOrFinishedPositionsRestartWithoutSeekingPastTheTrack() {
        assertEquals(0L, resumePosition(-10L, 180_000L))
        assertEquals(0L, resumePosition(180_000L, 180_000L))
        assertEquals(0L, resumePosition(210_000L, 180_000L))
        assertEquals(84_000L, resumePosition(84_000L, 0L))
    }

    @Test
    fun restoreWaitsForCorrectSourceAndExactQueueEvenIfSongExistsInBoth() {
        assertFalse(restoreQueueReady(false, listOf("a", "b"), listOf("a", "b")))
        assertFalse(restoreQueueReady(true, listOf("b", "a"), listOf("a", "b")))
        assertFalse(restoreQueueReady(true, listOf("a", "b"), listOf("a", "b", "c")))
        assertFalse(restoreQueueReady(true, emptyList(), emptyList()))
        assertTrue(restoreQueueReady(true, listOf("b", "a"), listOf("b", "a")))
    }
    @Test
    fun preparedQueueDoesNotCreateVisiblePlayer() {
        assertFalse(hasActiveListeningSession(engaged = false, playing = false, positionMs = 0L))
    }

    @Test
    fun explicitOrRealPlaybackCreatesVisiblePlayer() {
        assertTrue(hasActiveListeningSession(engaged = true, playing = false, positionMs = 0L))
        assertTrue(hasActiveListeningSession(engaged = false, playing = true, positionMs = 0L))
        assertTrue(hasActiveListeningSession(engaged = false, playing = false, positionMs = 42_000L))
    }

}
