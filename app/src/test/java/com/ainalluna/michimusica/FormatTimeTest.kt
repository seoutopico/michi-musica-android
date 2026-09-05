package com.ainalluna.michimusica

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTimeTest {
    @Test fun formatsDurations() {
        assertEquals("0:00", formatTime(0))
        assertEquals("1:05", formatTime(65_000))
        assertEquals("61:01", formatTime(3_661_000))
    }

    @Test fun clampsNegativeDurations() {
        assertEquals("0:00", formatTime(-1))
    }
}

