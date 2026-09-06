package com.ainalluna.michimusica.podcasts

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.ainalluna.michimusica.MainActivity
import com.ainalluna.michimusica.R
import com.ainalluna.michimusica.library.AudioCatalog
import com.ainalluna.michimusica.library.AudioSection
import kotlinx.coroutines.*
import java.io.File

internal fun podcastExtension(mime: String): String = when (mime.substringBefore(';').lowercase()) {
    "audio/mpeg", "audio/mp3" -> "mp3"
    "audio/mp4", "audio/m4a", "audio/x-m4a" -> "m4a"
    "audio/ogg", "audio/vorbis" -> "ogg"
    "audio/opus" -> "opus"
    "audio/aac", "audio/aacp" -> "aac"
    "audio/flac", "audio/x-flac" -> "flac"
    "audio/wav", "audio/x-wav", "audio/wave" -> "wav"
    else -> error("El formato de este audio no es compatible con Michi.")
}

internal fun podcastFilename(title: String, mime: String, exists: (String) -> Boolean): String {
    val stem = title.replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), "_").trim().trim('.').take(100).ifBlank { "Episodio" }
    val ext = podcastExtension(mime)
    for (index in 0..999) {
        val name = "$stem${if (index == 0) "" else " ($index)"}.$ext"
        if (!exists(name)) return name
    }
    error("Hay demasiados archivos con ese nombre.")
}

/** Owned by a foreground service, independent of navigation and activity recreation. */
class PodcastDownloadService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val repository by lazy { PodcastRepository.get(this) }
    private var runner: Job? = null
    private var active: Job? = null
    private var activeKey: String? = null

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION, notification("Preparando descarga…", -1))
        if (intent?.action == "cancel") {
            val key = intent.getStringExtra("key")
            if (key == activeKey) active?.cancel()
            scope.launch(Dispatchers.IO) {
                if (key != null && key != activeKey) repository.downloadUpdate(key) { it.copy(status = "cancelled", error = "") }
            }
        }
        if (runner?.isActive != true) runner = scope.launch {
            try {
                withContext(Dispatchers.IO) { repository.load() }
                while (true) {
                    val entry = repository.state.value.downloads.firstOrNull { it.status == "queued" } ?: break
                    activeKey = entry.key
                    active = launch { transfer(entry) }
                    active?.join(); active = null; activeKey = null
                }
            } finally { stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
        }
        return START_NOT_STICKY
    }

    private fun notification(title: String, progress: Int): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Descargas de podcasts", NotificationManager.IMPORTANCE_LOW))
        val open = PendingIntent.getActivity(this, 12, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_search_download)
            .setContentTitle("Descargando podcast").setContentText(title).setContentIntent(open)
            .setOnlyAlertOnce(true).setOngoing(true).setProgress(100, progress.coerceAtLeast(0), progress < 0).build()
    }

    private suspend fun transfer(entry: PodcastDownload) = withContext(Dispatchers.IO) {
        val temporary = File(cacheDir, "podcast-${entry.key}.part")
        var created: DocumentFile? = null
        var completed = false
        try {
            repository.downloadUpdate(entry.key) { it.copy(status = "downloading", error = "") }
            // Recheck public availability. Feed entries marked paid/previews are never downloaded.
            val fresh = PodcastNetwork.feed(entry.showUrl).episodes.firstOrNull { it.id == entry.episodeId }
                ?: error("Este episodio ya no figura como audio gratuito en el RSS.")
            val extension = podcastExtension(fresh.mime)
            val connection = PodcastNetwork.open(fresh.audio)
            try {
                val mime = connection.contentType.orEmpty().substringBefore(';').lowercase()
                require(mime.startsWith("audio/") || mime in listOf("application/octet-stream", "binary/octet-stream")) {
                    "El servidor no ha devuelto un archivo de audio público."
                }
                val expected = connection.contentLengthLong
                val max = 1024L * 1024 * 1024
                require(expected <= max) { "Este episodio supera el límite de 1 GB." }
                var bytes = 0L; var lastPercent = -2
                connection.inputStream.use { input -> temporary.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        bytes += count
                        require(bytes <= max) { "Este episodio supera el límite de 1 GB." }
                        output.write(buffer, 0, count)
                        val percent = if (expected > 0) ((bytes * 100 / expected).toInt().coerceIn(0, 99)) else -1
                        if (lastPercent != percent) {
                            lastPercent = percent
                            repository.progress.value = mapOf(entry.key to percent)
                            getSystemService(NotificationManager::class.java).notify(NOTIFICATION, notification(entry.title, percent))
                        }
                    }
                } }
                require(bytes > 0 && (expected < 0 || bytes == expected)) { "El audio se recibió incompleto. Reintenta la descarga." }
            } finally { connection.disconnect() }
            // Validate a real decodable local audio before exposing anything in the library.
            val retriever = android.media.MediaMetadataRetriever()
            try {
                retriever.setDataSource(temporary.absolutePath)
                require(retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) == "yes") {
                    "El archivo recibido no contiene una pista de audio."
                }
                requireCompletePodcast(fresh.duration,
                    retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L)
            } finally { retriever.release() }
            currentCoroutineContext().ensureActive()
            val root = DocumentFile.fromTreeUri(this@PodcastDownloadService, entry.folder.toUri()) ?: error("Vuelve a elegir la carpeta.")
            // A previous killed copy can leave only our journaled .part document; never delete existing audio.
            if (entry.uri.isNotBlank()) DocumentFile.fromSingleUri(this@PodcastDownloadService, entry.uri.toUri())?.let {
                if (it.name == "michi-${entry.key}.part") require(it.delete()) { "No se pudo limpiar la descarga interrumpida." }
            }
            val names = root.listFiles().mapNotNull { it.name }.toSet()
            val name = podcastFilename(entry.title, fresh.mime, names::contains)
            val target = root.createFile("application/octet-stream", "michi-${entry.key}.part") ?: error("No puedo crear el archivo. Comprueba la carpeta y el espacio libre.")
            created = target
            repository.downloadUpdate(entry.key) { it.copy(uri = target.uri.toString()) }
            contentResolver.openOutputStream(target.uri, "w")?.use { output ->
                temporary.inputStream().use { input ->
                    val buffer = ByteArray(65536)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = input.read(buffer); if (count < 0) break
                        output.write(buffer, 0, count)
                    }
                }
            } ?: error("No se pudo guardar el episodio en la carpeta.")
            withContext(NonCancellable) {
                require(target.renameTo(name)) { "La carpeta no permite finalizar el archivo. Elige otra carpeta." }
                require(target.name?.endsWith(".$extension", true) == true) { "La carpeta no conservó el formato del audio." }
                AudioCatalog(this@PodcastDownloadService).classify(target.uri.toString(), AudioSection.PODCASTS)
                repository.downloadUpdate(entry.key) { it.copy(uri = target.uri.toString(), status = "done", error = "", mime = fresh.mime, audio = fresh.audio) }
                completed = true
            }
        } catch (failure: Exception) {
            withContext(NonCancellable) {
                val cleaned = created?.let { runCatching { it.delete() }.getOrDefault(false) } ?: true
                repository.downloadUpdate(entry.key) {
                    it.copy(status = when (failure) { is CancellationException -> "cancelled"; is PodcastPreviewException -> "unavailable"; else -> "error" },
                        uri = if (cleaned) "" else it.uri, error = if (failure is CancellationException) "" else podcastError(failure))
                }
            }
        } finally {
            temporary.delete()
            repository.progress.value = emptyMap()
            if (completed) PodcastArtworkCache.prefetch(this@PodcastDownloadService, entry.image)
        }
    }

    override fun onTimeout(startId: Int, fgsType: Int) { scope.cancel(); stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
    override fun onDestroy() { scope.cancel(); super.onDestroy() }

    companion object {
        private const val CHANNEL = "podcast_downloads"
        private const val NOTIFICATION = 1201
        suspend fun request(context: Context, show: PodcastShow, episode: PodcastEpisode, folder: Uri) {
            val repo = PodcastRepository.get(context)
            val key = podcastId("${show.url}|${episode.id}|$folder")
            withContext(Dispatchers.IO) {
                repo.update { old ->
                    require(old.downloads.size < 10000 || old.downloads.any { it.key == key }) { "El catálogo de descargas está lleno." }
                    val previous = old.downloads.firstOrNull { it.key == key }
                    require(previous?.status !in listOf("queued", "downloading", "done", "music")) { "El episodio ya está descargado o en curso." }
                    val entry = PodcastDownload(key, show.url, episode.id, episode.title, show.title, episode.audio, episode.mime,
                        folder.toString(), uri = previous?.uri.orEmpty(), image = episode.image.ifBlank { show.image })
                    old.copy(downloads = old.downloads.filterNot { it.key == key } + entry)
                }
            }
            try { ContextCompat.startForegroundService(context, Intent(context, PodcastDownloadService::class.java)) }
            catch (failure: Exception) {
                withContext(Dispatchers.IO) { repo.downloadUpdate(key) { it.copy(status = "error", error = "No se pudo iniciar la descarga. Vuelve a intentarlo con Michi abierta.") } }
            }
        }
        fun cancel(context: Context, key: String) { context.startService(Intent(context, PodcastDownloadService::class.java).setAction("cancel").putExtra("key", key)) }
    }
}
