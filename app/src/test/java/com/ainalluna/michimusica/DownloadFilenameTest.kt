package com.ainalluna.michimusica

import com.ainalluna.michimusica.youtube.availableMp3Name
import org.junit.Assert.*
import org.junit.Test

class DownloadFilenameTest {
    @Test fun preservesExistingNamesAndFindsTheNextAvailableSuffix() {
        val existing = setOf("song.mp3", "song (1).mp3", "song (2).mp3")
        assertEquals("song (3).mp3", availableMp3Name("song.mp3", existing::contains))
    }
    @Test fun removesPathSeparatorsAndPreservesExtensionForLongTitles() {
        val name = availableMp3Name("../" + "x".repeat(250) + ".mp3") { false }
        assertFalse(name.contains('/'))
        assertTrue(name.endsWith(".mp3"))
        assertTrue(name.length <= 164)
    }
}
