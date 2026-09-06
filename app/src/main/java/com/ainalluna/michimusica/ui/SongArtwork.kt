package com.ainalluna.michimusica.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.ainalluna.michimusica.library.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/** Read-only, sampled covers. Never downloads an image or modifies the audio. */
private object EmbeddedArtwork {
    private data class Cover(val bitmap: Bitmap?)
    private val cache = object : LruCache<String, Cover>(8 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Cover) = value.bitmap?.allocationByteCount ?: 256
    }
    private val readers = Semaphore(2)

    suspend fun load(context: Context, uri: Uri, revision: Int, maxDimension: Int): Bitmap? = withContext(Dispatchers.IO) {
        val dimension = maxDimension.coerceIn(160, 1024)
        val key = "$uri#$revision#$dimension"
        cache.get(key)?.let { return@withContext it.bitmap }
        readers.withPermit {
            cache.get(key)?.let { return@withPermit it.bitmap }
            val bitmap = runCatching {
                val retriever = MediaMetadataRetriever()
                val bytes = try {
                    retriever.setDataSource(context, uri)
                    retriever.embeddedPicture
                } finally { retriever.release() }
                if (bytes == null || bytes.size > 12 * 1024 * 1024) return@runCatching null
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / sample > dimension) sample *= 2
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            }.getOrNull() ?: com.ainalluna.michimusica.podcasts.PodcastRepository.get(context).state.value.downloads
                .firstOrNull { it.status == "done" && it.uri == uri.toString() }?.let {
                    com.ainalluna.michimusica.podcasts.PodcastArtworkCache.cached(context, it.image)
                }
            cache.put(key, Cover(bitmap))
            bitmap
        }
    }
}

@Composable
fun SongArtwork(song: Song, modifier: Modifier, revision: Int = 0, maxDimension: Int = 320, largePlaceholder: Boolean = false) {
    val context = LocalContext.current.applicationContext
    val inspecting = LocalInspectionMode.current
    val bitmap by produceState<Bitmap?>(null, song.uri, revision, inspecting, maxDimension) {
        value = null
        if (!inspecting) value = EmbeddedArtwork.load(context, song.uri, revision, maxDimension)
    }
    Box(modifier.clip(RoundedCornerShape(5.dp)), contentAlignment = Alignment.Center) {
        val cover = bitmap
        if (cover != null) Image(cover.asImageBitmap(), contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else {
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(
                MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surfaceContainerHigh,
            ))), contentAlignment = Alignment.Center) { HomeCat(Modifier.size(if (largePlaceholder) 80.dp else 30.dp)) }
        }
    }
}
