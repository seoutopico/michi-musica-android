package com.ainalluna.michimusica.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.ainalluna.michimusica.library.Song

private val listSampleSong = Song("saved", "Luz.mp3", "Luz de septiembre", "Brisa", durationMs = 248_000, uri = "content://preview/luz".toUri())
private val listSamples = listOf(
    PlaylistCollection("night", "Noches tranquilas", 24),
    PlaylistCollection("journey", "Para perderse por el camino", 18),
    PlaylistCollection("dance", "Electrónica: de menos a más", 17, 2),
)

@Composable
internal fun ListsPreviewContent(empty: Boolean = false, skin: MichiSkin = MichiSkin.MIDNIGHT, loading: Boolean = false) {
    var active by remember { mutableStateOf<String?>(if (empty) null else "night") }
    MichiTheme(skin) {
        Scaffold(bottomBar = {
            Column {
                HomeMiniPlayer(listSampleSong, false, 42_000, 248_000, true, true, 0, {}, {}, {})
                HomeNavigation(2) {}
            }
        }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                ListsHome(112, if (empty) emptyList() else listSamples, active, loading, 0,
                    { active = null }, {}, { active = it })
            }
        }
    }
}

@Preview(name = "01 · Colección", group = "Listas real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun ListsCollectionPreview() = ListsPreviewContent()
@Preview(name = "02 · Sin listas", group = "Listas real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun ListsEmptyPreview() = ListsPreviewContent(empty = true)
@Preview(name = "03 · Texto grande", group = "Listas real", widthDp = 360, heightDp = 740, fontScale = 1.3f, showSystemUi = true)
@Composable private fun ListsLargePreview() = ListsPreviewContent()
@Preview(name = "04 · Rosa", group = "Listas real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun ListsRosePreview() = ListsPreviewContent(skin = MichiSkin.ROSE)
@Preview(name = "05 · Cargando", group = "Listas real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun ListsLoadingPreview() = ListsPreviewContent(loading = true)
