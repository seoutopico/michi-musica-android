package com.ainalluna.michimusica.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.ainalluna.michimusica.library.Song
import com.ainalluna.michimusica.youtube.*

private val sampleResults = listOf(
    YouTubeResult("sample-one", "Luz de septiembre — sesión acústica", "Brisa · Canal oficial", 248, ""),
    YouTubeResult("sample-two", "Luz de septiembre (en directo)", "Noches de música", 301, ""),
    YouTubeResult("sample-three", "Una canción con un título largo para comprobar la lectura", "Brisa", 224, ""),
)

@Composable
internal fun SearchPreview(state: SearchUiState = SearchUiState(), skin: MichiSkin = MichiSkin.MIDNIGHT) {
    var ui by remember { mutableStateOf(state) }
    MichiTheme(skin) {
        Scaffold(bottomBar = {
            Column {
                HomeMiniPlayer(Song("saved", "Luz.mp3", "Luz de septiembre", "Brisa", durationMs = 248_000, uri = "content://preview/luz".toUri()),
                    false, 42_000, 248_000, true, true, 0, {}, {}, {})
                HomeNavigation(1) {}
            }
        }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                SearchContent(ui, true, { ui = ui.copy(query = it) },
                    { ui = ui.copy(submitted = ui.query, results = sampleResults) },
                    { ui = ui.copy(previewId = it.id, previewPlaying = !ui.previewPlaying) },
                    { ui = ui.copy(savedIds = ui.savedIds + it.id) }, {})
            }
        }
    }
}

@Preview(name = "01 · Entrada", group = "Buscar real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun SearchEntryPreview() = SearchPreview()
@Preview(name = "02 · Resultados", group = "Buscar real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun SearchResultsPreview() = SearchPreview(SearchUiState(query = "Luz de septiembre", submitted = "Luz de septiembre", results = sampleResults))
@Preview(name = "03 · Descarga", group = "Buscar real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun SearchDownloadPreview() = SearchPreview(SearchUiState(query = "Brisa", submitted = "Brisa", results = sampleResults, downloadingId = "sample-one", progress = 48))
@Preview(name = "04 · Error", group = "Buscar real", widthDp = 360, heightDp = 740, fontScale = 1.3f, showSystemUi = true)
@Composable private fun SearchErrorPreview() = SearchPreview(SearchUiState(query = "Brisa", submitted = "Brisa", searchError = "Comprueba la conexión y vuelve a intentarlo."))
@Preview(name = "05 · Rosa", group = "Buscar real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun SearchRosePreview() = SearchPreview(skin = MichiSkin.ROSE)
@Preview(name = "06 · Resultados · Texto grande", group = "Buscar real", widthDp = 360, heightDp = 740, fontScale = 1.3f, showSystemUi = true)
@Composable private fun SearchLargePreview() = SearchPreview(SearchUiState(query = "Brisa", submitted = "Brisa", results = sampleResults))
