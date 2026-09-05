package com.ainalluna.michimusica.lyrics

import com.ainalluna.michimusica.security.readBoundedText

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class StoredLyrics(val content: String, val synced: Boolean)

object LyricsStorage {
    private const val DIRECTORY = "Michi Letras"

    suspend fun load(context: Context, rootUri: Uri, audioFilename: String): StoredLyrics? = withContext(Dispatchers.IO) {
        val folder = DocumentFile.fromTreeUri(context, rootUri)?.findFile(DIRECTORY) ?: return@withContext null
        val stem = safeStem(audioFilename)
        listOf("$stem.lrc" to true, "$stem.txt" to false).firstNotNullOfOrNull { (name, synced) ->
            folder.findFile(name)?.uri?.let { uri ->
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readBoundedText(300_000) }
                content?.takeIf { it.length <= 300_000 }?.let { StoredLyrics(it, synced) }
            }
        }
    }

    suspend fun save(context: Context, rootUri: Uri, audioFilename: String, content: String, synced: Boolean) =
        withContext(Dispatchers.IO) {
            require(content.isNotBlank() && content.length <= 300_000) { "La letra no es válida." }
            val root = DocumentFile.fromTreeUri(context, rootUri) ?: error("No puedo abrir la carpeta de música.")
            val folder = root.findFile(DIRECTORY) ?: root.createDirectory(DIRECTORY)
                ?: error("No puedo crear la carpeta Michi Letras.")
            val stem = safeStem(audioFilename)
            val extension = if (synced) "lrc" else "txt"
            val obsolete = if (synced) "$stem.txt" else "$stem.lrc"
            val target = folder.findFile("$stem.$extension")
                ?: folder.createFile("text/plain", "$stem.$extension")
                ?: error("No puedo crear el archivo de letra.")
            context.contentResolver.openOutputStream(target.uri, "wt")?.bufferedWriter()?.use { it.write(content) }
                ?: error("No puedo guardar la letra.")
            folder.findFile(obsolete)?.let { require(it.delete()) { "No se pudo retirar la versión anterior de la letra." } }
        }

    suspend fun remove(context: Context, rootUri: Uri, audioFilename: String) = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, rootUri) ?: error("No puedo abrir la carpeta de música.")
        require(root.canRead()) { "No puedo leer la carpeta de música." }
        val folder = root.findFile(DIRECTORY) ?: return@withContext
        val stem = safeStem(audioFilename)
        folder.findFile("$stem.lrc")?.let { require(it.delete()) { "No se pudo quitar la letra sincronizada." } }
        folder.findFile("$stem.txt")?.let { require(it.delete()) { "No se pudo quitar la letra." } }
        if (folder.listFiles().isEmpty()) folder.delete()
    }

    internal fun safeStem(filename: String): String = filename.substringBeforeLast('.', filename)
        .replace(Regex("""[\\/:*?\"<>|]"""), "_")
        .take(120)
        .ifBlank { "cancion" }
}
