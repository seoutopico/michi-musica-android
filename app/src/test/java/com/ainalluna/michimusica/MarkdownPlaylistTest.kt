package com.ainalluna.michimusica

import com.ainalluna.michimusica.library.MarkdownPlaylist
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownPlaylistTest {
    private val filenames = listOf("Amanecer.mp3", "Gata lunar.flac", "Café.ogg")

    @Test
    fun resolvesFriendlyEntriesInOrderAndReportsProblems() {
        val result = MarkdownPlaylist.resolveFilenames(
            "# Noche tranquila\n- `Amanecer.mp3`\n- [Gata](Gata%20lunar.flac)\n- Amanecer\n- Falta.mp3",
            filenames,
        )
        assertEquals("Noche tranquila", result.title)
        assertEquals(listOf("Amanecer.mp3", "Gata lunar.flac"), result.filenames)
        assertEquals(listOf("Amanecer"), result.duplicates)
        assertEquals(listOf("Falta.mp3"), result.missing)
    }

    @Test
    fun acceptsUniqueStemAndRelativePath() {
        val result = MarkdownPlaylist.resolveFilenames("- album\\Amanecer.mp3\n- Café", filenames)
        assertEquals(listOf("Amanecer.mp3", "Café.ogg"), result.filenames)
    }
}
