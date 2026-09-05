package com.ainalluna.michimusica

import com.ainalluna.michimusica.ui.playerSeekPosition
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerSeekTest {
    @Test fun seekMapsTrackFractionToMilliseconds() {
        assertEquals(90_000L, playerSeekPosition(.5f, 180_000L))
        assertEquals(180_000L, playerSeekPosition(1f, 180_000L))
    }
    @Test fun outOfRangeGesturesStayInsideTrack() {
        assertEquals(0L, playerSeekPosition(-.4f, 180_000L))
        assertEquals(180_000L, playerSeekPosition(1.4f, 180_000L))
    }
    @Test fun unavailableDurationAndInvalidFractionsDoNotProduceInvalidSeek() {
        assertEquals(0L, playerSeekPosition(.5f, -1L))
        assertEquals(0L, playerSeekPosition(.5f, 0L))
        assertEquals(0L, playerSeekPosition(Float.NaN, 180_000L))
        assertEquals(0L, playerSeekPosition(Float.POSITIVE_INFINITY, 180_000L))
    }
}
