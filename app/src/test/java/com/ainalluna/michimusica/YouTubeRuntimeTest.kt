package com.ainalluna.michimusica

import com.ainalluna.michimusica.youtube.shouldUpdateYoutubeDl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubeRuntimeTest {
    private val day = 24L * 60L * 60L * 1_000L

    @Test
    fun `updates when no successful check exists`() {
        assertTrue(shouldUpdateYoutubeDl(0L, 1_000L))
    }

    @Test
    fun `does not update twice in the same day`() {
        assertFalse(shouldUpdateYoutubeDl(1_000L, 1_000L + day - 1L))
    }

    @Test
    fun `updates after one day`() {
        assertTrue(shouldUpdateYoutubeDl(1_000L, 1_000L + day))
    }

    @Test
    fun `updates if the device clock moved backwards`() {
        assertTrue(shouldUpdateYoutubeDl(2_000L, 1_000L))
    }
}
