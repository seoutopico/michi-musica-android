package com.ainalluna.michimusica.youtube

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.ainalluna.michimusica.ui.HomeCat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/** Only the public YouTube thumbnail for a validated result ID; never local audio. */
private object SearchThumbnails {
    private val readers = Semaphore(3)
    private val cache = object : LruCache<String, Bitmap>(4 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount
    }
    suspend fun load(id: String): Bitmap? = withContext(Dispatchers.IO) {
        if (!Regex("^[A-Za-z0-9_-]{11}$").matches(id)) return@withContext null
        cache.get(id)?.let { return@withContext it }
        readers.withPermit {
            cache.get(id)?.let { return@withPermit it }
            runCatching {
                val connection = URL("https://i.ytimg.com/vi/$id/mqdefault.jpg").openConnection() as HttpURLConnection
                val bytes = try {
                    connection.connectTimeout = 8_000; connection.readTimeout = 8_000
                    connection.instanceFollowRedirects = false
                    if (connection.responseCode != 200) return@runCatching null
                    connection.inputStream.use { input ->
                        val output = java.io.ByteArrayOutputStream()
                        val buffer = ByteArray(8_192)
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            if (output.size() + count > 1_048_576) return@runCatching null
                            output.write(buffer, 0, count)
                        }
                        output.toByteArray()
                    }
                } finally { connection.disconnect() }
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 480) sample *= 2
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })?.also { cache.put(id, it) }
            }.getOrNull()
        }
    }
}

@Composable
fun SearchThumbnail(result: YouTubeResult, modifier: Modifier = Modifier) {
    val inspecting = LocalInspectionMode.current
    val bitmap by produceState<Bitmap?>(null, result.id, inspecting) {
        value = null
        if (!inspecting) value = SearchThumbnails.load(result.id)
    }
    Box(modifier.clip(RoundedCornerShape(5.dp)).background(Brush.linearGradient(listOf(
        MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surfaceContainerHigh))), contentAlignment = Alignment.Center) {
        val cover = bitmap
        if (cover == null) HomeCat(Modifier.size(28.dp))
        else Image(cover.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}
