package com.ainalluna.michimusica.lyrics

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.ainalluna.michimusica.library.Song

@Composable
fun LyricsScreen(song: Song, folderUri: Uri, positionMs: Long, onBack: (() -> Unit)? = null,
                 onSeek: (Long) -> Unit = {}, onChooseFolder: () -> Unit = {}) {
    key(song.id, folderUri) {
        val context = LocalContext.current.applicationContext
        val scope = rememberCoroutineScope()
        val controller = remember { LyricsController(context, folderUri, song, scope) }
        LaunchedEffect(controller) { controller.load() }
        val back = { if (!controller.back()) onBack?.invoke(); Unit }
        BackHandler(controller.ui.editing || controller.ui.candidate != null) { controller.back() }
        LyricsHome(song, controller.ui, positionMs, back, controller::edit, controller::editTitle,
            controller::editArtist, controller::search, controller::preview, controller::save,
            controller::remove, onSeek, onChooseFolder)
    }
}
