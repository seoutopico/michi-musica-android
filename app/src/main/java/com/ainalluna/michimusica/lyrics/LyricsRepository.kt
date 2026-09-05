package com.ainalluna.michimusica.lyrics

import com.ainalluna.michimusica.security.readBoundedText

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class LyricsMatch(
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val durationSeconds: Double,
    val syncedLyrics: String?,
    val plainLyrics: String?,
) {
    val content: String get() = syncedLyrics?.takeIf(String::isNotBlank) ?: plainLyrics.orEmpty()
    val synced: Boolean get() = !syncedLyrics.isNullOrBlank()
}

object LyricsRepository {
    suspend fun search(title: String, artist: String): List<LyricsMatch> = withContext(Dispatchers.IO) {
        require(title.isNotBlank()) { "Escribe el título de la canción." }
        val endpoint = Uri.parse("https://lrclib.net/api/search").buildUpon()
            .appendQueryParameter("track_name", title.take(160))
            .apply { if (artist.isNotBlank()) appendQueryParameter("artist_name", artist.take(160)) }
            .build().toString()
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 12_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Michi-Musica-Android/1.1 (private local player)")
        }
        try {
            require(connection.responseCode in 200..299) { "LRCLIB respondió ${connection.responseCode}." }
            val body = connection.inputStream.bufferedReader().use { it.readBoundedText(300_000) }
            require(body.length <= 300_000) { "La respuesta de letras es demasiado grande." }
            val records = JSONArray(body)
            buildList {
                for (index in 0 until minOf(records.length(), 8)) {
                    val item = records.getJSONObject(index)
                    val synced = item.optString("syncedLyrics").takeIf { it.isNotBlank() && it != "null" }
                    val plain = item.optString("plainLyrics").takeIf { it.isNotBlank() && it != "null" }
                    if (synced != null || plain != null) add(
                        LyricsMatch(
                            trackName = item.optString("trackName", title),
                            artistName = item.optString("artistName", artist),
                            albumName = item.optString("albumName"),
                            durationSeconds = item.optDouble("duration", 0.0),
                            syncedLyrics = synced,
                            plainLyrics = plain,
                        ),
                    )
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}
