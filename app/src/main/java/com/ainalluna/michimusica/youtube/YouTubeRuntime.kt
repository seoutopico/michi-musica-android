package com.ainalluna.michimusica.youtube

import android.content.Context
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val UPDATE_INTERVAL_MS = 24L * 60L * 60L * 1_000L
private const val PREFS_NAME = "michi_youtube_runtime"
private const val LAST_UPDATE_CHECK_KEY = "last_successful_update_check"

internal fun shouldUpdateYoutubeDl(lastSuccessfulCheck: Long, now: Long): Boolean =
    lastSuccessfulCheck <= 0L || now < lastSuccessfulCheck || now - lastSuccessfulCheck >= UPDATE_INTERVAL_MS

/** Prepares yt-dlp and keeps its YouTube extractors current without checking on every action. */
internal object YouTubeRuntime {
    private val updateMutex = Mutex()

    fun initialize(context: Context) {
        YoutubeDL.getInstance().init(context.applicationContext)
    }

    suspend fun ensureCurrent(context: Context) {
        val appContext = context.applicationContext
        initialize(appContext)
        updateMutex.withLock {
            val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            if (!shouldUpdateYoutubeDl(preferences.getLong(LAST_UPDATE_CHECK_KEY, 0L), now)) return

            // YouTube changes frequently; yt-dlp recommends nightly builds for timely extractor fixes.
            YoutubeDL.getInstance().updateYoutubeDL(appContext, YoutubeDL.UpdateChannel.NIGHTLY)
            preferences.edit().putLong(LAST_UPDATE_CHECK_KEY, now).apply()
        }
    }
}
