package com.ainalluna.michimusica.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.ainalluna.michimusica.library.Song

@Composable
internal fun PlayerPreviewContent(skin: MichiSkin = MichiSkin.MIDNIGHT, longTitle: Boolean = false, failed: Boolean = false) {
    val song = Song("preview", "Luz.mp3", if (longTitle) "Luz de septiembre — una noche junto al mar" else "Luz de septiembre",
        "Brisa", durationMs = 248_000, uri = "content://preview/luz".toUri())
    var playing by remember { mutableStateOf(false) }
    var shuffle by remember { mutableStateOf(false) }
    var repeat by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(84_000) }
    MichiTheme(skin) {
        Scaffold(bottomBar = { HomeNavigation(0) {} }) { padding ->
            PlayerHome(song, playing, position, 248_000, shuffle, repeat, true, true, true, false, failed,
                "Noches tranquilas", 0, {}, { playing = !playing }, { position = 0 }, { position = 0 },
                { position = it }, { shuffle = !shuffle }, { repeat = !repeat }, {}, {}, Modifier.padding(padding))
        }
    }
}

@Preview(name = "01 · Medianoche", group = "Player real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun PlayerMidnightPreview() = PlayerPreviewContent()
@Preview(name = "02 · Texto grande", group = "Player real", widthDp = 360, heightDp = 740, fontScale = 1.3f, showSystemUi = true)
@Composable private fun PlayerLargePreview() = PlayerPreviewContent(longTitle = true)
@Preview(name = "03 · Rosa", group = "Player real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun PlayerRosePreview() = PlayerPreviewContent(skin = MichiSkin.ROSE)
@Preview(name = "04 · Error", group = "Player real", widthDp = 412, heightDp = 860, showSystemUi = true)
@Composable private fun PlayerErrorPreview() = PlayerPreviewContent(failed = true)
@Preview(name = "05 · Horizontal", group = "Player real", widthDp = 800, heightDp = 400, showSystemUi = true)
@Composable private fun PlayerLandscapePreview() = PlayerPreviewContent(longTitle = true)
