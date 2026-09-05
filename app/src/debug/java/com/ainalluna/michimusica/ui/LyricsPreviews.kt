package com.ainalluna.michimusica.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.ainalluna.michimusica.library.Song
import com.ainalluna.michimusica.lyrics.*

private val lyricSampleSong = Song("lyric-preview", "Luz.mp3", "Luz de septiembre", "Brisa", durationMs = 248_000, uri = "content://preview/luz".toUri())
private const val originalSampleLyrics = "[00:00]La tarde vuelve despacio\n[00:08]y el aire mueve el mantel\n[00:16]dejamos abierta la puerta\n[00:24]por si nos visita el ayer\n[00:32]La calle se llena de pasos\n[00:40]la luz se detiene en la piel\n[00:48]cantamos mirando la lluvia\n[00:56]y empieza septiembre otra vez"
private val lyricMatch = LyricsMatch("Luz de septiembre", "Brisa", "Días tranquilos", 248.0, originalSampleLyrics, null)

@Composable
internal fun LyricsPreviewContent(mode: String = "reading", skin: MichiSkin = MichiSkin.MIDNIGHT) {
    var ui by remember { mutableStateOf(LyricsUiState("Luz de septiembre", "Brisa", loading = false,
        stored = if (mode == "empty" || mode == "search") null else StoredLyrics(originalSampleLyrics, true),
        editing = mode == "search" || mode == "preview", results = if (mode == "search") listOf(lyricMatch) else emptyList(),
        searched = mode == "search", candidate = if (mode == "preview") lyricMatch else null)) }
    var position by remember { mutableLongStateOf(16_000) }
    MichiTheme(skin) {
        Scaffold(bottomBar = { Column {
            HomeMiniPlayer(lyricSampleSong, false, position, 248_000, true, true, 0, {}, {}, {})
            HomeNavigation(0) {}
        } }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                LyricsHome(lyricSampleSong, ui, position, { ui = ui.copy(editing = false, candidate = null) },
                    { ui = ui.copy(editing = true) }, { ui = ui.copy(title = it) }, { ui = ui.copy(artist = it) },
                    { ui = ui.copy(searched = true, results = listOf(lyricMatch)) }, { ui = ui.copy(candidate = it) },
                    { ui = ui.copy(stored = StoredLyrics(originalSampleLyrics, true), editing = false, candidate = null) },
                    { ui = ui.copy(stored = null) }, { position = it }, {})
            }
        }
    }
}

@Preview(name = "01 · Sincronizada", group = "Letra real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun LyricsReadingPreview() = LyricsPreviewContent()
@Preview(name = "02 · Sin letra", group = "Letra real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun LyricsEmptyPreview() = LyricsPreviewContent("empty")
@Preview(name = "03 · Buscar", group = "Letra real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun LyricsSearchPreview() = LyricsPreviewContent("search")
@Preview(name = "04 · Revisar coincidencia", group = "Letra real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun LyricsCandidatePreview() = LyricsPreviewContent("preview")
@Preview(name = "05 · Texto grande", group = "Letra real", widthDp = 360, heightDp = 740, fontScale = 1.3f, showSystemUi = true)
@Composable private fun LyricsLargePreview() = LyricsPreviewContent()
@Preview(name = "06 · Rosa", group = "Letra real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun LyricsRosePreview() = LyricsPreviewContent(skin = MichiSkin.ROSE)
