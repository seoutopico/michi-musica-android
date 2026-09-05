package com.ainalluna.michimusica.playback

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.content.edit
import androidx.media3.common.Player
import com.ainalluna.michimusica.library.AudioCatalog
import com.ainalluna.michimusica.library.episodeTransitionPosition
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var catalog: AudioCatalog
    private val handler = Handler(Looper.getMainLooper())
    private val checkpoint = object : Runnable {
        override fun run() { saveListening(); handler.postDelayed(this, 5_000) }
    }

    private fun saveListening() {
        val player = mediaSession?.player ?: return
        val item = player.currentMediaItem ?: return
        if (player.currentPosition <= 0 && !player.playWhenReady) return
        val position = if (player.playbackState == Player.STATE_ENDED && player.duration > 0) player.duration else player.currentPosition.coerceAtLeast(0)
        catalog.savePosition(item.mediaId, position)
        getSharedPreferences("michi_preferences", MODE_PRIVATE).edit {
            putString("last_song_id", item.mediaId)
            putLong("last_song_position", position)
            putString("last_playlist_uri", item.mediaMetadata.extras?.getString("playlist_uri"))
        }
    }

    override fun onCreate() {
        super.onCreate()
        catalog = AudioCatalog(this)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
        mediaSession = MediaSession.Builder(this, player).build()
        player.addListener(object : Player.Listener {
            @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
            override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                val oldId = oldPosition.mediaItem?.mediaId
                val newId = newPosition.mediaItem?.mediaId
                if (oldId != null && oldId != newId) catalog.savePosition(oldId, oldPosition.positionMs)
                // Also resume the item selected after deletion, without overriding explicit seeks.
                if (oldId != newId && newId != null && catalog.isPodcast(newId) &&
                    (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION || reason == Player.DISCONTINUITY_REASON_SEEK || reason == Player.DISCONTINUITY_REASON_REMOVE)) {
                    val target = episodeTransitionPosition(catalog.position(newId),
                        newPosition.mediaItem?.mediaMetadata?.extras?.getLong("duration_ms") ?: 0L, newPosition.positionMs)
                    if (target != null) handler.post {
                        if (player.currentMediaItem?.mediaId == newId) {
                            player.seekTo(target)
                        }
                    }
                }
            }
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (!playWhenReady) saveListening()
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) saveListening()
            }
        })
        handler.post(checkpoint)
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession.takeIf { controllerInfo.uid == android.os.Process.myUid() || controllerInfo.isTrusted }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player ?: return
        if (!player.playWhenReady || player.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        saveListening()
        handler.removeCallbacksAndMessages(null)
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
