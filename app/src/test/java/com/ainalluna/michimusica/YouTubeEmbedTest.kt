package com.ainalluna.michimusica

import com.ainalluna.michimusica.youtube.youtubeEmbedHtml
import com.ainalluna.michimusica.youtube.youtubeEmbedError
import org.junit.Assert.*
import org.junit.Test

class YouTubeEmbedTest {
    @Test fun rejectsIdsThatCouldInjectHtmlOrJavascript() {
        for (id in listOf("", "short", "<script>bad", "1234567890'", "https://youtu.be/M7lc1UVf-VE")) {
            assertThrows(IllegalArgumentException::class.java) {
                youtubeEmbedHtml(id, "com.ainalluna.michimusica", 0.0, false)
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            youtubeEmbedHtml("M7lc1UVf-VE", "example.com';alert(1)", 0.0, false)
        }
    }

    @Test fun restoredVideoHasAnAppOriginAndDoesNotAutoplay() {
        val html = youtubeEmbedHtml("M7lc1UVf-VE", "com.ainalluna.michimusica", 42.9, false)
        assertTrue(html.contains("allowed=false"))
        assertTrue(html.contains("start:42"))
        assertTrue(html.contains("origin:'https://com.ainalluna.michimusica'"))
        assertTrue(html.contains("controls:1,fs:1"))
    }

    @Test fun unavailableOrInvalidPositionsStartAtZero() {
        for (position in listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertTrue(youtubeEmbedHtml("M7lc1UVf-VE", "com.ainalluna.michimusica", position, true).contains("start:0"))
        }
    }

    @Test fun blockedEmbedsOfferYouTubeInsteadOfRetryingExtraction() {
        assertEquals(youtubeEmbedError(101), youtubeEmbedError(150))
        assertTrue(youtubeEmbedError(150).contains("solo se puede reproducir en YouTube"))
        assertTrue(youtubeEmbedError(100).contains("privado"))
        assertTrue(youtubeEmbedError(153).contains("YouTube"))
    }
}
