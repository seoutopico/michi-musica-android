package com.ainalluna.michimusica.podcasts

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.ainalluna.michimusica.library.Song
import kotlinx.coroutines.*

@Stable
class PodcastController internal constructor(val context: Context, private val scope: CoroutineScope) {
    val repository = PodcastRepository.get(context)
    var ready by mutableStateOf(false); private set
    var error by mutableStateOf(""); private set
    var adding by mutableStateOf(false); private set
    var addError by mutableStateOf(""); private set

    internal suspend fun start() {
        try {
            withContext(Dispatchers.IO) { repository.load(); PodcastRefreshService.schedule(context) }
            ready = true
        } catch (failure: Exception) { error = "No pude leer los podcasts guardados. Cierra y vuelve a abrir Michi; tus audios siguen en la carpeta." }
    }
    private fun action(block: suspend () -> Unit) { scope.launch {
        try { block(); error = "" } catch (cancel: CancellationException) { throw cancel }
        catch (failure: Exception) { error = podcastError(failure) }
    } }
    fun refresh() = action { repository.refresh() }
    fun follow(url: String, done: () -> Unit) {
        if (adding) return
        adding = true; addError = ""
        scope.launch {
            try { repository.follow(url); done() }
            catch (cancel: CancellationException) { throw cancel }
            catch (failure: Exception) { addError = podcastError(failure) }
            finally { adding = false }
        }
    }
    fun clearAddError() { addError = "" }
    fun dismissError() { error = "" }
    fun unfollow(url: String) = action { repository.unfollow(url) }
    fun seen(url: String? = null) = action { repository.markSeen(url) }
    fun settings(automatic: Boolean, notifications: Boolean) = action { repository.settings(automatic, notifications) }
    fun download(show: PodcastShow, episode: PodcastEpisode, folder: Uri) = action { PodcastDownloadService.request(context, show, episode, folder) }
    fun cancel(key: String) = action { PodcastDownloadService.cancel(context, key) }
    fun retry(entry: PodcastDownload) = action {
        PodcastDownloadService.request(context,
            PodcastShow(entry.showUrl, entry.author, "", entry.image, emptyList()),
            PodcastEpisode(entry.episodeId, entry.title, "", entry.audio, entry.mime, 0, 0, entry.image), entry.folder.toUri())
    }
    fun reconcile(songs: List<Song>, folder: Uri) = action {
        val ids = songs.map { it.id }.toSet()
        withContext(Dispatchers.IO) {
            val podcastIds = com.ainalluna.michimusica.library.AudioCatalog(context).podcastIds()
            fun reconciled(d: PodcastDownload) = if (d.status in listOf("done", "music") && d.folder == folder.toString()) when {
                d.uri !in ids && androidx.documentfile.provider.DocumentFile.fromSingleUri(context, d.uri.toUri())?.exists() != true -> d.copy(status = "missing", uri = "")
                d.uri !in podcastIds -> d.copy(status = "music")
                else -> d.copy(status = "done")
            } else d
            if (repository.state.value.downloads.any { reconciled(it) != it })
                repository.update { it.copy(downloads = it.downloads.map(::reconciled)) }
        }
    }
}

@Composable
fun rememberPodcastController(): PodcastController {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val controller = remember { PodcastController(context, scope) }
    LaunchedEffect(controller) { controller.start() }
    return controller
}
