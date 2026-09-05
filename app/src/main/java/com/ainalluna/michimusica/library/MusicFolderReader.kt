package com.ainalluna.michimusica.library

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import android.util.AtomicFile
import java.io.File

object MusicFolderReader {
    private val supportedExtensions = setOf("mp3", "wav", "ogg", "m4a", "aac", "flac", "opus")

    fun isSupportedAudio(filename: String): Boolean =
        filename.substringAfterLast('.', "").lowercase() in supportedExtensions

    @Volatile private var memory: LibrarySnapshot? = null

    private fun cacheFile(context: Context) = AtomicFile(File(context.filesDir, "library-snapshot.bin"))

    @Synchronized private fun snapshot(context: Context, treeUri: Uri): LibrarySnapshot? {
        val saved = memory ?: runCatching { cacheFile(context).openRead().use(LibrarySnapshot::read) }
            .getOrNull()?.also { memory = it }
        return saved?.takeIf { it.folder == treeUri.toString() }
    }

    fun cached(context: Context, treeUri: Uri): List<Song>? =
        snapshot(context, treeUri)?.entries?.map { it.song() }

    private fun CachedAudio.song() = Song(uri, filename, title, artist, album, duration, Uri.parse(uri))

    @Synchronized
    fun forget(context: Context, treeUri: Uri, id: String) {
        snapshot(context, treeUri)?.let { save(context, it.copy(entries = it.entries.filterNot { item -> item.uri == id })) }
    }

    private fun save(context: Context, value: LibrarySnapshot) {
        memory = value
        val file = cacheFile(context)
        // A failed cache write must never make a readable music folder look inaccessible.
        runCatching {
            val output = file.startWrite()
            try { value.write(output); file.finishWrite(output) }
            catch (failure: Exception) { file.failWrite(output); throw failure }
        }
    }

    @Synchronized
    fun read(context: Context, treeUri: Uri, force: Boolean = false): List<Song> {
        val previous = snapshot(context, treeUri)?.entries?.associateBy { it.uri }.orEmpty()
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
        val columns = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_LAST_MODIFIED, DocumentsContract.Document.COLUMN_SIZE)
        val found = mutableListOf<CachedAudio>()
        val cursor = context.contentResolver.query(children, columns, null, null, null)
            ?: error("No puedo leer la carpeta autorizada.")
        cursor.use {
            while (it.moveToNext()) {
                val filename = it.getString(1).orEmpty()
                if (it.getString(2) == DocumentsContract.Document.MIME_TYPE_DIR || !isSupportedAudio(filename)) continue
                val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, it.getString(0))
                val modified = it.getLong(3)
                val size = it.getLong(4)
                val cached = previous[uri.toString()]
                if (!force && cached?.matches(filename, modified, size) == true) {
                    found.add(cached)
                    continue
                }
                val metadata = runCatching {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, uri)
                        SongMetadata(
                            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).orEmpty(),
                            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).orEmpty(),
                            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM).orEmpty(),
                            durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L,
                        )
                    } finally {
                        retriever.release()
                    }
                }.getOrNull()
                found.add(CachedAudio(uri.toString(), filename, if (metadata == null) 0L else modified, size,
                    metadata?.title.orEmpty().ifBlank { filename.substringBeforeLast('.', filename) },
                    metadata?.artist.orEmpty(), metadata?.album.orEmpty(), metadata?.durationMs ?: 0L))
            }
        }
        val result = LibrarySnapshot(treeUri.toString(), found.sortedBy { it.title.lowercase() })
        save(context, result)
        return result.entries.map { it.song() }
    }

    private data class SongMetadata(
        val title: String = "",
        val artist: String = "",
        val album: String = "",
        val durationMs: Long = 0L,
    )
}
