package com.ainalluna.michimusica.podcasts

import android.content.Context
import android.util.AtomicFile
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class PodcastRepository private constructor(private val context: Context) {
    private val file = AtomicFile(File(context.filesDir, "podcasts.bin"))
    private val mutable = MutableStateFlow(PodcastState())
    val state = mutable.asStateFlow()
    private var loaded = false
    private val refreshLock = Mutex()
    val refreshing = MutableStateFlow(false)
    val progress = MutableStateFlow<Map<String, Int>>(emptyMap())

    @Synchronized fun load() {
        if (loaded) return
        val saved = if (file.baseFile.exists() || File(file.baseFile.path + ".bak").exists()) file.openRead().use(PodcastStore::read) else PodcastState()
        // A killed process never turns an unfinished document into a playable episode.
        mutable.value = saved.copy(downloads = saved.downloads.map {
            if (it.status in listOf("queued", "downloading")) {
                val document = if (it.uri.isNotBlank()) androidx.documentfile.provider.DocumentFile.fromSingleUri(context, it.uri.toUri()) else null
                if (document?.isFile == true && com.ainalluna.michimusica.library.MusicFolderReader.isSupportedAudio(document.name.orEmpty())) {
                    com.ainalluna.michimusica.library.AudioCatalog(context).classify(document.uri.toString(), com.ainalluna.michimusica.library.AudioSection.PODCASTS)
                    it.copy(status = "done", error = "")
                } else it.copy(status = "error", error = "La descarga se interrumpió. Pulsa Reintentar.")
            } else it
        })
        // A process kill can leave private extraction files. No transfer can exist before first load.
        context.cacheDir.listFiles().orEmpty().filter {
            Regex("podcast-youtube-[0-9a-f-]{36}|podcast-[0-9a-f]{64}\\.part").matches(it.name)
        }.forEach { runCatching { it.deleteRecursively() } }
        loaded = true
    }

    @Synchronized internal fun update(transform: (PodcastState) -> PodcastState) {
        load()
        val next = transform(mutable.value)
        val output = file.startWrite()
        try { PodcastStore.write(next, output); file.finishWrite(output) }
        catch (failure: Exception) { file.failWrite(output); throw failure }
        mutable.value = next
    }

    suspend fun follow(raw: String) = withContext(Dispatchers.IO) {
        load()
        val url = publicFeedUrl(raw)
        require(state.value.shows.none { it.url == url }) { "Ya sigues este podcast." }
        val fetched = podcastSource(url)
        require(fetched.episodes.isNotEmpty()) { "No hay episodios públicos gratuitos compatibles en este enlace." }
        update { old ->
            require(old.shows.size < 100) { "Puedes seguir hasta 100 podcasts." }
            require(old.shows.none { it.url == fetched.url }) { "Ya sigues este podcast." }
            old.copy(shows = old.shows + mergePodcast(null, fetched, System.currentTimeMillis()))
        }
        PodcastRefreshService.schedule(context)
    }

    suspend fun refresh(): Int = refreshLock.withLock {
        withContext(Dispatchers.IO) {
            load(); refreshing.value = true
            var added = 0
            try {
                for (show in state.value.shows) {
                    kotlinx.coroutines.currentCoroutineContext().ensureActivePodcast()
                    try {
                        val fetched = podcastSource(show.url)
                        update { old ->
                            old.copy(shows = old.shows.map {
                                if (it.url != show.url) it else mergePodcast(it, fetched, System.currentTimeMillis()).also { merged ->
                                    added += merged.episodes.count { e -> e.id !in it.seen && e.isNew }
                                }
                            })
                        }
                    } catch (failure: Exception) {
                        if (failure is kotlinx.coroutines.CancellationException) throw failure
                        update { old -> old.copy(shows = old.shows.map { if (it.url == show.url) it.copy(error = podcastError(failure)) else it }) }
                    }
                }
            } finally { refreshing.value = false }
            added
        }
    }

    suspend fun unfollow(url: String) = withContext(Dispatchers.IO) {
        update { it.copy(shows = it.shows.filterNot { s -> s.url == url }) }
        PodcastRefreshService.schedule(context)
    }

    suspend fun markSeen(url: String? = null) = withContext(Dispatchers.IO) {
        update { it.copy(shows = it.shows.map { s -> if (url == null || s.url == url) s.copy(episodes = s.episodes.map { e -> e.copy(isNew = false) }) else s }) }
    }

    suspend fun settings(automatic: Boolean, notifications: Boolean) = withContext(Dispatchers.IO) {
        update { it.copy(automatic = automatic, notifications = notifications) }
        PodcastRefreshService.schedule(context)
    }

    internal fun downloadUpdate(key: String, transform: (PodcastDownload) -> PodcastDownload) =
        update { it.copy(downloads = it.downloads.map { d -> if (d.key == key) transform(d) else d }) }

    fun decorate(songs: List<com.ainalluna.michimusica.library.Song>): List<com.ainalluna.michimusica.library.Song> {
        val metadata = state.value.downloads.filter { it.status in listOf("done", "music") }.associateBy { it.uri }
        return songs.map { song -> metadata[song.id]?.let { song.copy(title = it.title, artist = it.author) } ?: song }
    }

    companion object {
        // The constructor receives applicationContext only; no Activity or View is retained.
        @android.annotation.SuppressLint("StaticFieldLeak")
        @Volatile private var instance: PodcastRepository? = null
        fun get(context: Context): PodcastRepository = instance ?: synchronized(this) {
            instance ?: PodcastRepository(context.applicationContext).also { instance = it }
        }
    }
}

private fun kotlin.coroutines.CoroutineContext.ensureActivePodcast() {
    if (this[kotlinx.coroutines.Job]?.isActive == false) throw kotlinx.coroutines.CancellationException()
}
