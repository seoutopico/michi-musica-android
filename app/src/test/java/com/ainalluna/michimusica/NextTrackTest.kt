package com.ainalluna.michimusica

import com.ainalluna.michimusica.playback.nextTrackIndex
import org.junit.Assert.*
import org.junit.Test

class NextTrackTest {
    @Test fun shuffleFollowsTheRemainingOrderBeforeWrapping() {
        val order = listOf(2, 0, 3, 1)
        val visited = mutableListOf(order.first())
        for (index in 0 until order.lastIndex) {
            visited += nextTrackIndex(order[index], order[index + 1], order.first(), 4, true)!!
        }
        assertEquals(order, visited)
        assertEquals(4, visited.distinct().size)
        assertEquals(2, nextTrackIndex(1, -1, 2, 4, true))
    }
    @Test fun sequentialEndDoesNotWrap() {
        assertNull(nextTrackIndex(3, -1, 0, 4, false))
        assertEquals(3, nextTrackIndex(2, 3, 0, 4, false))
    }
    @Test fun emptySingleAndUnavailableOrdersHaveNoDifferentNextTrack() {
        assertNull(nextTrackIndex(0, -1, 0, 1, true))
        assertNull(nextTrackIndex(-1, -1, -1, 0, true))
        assertNull(nextTrackIndex(2, -1, -1, 4, true))
        assertNull(nextTrackIndex(2, -1, 2, 4, true))
    }
}
