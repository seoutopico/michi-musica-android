package com.ainalluna.michimusica

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistSyncTest {
    @Test
    fun detectsSongsAddedAfterTheInitialFolderScan() {
        assertTrue(playlistNeedsUpdate(listOf("first"), listOf("first", "second")))
    }

    @Test
    fun leavesAnAlreadySynchronizedQueueUntouched() {
        assertFalse(playlistNeedsUpdate(listOf("first", "second"), listOf("first", "second")))
    }

    @Test
    fun detectsAChangedOrderOrFolder() {
        assertTrue(playlistNeedsUpdate(listOf("first", "second"), listOf("second", "first")))
    }
}
