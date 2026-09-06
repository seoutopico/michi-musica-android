package com.ainalluna.michimusica.podcasts

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

data class PodcastDownload(
    val key: String, val showUrl: String, val episodeId: String, val title: String, val author: String,
    val audio: String, val mime: String, val folder: String, val uri: String = "", val status: String = "queued",
    val error: String = "", val image: String = "",
)

data class PodcastState(
    val shows: List<PodcastShow> = emptyList(), val downloads: List<PodcastDownload> = emptyList(),
    val automatic: Boolean = true, val notifications: Boolean = false,
)

/** One atomic snapshot: following, seen episode identities and download receipts move together. */
internal object PodcastStore {
    fun write(value: PodcastState, output: OutputStream) {
        val d = DataOutputStream(output)
        d.writeInt(1); d.writeBoolean(value.automatic); d.writeBoolean(value.notifications)
        d.writeInt(value.shows.size)
        value.shows.forEach { s ->
            listOf(s.url, s.title, s.author, s.image, s.error).forEach(d::writeUTF)
            d.writeLong(s.checked); d.writeInt(s.excluded)
            d.writeInt(s.seen.size); s.seen.forEach(d::writeUTF)
            d.writeInt(s.episodes.size)
            s.episodes.forEach { e ->
                listOf(e.id, e.title, e.description, e.audio, e.mime, e.image).forEach(d::writeUTF)
                d.writeLong(e.published); d.writeLong(e.duration); d.writeBoolean(e.isNew)
            }
        }
        d.writeInt(value.downloads.size)
        value.downloads.forEach { e ->
            listOf(e.key, e.showUrl, e.episodeId, e.title, e.author, e.audio, e.mime, e.folder, e.uri, e.status, e.error, e.image).forEach(d::writeUTF)
        }
        d.flush()
    }

    fun read(input: InputStream): PodcastState {
        val d = DataInputStream(input)
        require(d.readInt() == 1)
        fun count(max: Int) = d.readInt().also { require(it in 0..max) }
        val automatic = d.readBoolean(); val notifications = d.readBoolean()
        val shows = List(count(100)) {
            val url = d.readUTF(); val title = d.readUTF(); val author = d.readUTF(); val image = d.readUTF(); val error = d.readUTF()
            val checked = d.readLong(); val excluded = d.readInt()
            val seen = List(count(20000)) { d.readUTF() }.toSet()
            val episodes = List(count(1000)) {
                val id = d.readUTF(); val t = d.readUTF(); val description = d.readUTF(); val audio = d.readUTF(); val mime = d.readUTF(); val img = d.readUTF()
                PodcastEpisode(id, t, description, audio, mime, d.readLong(), d.readLong(), img, d.readBoolean())
            }
            PodcastShow(url, title, author, image, episodes, checked, error, seen, excluded)
        }
        val downloads = List(count(10000)) {
            PodcastDownload(d.readUTF(), d.readUTF(), d.readUTF(), d.readUTF(), d.readUTF(), d.readUTF(), d.readUTF(), d.readUTF(), d.readUTF(), d.readUTF(), d.readUTF(), d.readUTF())
        }
        require(d.read() == -1)
        return PodcastState(shows, downloads, automatic, notifications)
    }
}
