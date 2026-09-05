package com.ainalluna.michimusica.youtube

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ainalluna.michimusica.library.AudioCatalog
import com.ainalluna.michimusica.library.AudioSection
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** State belongs to the app root so switching tabs keeps the search and any current download. */
data class SearchUiState(
    val query: String = "", val submitted: String? = null,
    val results: List<YouTubeResult> = emptyList(), val searching: Boolean = false,
    val searchError: String? = null,
    val previewId: String? = null, val previewPlaying: Boolean = false,
    val video: YouTubeResult? = null, val videoPosition: Double = 0.0, val videoAutoplay: Boolean = false,
    val downloadingId: String? = null, val progress: Int = 0,
    val pendingDownload: YouTubeResult? = null,
    val savedIds: Set<String> = emptySet(), val rowError: Pair<String, String>? = null,
) {
    val busy: Boolean get() = searching || downloadingId != null
}

@Stable
class YouTubeSearchController(private val context: Context, private val scope: CoroutineScope) {
    var ui by mutableStateOf(SearchUiState())
        private set
    private val catalog = AudioCatalog(context)
    var downloadSection by mutableStateOf(catalog.lastDownloadSection)
    var folderUri: Uri? = null
    var onPreviewStarted: () -> Unit = {}
    var onDownloaded: (String) -> Unit = {}
    var pauseVideo: () -> Unit = {}
    fun release() { pauseVideo(); pauseVideo = {} }
    fun editQuery(value: String) { ui = ui.copy(query = value.take(300), searchError = null) }
    fun pausePreview() {
        pauseVideo()
        ui = ui.copy(previewPlaying = false, videoAutoplay = false)
    }
    fun closeVideo() {
        pausePreview()
        ui = ui.copy(video = null, previewId = null, videoPosition = 0.0)
    }
    fun videoPlaying(id: String, playing: Boolean) {
        if (ui.video?.id != id) return
        if (playing) onPreviewStarted()
        ui = ui.copy(previewPlaying = playing)
    }
    fun videoPosition(id: String, seconds: Double) {
        if (ui.video?.id == id && seconds.isFinite() && seconds >= 0) ui = ui.copy(videoPosition = seconds)
    }
    fun search() {
        if (ui.busy || ui.query.isBlank()) return
        val query = ui.query.trim()
        closeVideo()
        ui = ui.copy(searching = true, submitted = query, results = emptyList(), searchError = null, rowError = null)
        scope.launch {
            try {
                val results = YouTubeService.search(context, query)
                ui = ui.copy(results = results)
            }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { ui = ui.copy(searchError = "Comprueba la conexión y usa el nombre de una canción o el enlace de un vídeo de YouTube.") }
            finally { ui = ui.copy(searching = false) }
        }
    }
    fun preview(result: YouTubeResult) {
        if (ui.busy) return
        if (ui.video?.id == result.id) return
        closeVideo()
        onPreviewStarted()
        ui = ui.copy(video = result, previewId = result.id, videoAutoplay = true, rowError = null)
    }
    fun requestDownload(result: YouTubeResult) {
        if (!ui.busy && result.id !in ui.savedIds) ui = ui.copy(pendingDownload = result)
    }
    fun cancelDownloadChoice() { ui = ui.copy(pendingDownload = null) }
    fun confirmDownload() {
        val result = ui.pendingDownload ?: return
        ui = ui.copy(pendingDownload = null)
        catalog.lastDownloadSection = downloadSection
        download(result, downloadSection)
    }
    private fun download(result: YouTubeResult, section: AudioSection) {
        val destination = folderUri ?: return
        if (ui.busy || result.id in ui.savedIds) return
        closeVideo()
        ui = ui.copy(downloadingId = result.id, progress = 0, rowError = null)
        scope.launch {
            try {
                val filename = YouTubeDownloader.downloadMp3(context, destination, result.id, section) { next ->
                    scope.launch { ui = ui.copy(progress = next) }
                }
                ui = ui.copy(savedIds = ui.savedIds + result.id)
                onDownloaded(filename)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { ui = ui.copy(rowError = result.id to "No se ha guardado. Revisa la conexión y el acceso a tu carpeta; vuelve a pulsar Guardar MP3.") }
            finally { ui = ui.copy(downloadingId = null) }
        }
    }
}

@Composable
fun rememberYouTubeSearchController(folderUri: Uri?, onPreviewStarted: () -> Unit, onDownloaded: (String) -> Unit): YouTubeSearchController {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val controller = remember { YouTubeSearchController(context, scope) }
    SideEffect {
        controller.folderUri = folderUri
        controller.onPreviewStarted = onPreviewStarted
        controller.onDownloaded = onDownloaded
    }
    DisposableEffect(controller) { onDispose { controller.release() } }
    return controller
}

@Composable
fun YouTubeScreen(controller: YouTubeSearchController, onChooseFolder: () -> Unit) {
    controller.ui.pendingDownload?.let { result ->
        AlertDialog(onDismissRequest = controller::cancelDownloadChoice,
            title = { Text("Guardar audio") },
            text = { Column {
                Text(result.title)
                Spacer(Modifier.height(12.dp))
                AudioSection.entries.forEach { section ->
                    Row(Modifier.fillMaxWidth().selectable(controller.downloadSection == section, role = Role.RadioButton,
                        onClick = { controller.downloadSection = section }).padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(controller.downloadSection == section, onClick = null)
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(section.label)
                            Text(if (section == AudioSection.PODCASTS) "Separado de tu música. Guarda tu posición." else "En tu biblioteca de canciones y Azar.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } },
            confirmButton = { TextButton(controller::confirmDownload) { Text("Guardar en ${controller.downloadSection.label}") } },
            dismissButton = { TextButton(controller::cancelDownloadChoice) { Text("Cancelar") } })
    }
    SearchContent(controller.ui, controller.folderUri != null, controller::editQuery, controller::search,
        controller::preview, controller::requestDownload, onChooseFolder,
        videoPlayer = controller.ui.video?.let { result ->
            { YouTubeVideoPlayer(result, controller.ui.videoPosition, controller.ui.videoAutoplay,
                onPlaying = { controller.videoPlaying(result.id, it) },
                onPosition = { controller.videoPosition(result.id, it) },
                registerPause = { controller.pauseVideo = it }, onClose = controller::closeVideo) }
        })
}
