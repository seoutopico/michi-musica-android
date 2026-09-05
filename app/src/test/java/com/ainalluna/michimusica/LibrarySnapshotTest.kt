package com.ainalluna.michimusica

import com.ainalluna.michimusica.library.CachedAudio
import com.ainalluna.michimusica.library.LibrarySnapshot
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.*
import org.junit.Test

class LibrarySnapshotTest {
    private val audio = CachedAudio("content://music/1", "Música 猫.mp3", 100L, 500L,
        "Canción", "Artista", "Álbum", 180_000L)

    @Test fun snapshotSurvivesProcessRestartIncludingEmptyLibraryAndUnicode() {
        for (entries in listOf(listOf(audio), emptyList())) {
            val snapshot = LibrarySnapshot("content://music/tree", entries)
            val bytes = ByteArrayOutputStream().also { snapshot.write(it) }.toByteArray()
            assertEquals(snapshot, LibrarySnapshot.read(ByteArrayInputStream(bytes)))
        }
    }

    @Test fun changedOrUnknownFileAttributesRequireReadingMetadataAgain() {
        assertTrue(audio.matches(audio.filename, 100, 500))
        assertFalse(audio.matches("renamed.mp3", 100, 500))
        assertFalse(audio.matches(audio.filename, 101, 500))
        assertFalse(audio.matches(audio.filename, 100, 501))
        assertFalse(audio.copy(modified = 0).matches(audio.filename, 0, 500))
        assertFalse(audio.copy(size = 0).matches(audio.filename, 100, 0))
    }

    @Test fun truncatedCacheIsRejectedInsteadOfReturningPartialCatalog() {
        val bytes = ByteArrayOutputStream().also { LibrarySnapshot("folder", listOf(audio)).write(it) }.toByteArray()
        assertTrue(runCatching { LibrarySnapshot.read(ByteArrayInputStream(bytes.copyOf(bytes.size - 1))) }.isFailure)
    }

    @Test fun incompatibleVersionIsRejected() {
        assertTrue(runCatching { LibrarySnapshot.read(ByteArrayInputStream(byteArrayOf(0, 0, 0, 2))) }.isFailure)
    }
}
