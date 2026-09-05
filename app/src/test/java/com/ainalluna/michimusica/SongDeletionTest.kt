package com.ainalluna.michimusica

import com.ainalluna.michimusica.library.removedSongIndices
import org.junit.Assert.*
import org.junit.Test

class SongDeletionTest {
    @Test fun deletingRepeatedReferencesPreservesOtherSongsAndTheirOrder() {
        val queue = mutableListOf("a", "deleted", "b", "deleted", "c")
        removedSongIndices(queue, "deleted").forEach { queue.removeAt(it) }
        assertEquals(listOf("a", "b", "c"), queue)
    }

    @Test fun deletingLastSongLeavesNoPlayableReference() {
        val queue = mutableListOf("deleted")
        removedSongIndices(queue, "deleted").forEach { queue.removeAt(it) }
        assertTrue(queue.isEmpty())
    }

    @Test fun deletingSongOutsideActiveQueueDoesNotAffectPlaybackOrder() {
        val queue = listOf("c", "a", "b")
        assertTrue(removedSongIndices(queue, "elsewhere").isEmpty())
        assertTrue(removedSongIndices(emptyList(), "elsewhere").isEmpty())
    }
}
