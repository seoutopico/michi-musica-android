package com.ainalluna.michimusica.library

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

object MusicFolderReader {
    private val supportedExtensions = setOf("mp3", "wav", "ogg", "m4a", "aac", "flac", "opus")

    fun isSupportedAudio(filename: String): Boolean =
        filename.substringAfterLast('.', "").lowercase() in supportedExtensions

    fun read(context: Context, treeUri: Uri): List<Song> {
        val root = DocumentFile.fromTreeUri(context, treeUri)
        require(root != null && root.isDirectory && root.canRead()) { "No puedo leer la carpeta autorizada." }
        return root.listFiles()
            .asSequence()
            .filter { it.isFile && isSupportedAudio(it.name.orEmpty()) }
            .map { file ->
                val filename = file.name.orEmpty()
                val metadata = runCatching {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, file.uri)
                        SongMetadata(
                            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).orEmpty(),
                            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).orEmpty(),
                            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM).orEmpty(),
                            durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L,
                        )
                    } finally {
                        retriever.release()
                    }
                }.getOrDefault(SongMetadata())
                Song(
                    id = file.uri.toString(),
                    filename = filename,
                    title = metadata.title.ifBlank { filename.substringBeforeLast('.', filename) },
                    artist = metadata.artist,
                    album = metadata.album,
                    durationMs = metadata.durationMs,
                    uri = file.uri,
                )
            }
            .sortedBy { it.title.lowercase() }
            .toList()
    }

    private data class SongMetadata(
        val title: String = "",
        val artist: String = "",
        val album: String = "",
        val durationMs: Long = 0L,
    )
}
