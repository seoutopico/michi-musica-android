package com.ainalluna.michimusica.podcasts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.ainalluna.michimusica.ui.HomeCat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File

internal object PodcastArtworkCache {
    private val gate = Semaphore(2)
    fun cached(context: Context, url: String): Bitmap? = if (url.isBlank()) null else runCatching {
        BitmapFactory.decodeFile(File(context.cacheDir, "podcast-covers/${podcastId(url)}.jpg").absolutePath)
    }.getOrNull()
    suspend fun prefetch(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext null
        gate.withPermit {
            runCatching {
                val directory = File(context.cacheDir, "podcast-covers").apply { mkdirs() }
                val file = File(directory, "${podcastId(url)}.jpg")
                if (file.exists()) return@runCatching BitmapFactory.decodeFile(file.absolutePath)
                val data = PodcastNetwork.bytes(url, 3 * 1024 * 1024)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(data, 0, data.size, bounds)
                require(bounds.outWidth in 1..16000 && bounds.outHeight in 1..16000)
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 320) sample *= 2
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, BitmapFactory.Options().apply { inSampleSize = sample })
                    ?: return@runCatching null
                file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
                directory.listFiles()?.sortedByDescending { it.lastModified() }?.drop(150)?.forEach { it.delete() }
                bitmap
            }.getOrNull()
        }
    }
}

@Composable
fun PodcastArtwork(url: String, modifier: Modifier) {
    val context = LocalContext.current.applicationContext
    val inspection = LocalInspectionMode.current
    val bitmap by produceState<Bitmap?>(null, url, inspection) { if (!inspection) value = PodcastArtworkCache.prefetch(context, url) }
    Box(modifier.clip(RoundedCornerShape(5.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
        bitmap?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            ?: HomeCat(Modifier.size(30.dp))
    }
}
