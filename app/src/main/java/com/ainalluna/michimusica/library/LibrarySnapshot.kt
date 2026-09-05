package com.ainalluna.michimusica.library

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

/** Private, versioned cache. No audio bytes or editable tags are stored here. */
internal data class CachedAudio(
    val uri: String, val filename: String, val modified: Long, val size: Long,
    val title: String, val artist: String, val album: String, val duration: Long,
) {
    fun matches(name: String, lastModified: Long, length: Long): Boolean =
        modified > 0 && size > 0 && filename == name && modified == lastModified && size == length
}

internal data class LibrarySnapshot(val folder: String, val entries: List<CachedAudio>) {
    fun write(output: OutputStream) {
        val data = DataOutputStream(output)
        data.writeInt(1)
        data.writeUTF(folder)
        data.writeInt(entries.size)
        entries.forEach {
            data.writeUTF(it.uri); data.writeUTF(it.filename)
            data.writeLong(it.modified); data.writeLong(it.size)
            data.writeUTF(it.title); data.writeUTF(it.artist); data.writeUTF(it.album)
            data.writeLong(it.duration)
        }
        data.flush()
    }

    companion object {
        fun read(input: InputStream): LibrarySnapshot {
            val data = DataInputStream(input)
            require(data.readInt() == 1)
            val folder = data.readUTF()
            val count = data.readInt()
            require(count in 0..100_000)
            return LibrarySnapshot(folder, List(count) {
                CachedAudio(data.readUTF(), data.readUTF(), data.readLong(), data.readLong(),
                    data.readUTF(), data.readUTF(), data.readUTF(), data.readLong())
            })
        }
    }
}
