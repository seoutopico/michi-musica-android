package com.ainalluna.michimusica.youtube

import android.content.Context
import android.net.Uri
import com.ainalluna.michimusica.library.AudioCatalog
import com.ainalluna.michimusica.library.AudioSection
import androidx.documentfile.provider.DocumentFile
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

internal fun availableMp3Name(filename: String, exists: (String) -> Boolean): String {
    val stem = filename.removeSuffix(".mp3").replace(Regex("""[\\/:*?\"<>|]"""), "_").take(160).ifBlank { "cancion" }
    for (index in 0..999) {
        val name = if (index == 0) "$stem.mp3" else "$stem ($index).mp3"
        if (!exists(name)) return name
    }
    error("Ya hay demasiados archivos con ese nombre.")
}

object YouTubeDownloader {
    suspend fun downloadMp3(
        context: Context,
        folderUri: Uri,
        videoId: String,
        section: AudioSection = AudioSection.MUSIC,
        onProgress: (Int) -> Unit,
    ): String = withContext(Dispatchers.IO) {
        require(Regex("^[A-Za-z0-9_-]{11}$").matches(videoId)) { "El vídeo seleccionado no es válido." }
        val source = "https://www.youtube.com/watch?v=$videoId"
        val work = File(context.cacheDir, "michi-download-${UUID.randomUUID()}")
        check(work.mkdirs()) { "No se pudo preparar la descarga." }
        try {

        YouTubeRuntime.ensureCurrent(context)
        FFmpeg.getInstance().init(context.applicationContext)
        val request = YoutubeDLRequest(source).apply {
            addOption("--no-playlist")
            addOption("--extract-audio")
            addOption("--audio-format", "mp3")
            addOption("--audio-quality", "0")
            addOption("--embed-metadata")
            addOption("--embed-thumbnail")
            addOption("--convert-thumbnails", "jpg")
            addOption("--output", File(work, "%(title).160B [%(id)s].%(ext)s").absolutePath)
        }
        YoutubeDL.getInstance().execute(request, "michi-mp3") { progress, _, _ ->
            onProgress(progress.toInt().coerceIn(0, 100))
        }
        val mp3 = work.listFiles()?.filter { it.isFile && it.extension.equals("mp3", ignoreCase = true) }
            ?.maxByOrNull(File::lastModified)
            ?: error("La descarga terminó, pero no encuentro el MP3.")
        val root = DocumentFile.fromTreeUri(context, folderUri) ?: error("No puedo abrir la carpeta de música.")
        val safeName = availableMp3Name(mp3.name) { root.findFile(it) != null }
        val target = root.createFile("audio/mpeg", safeName) ?: error("No puedo crear el MP3 en la carpeta.")
        try {
            context.contentResolver.openOutputStream(target.uri, "w")?.use { output ->
                mp3.inputStream().use { input -> input.copyTo(output) }
            } ?: error("No puedo copiar el MP3 a la carpeta.")
        } catch (failure: Exception) {
            target.delete() // Only the new, incomplete document created by this operation.
            throw failure
        }
        AudioCatalog(context).classify(target.uri.toString(), section)
        target.name ?: safeName
        } finally { work.deleteRecursively() } // Private UUID directory owned by this operation.
    }
}
