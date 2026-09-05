package com.ainalluna.michimusica

import com.ainalluna.michimusica.youtube.YouTubeInput
import com.ainalluna.michimusica.youtube.classifyYouTubeInput
import org.junit.Assert.assertEquals
import org.junit.Test

class YouTubeInputTest {
    @Test
    fun keepsTextAsAnInternalSearch() {
        assertEquals(YouTubeInput.Search("música tranquila"), classifyYouTubeInput("música tranquila"))
    }

    @Test
    fun keepsDirectYouTubeLinks() {
        val link = "https://youtu.be/abcdefghijk"
        assertEquals(YouTubeInput.Video("abcdefghijk"), classifyYouTubeInput(link))
    }

    @Test
    fun acceptsShortsAndMusicLinks() {
        assertEquals(
            YouTubeInput.Video("abcdefghijk"),
            classifyYouTubeInput("https://www.youtube.com/shorts/abcdefghijk"),
        )
        assertEquals(
            YouTubeInput.Video("123456789_-"),
            classifyYouTubeInput("https://music.youtube.com/watch?v=123456789_-&list=RDAMVM"),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsExternalLinks() {
        classifyYouTubeInput("https://example.com/video")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsYouTubeChannelsAndPlaylists() {
        classifyYouTubeInput("https://www.youtube.com/playlist?list=PL123")
    }
}
