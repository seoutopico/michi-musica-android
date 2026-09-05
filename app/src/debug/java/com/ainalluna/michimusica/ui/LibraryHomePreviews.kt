package com.ainalluna.michimusica.ui

import androidx.core.net.toUri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ainalluna.michimusica.library.Song

private val homeSamples = listOf(
    Song("luz", "Luz de septiembre.mp3", "Luz de septiembre", "Brisa", durationMs = 248_000, uri = "content://preview/luz".toUri()),
    Song("lluvia", "Bajo la lluvia.mp3", "Bajo la lluvia", "Elena y los días", durationMs = 256_000, uri = "content://preview/lluvia".toUri()),
    Song("sol", "Al otro lado del sol.mp3", "Al otro lado del sol", "Clara del Mar", durationMs = 222_000, uri = "content://preview/sol".toUri()),
    Song("lugar", "Un lugar tranquilo.mp3", "Un lugar tranquilo", "Brisa", durationMs = 231_000, uri = "content://preview/lugar".toUri()),
    Song("volver", "Volver despacio.mp3", "Volver despacio", "Norte", durationMs = 262_000, uri = "content://preview/volver".toUri()),
)

/** Uses the production components. Artwork is the honest no-cover state, not generated sample covers. */
@Composable
fun LibraryHomePreviewContent(skin: MichiSkin = MichiSkin.MIDNIGHT, hasSelection: Boolean = true, initialNotice: LibraryNotice? = null) {
    var selected by remember { mutableStateOf(if (hasSelection) homeSamples.first() else null) }
    var playing by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf(initialNotice) }
    var position by remember { mutableLongStateOf(84_000L) }
    var deleteTarget by remember { mutableStateOf<Song?>(null) }
    MichiTheme(skin) {
        Scaffold(bottomBar = {
            Column(Modifier.fillMaxWidth()) {
                selected?.let { song ->
                    HomeMiniPlayer(song, playing, position, song.durationMs, true, true, 0,
                        { playing = !playing },
                        { selected = homeSamples[(homeSamples.indexOf(song) + 1) % homeSamples.size]; position = 0 }, {})
                }
                HomeNavigation(0) {}
            }
        }) { padding ->
            Box(Modifier.padding(padding)) {
                LibraryHome(homeSamples, selected?.id, playing, true, false, notice, null, 0,
                    onSelect = { selected = it; playing = true; position = 0 },
                    onShuffle = { selected = homeSamples.last(); playing = true; position = 0 },
                    onSettings = {}, onChooseFolder = {}, onAllMusic = {}, onDismissNotice = { notice = null }, onDelete = { deleteTarget = it })
            }
        }
        deleteTarget?.let { SongDeletionDialog(it, false, null, { deleteTarget = null }, { deleteTarget = null }) }
    }
}

@Preview(name = "01 · Portada aprobada · Medianoche", group = "Biblioteca real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun MidnightHomePreview() = LibraryHomePreviewContent()
@Preview(name = "02 · Sin escucha previa", group = "Biblioteca real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun NoSelectionHomePreview() = LibraryHomePreviewContent(hasSelection = false)
@Preview(name = "03 · Texto grande · 360 dp", group = "Biblioteca real", widthDp = 360, heightDp = 740, fontScale = 1.3f, showSystemUi = true)
@Composable private fun LargeTextHomePreview() = LibraryHomePreviewContent()
@Preview(name = "04 · Rosa", group = "Biblioteca real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun RoseHomePreview() = LibraryHomePreviewContent(skin = MichiSkin.ROSE)
