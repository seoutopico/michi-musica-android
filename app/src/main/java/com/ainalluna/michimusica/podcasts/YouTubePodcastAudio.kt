package com.ainalluna.michimusica.podcasts

import android.content.Context
import com.ainalluna.michimusica.youtube.YouTubeRuntime
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.net.URI
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/** Prepare private audio only. The podcast service owns the cancellable SAF commit and receipt. */
internal object YouTubePodcastAudio {
    suspend fun prepare(context: Context, entry: PodcastDownload, target: File, progress: (Int) -> Unit): PodcastEpisode = withTimeout(30 * 60 * 1000L) {
        val source = URI(publicHttps(entry.audio))
        val id = source.query.orEmpty().removePrefix("v=")
        require(source.host == "www.youtube.com" && source.path == "/watch" && Regex("[A-Za-z0-9_-]{11}").matches(id) &&
            entry.episodeId == podcastId("youtube:$id")) { "El episodio de YouTube no es válido." }
        val channel = YouTubePodcasts.normalize(entry.showUrl).substringAfterLast('/')
        val work = File(context.cacheDir, "podcast-youtube-${UUID.randomUUID()}")
        check(work.mkdirs()) { "No puedo preparar el audio. Comprueba el espacio libre." }
        try {
            YouTubeRuntime.ensureCurrent(context)
            FFmpeg.getInstance().init(context.applicationContext)
            fun request() = YoutubeDLRequest(source.toString()).apply {
                addOption("--ignore-config"); addOption("--no-playlist"); addOption("--no-warnings")
                addOption("--socket-timeout", "20"); addOption("--retries", "2")
            }
            val metadata = JSONObject(execute(request().apply { addOption("--dump-single-json") }, progress = {}).out)
            fun verify(info: JSONObject) {
                require(info.optString("id") == id && info.optString("channel_id") == channel) { "El vídeo no corresponde a este canal." }
                require(info.optString("availability") == "public" && !restrictedPodcast(info.optString("title"), info.optString("description"))) {
                    "Este vídeo no está disponible como contenido público gratuito."
                }
                require(info.optString("live_status") !in setOf("is_live", "is_upcoming", "post_live")) {
                    "El directo todavía no está disponible como episodio terminado. Inténtalo cuando YouTube publique su grabación."
                }
            }
            verify(metadata)
            currentCoroutineContext().ensureActive()
            execute(request().apply {
                addOption("--match-filter", "availability = public & !is_live")
                addOption("--format", "bestaudio/best"); addOption("--max-filesize", "1G")
                addOption("--extract-audio"); addOption("--audio-format", "mp3"); addOption("--audio-quality", "0")
                addOption("--embed-metadata"); addOption("--write-info-json")
                addOption("--output", File(work, "episode.%(ext)s").absolutePath)
            }, progress)
            currentCoroutineContext().ensureActive()
            val info = File(work, "episode.info.json")
            require(info.isFile && info.length() <= PodcastFeed.MAX_BYTES) { "No se pudo comprobar el audio de YouTube." }
            val finalMetadata = JSONObject(info.readText())
            verify(finalMetadata)
            val mp3 = File(work, "episode.mp3")
            require(mp3.isFile && mp3.length() in 1..(1024L * 1024 * 1024)) { "YouTube no ha proporcionado un audio completo de menos de 1 GB." }
            check(mp3.renameTo(target)) { "No puedo preparar el archivo descargado." }
            PodcastEpisode(entry.episodeId, entry.title, "", source.toString(), "audio/mpeg", 0,
                (finalMetadata.optDouble("duration", 0.0) * 1000).toLong(), entry.image)
        } catch (failure: Exception) {
            currentCoroutineContext().ensureActive()
            if (failure is com.yausername.youtubedl_android.YoutubeDLException) throw IllegalStateException(
                "YouTube no permite descargar este audio ahora. Puede requerir acceso o estar temporalmente bloqueado. Reinténtalo más tarde.", failure)
            throw failure
        } finally { work.deleteRecursively() } // Private UUID directory created above; never a user folder.
    }

    private suspend fun execute(request: YoutubeDLRequest, progress: (Int) -> Unit) = coroutineScope {
        val processId = "podcast-${UUID.randomUUID()}"
        val finished = AtomicBoolean(false)
        // execute() blocks a worker thread. Keep trying after cancellation to cover process-start races.
        val cancellation = launch(Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) {
            try { awaitCancellation() } finally {
                withContext(NonCancellable) {
                    while (!finished.get()) {
                        YoutubeDL.getInstance().destroyProcessById(processId)
                        delay(50)
                    }
                }
            }
        }
        try {
            withContext(Dispatchers.IO) {
                try { YoutubeDL.getInstance().execute(request, processId) { value, _, _ -> progress(value.toInt().coerceIn(0, 99)) } }
                finally { finished.set(true) }
            }
        } finally {
            finished.set(true)
            cancellation.cancelAndJoin()
        }
    }
}
