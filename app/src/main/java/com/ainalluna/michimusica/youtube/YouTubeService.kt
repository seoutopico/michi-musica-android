package com.ainalluna.michimusica.youtube

import android.content.Context
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URI

private val VIDEO_ID = Regex("^[A-Za-z0-9_-]{11}$")

internal sealed interface YouTubeInput {
    data class Search(val query: String) : YouTubeInput
    data class Video(val id: String) : YouTubeInput
}

data class YouTubeResult(
    val id: String,
    val title: String,
    val channel: String,
    val durationSeconds: Long,
    val thumbnail: String,
)

internal fun classifyYouTubeInput(raw: String): YouTubeInput {
    val input = raw.trim()
    require(input.isNotBlank()) { "Escribe una canción, artista o enlace." }
    require(input.length <= 300) { "La búsqueda es demasiado larga." }
    if (!input.startsWith("http://", true) && !input.startsWith("https://", true)) {
        return YouTubeInput.Search(input.take(200))
    }
    val uri = runCatching { URI(input) }.getOrNull() ?: error("El enlace no es válido.")
    val host = uri.host?.lowercase().orEmpty()
    require(host in setOf("youtube.com", "www.youtube.com", "m.youtube.com", "music.youtube.com", "youtu.be")) {
        "El enlace debe ser de YouTube."
    }
    val segments = uri.path.orEmpty().trim('/').split('/').filter(String::isNotBlank)
    val id = when {
        host == "youtu.be" -> segments.firstOrNull()
        uri.path == "/watch" -> uri.rawQuery.orEmpty().split('&').firstOrNull { it.startsWith("v=") }?.substringAfter("v=")
        segments.firstOrNull() in setOf("shorts", "live", "embed") -> segments.getOrNull(1)
        else -> null
    }
    require(id != null && VIDEO_ID.matches(id)) {
        "Copia el enlace de un vídeo de YouTube, no el de una lista o canal."
    }
    return YouTubeInput.Video(id)
}

object YouTubeService {
    private fun initialize(context: Context) {
        YouTubeRuntime.initialize(context)
        FFmpeg.getInstance().init(context.applicationContext)
    }

    suspend fun search(context: Context, raw: String): List<YouTubeResult> = withContext(Dispatchers.IO) {
        initialize(context)
        val input = classifyYouTubeInput(raw)
        val source = when (input) {
            is YouTubeInput.Search -> "ytsearch8:${input.query}"
            is YouTubeInput.Video -> "https://www.youtube.com/watch?v=${input.id}"
        }
        val request = YoutubeDLRequest(source).apply {
            addOption("--dump-single-json")
            addOption("--no-warnings")
            if (input is YouTubeInput.Search) addOption("--flat-playlist") else addOption("--no-playlist")
        }
        parseSearchResponse(YoutubeDL.getInstance().execute(request).out, input is YouTubeInput.Video)
    }

    internal fun parseSearchResponse(raw: String, singleVideo: Boolean): List<YouTubeResult> {
        val root = JSONObject(raw)
        val entries = if (singleVideo) listOf(root) else buildList {
            val array = root.optJSONArray("entries") ?: return@buildList
            for (index in 0 until minOf(array.length(), 8)) array.optJSONObject(index)?.let(::add)
        }
        return entries.mapNotNull { item ->
            val id = item.optString("id")
            if (!VIDEO_ID.matches(id)) return@mapNotNull null
            val thumbnails = item.optJSONArray("thumbnails")
            YouTubeResult(
                id = id,
                title = item.optString("title").ifBlank { "Sin título" },
                channel = item.optString("channel").ifBlank { item.optString("uploader", "YouTube") },
                durationSeconds = item.optLong("duration", 0L),
                thumbnail = thumbnails?.optJSONObject(thumbnails.length() - 1)?.optString("url").orEmpty(),
            )
        }
    }
}
