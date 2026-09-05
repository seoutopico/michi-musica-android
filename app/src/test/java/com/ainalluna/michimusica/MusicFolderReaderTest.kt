package com.ainalluna.michimusica

import com.ainalluna.michimusica.library.MusicFolderReader
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicFolderReaderTest {
    @Test
    fun acceptsSupportedFormatsWithoutCaseSensitivity() {
        assertTrue(MusicFolderReader.isSupportedAudio("Música.FLAC"))
        assertTrue(MusicFolderReader.isSupportedAudio("tema.opus"))
        assertTrue(MusicFolderReader.isSupportedAudio("grabación.MP3"))
    }

    @Test
    fun rejectsFilesThatAreNotAudio() {
        assertFalse(MusicFolderReader.isSupportedAudio("portada.jpg"))
        assertFalse(MusicFolderReader.isSupportedAudio("lista.md"))
        assertFalse(MusicFolderReader.isSupportedAudio("sin-extension"))
    }
}

