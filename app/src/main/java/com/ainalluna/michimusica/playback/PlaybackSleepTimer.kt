package com.ainalluna.michimusica.playback

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/** Monotonic real time, independent of Activity, seeking, playback speed and the wall clock. */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
internal class PlaybackSleepTimer(
    private val player: ExoPlayer,
    private val isPodcast: (String) -> Boolean,
    private val save: () -> Unit,
    private val now: () -> Long = SystemClock::elapsedRealtime,
) : Player.Listener {
    private val handler = Handler(Looper.getMainLooper())
    private var plan = SleepPlan()
    private val tick = Runnable { check() }
    init {
        player.addListener(this)
        PodcastSleepTimer.select = ::choose
        publish()
    }

    fun choose(choice: SleepChoice): Boolean {
        check(Looper.myLooper() == player.applicationLooper)
        val id = player.currentMediaItem?.mediaId
        if (choice != SleepChoice.OFF && (id == null || !isPodcast(id))) return false
        plan = SleepPlan.start(choice, id.orEmpty(), now())
        player.pauseAtEndOfMediaItems = choice == SleepChoice.END
        check()
        return true
    }

    private fun publish() = PodcastSleepTimer.publish(plan, now())
    private fun clear() {
        plan = SleepPlan()
        handler.removeCallbacks(tick)
        player.pauseAtEndOfMediaItems = false
        publish()
    }
    private fun finish() {
        clear()
        player.pause()
        save()
    }
    fun catalogChanged() = check()
    private fun check() {
        handler.removeCallbacks(tick)
        val id = player.currentMediaItem?.mediaId
        if (plan.cancelFor(id, id != null && isPodcast(id))) clear()
        else if (plan.expired(now())) finish()
        else {
            publish()
            if (plan.choice.minutes > 0) handler.postDelayed(tick, minOf(1000L, plan.remaining(now()).coerceAtLeast(1)))
        }
    }
    override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) = check()
    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        if (!playWhenReady && reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM && plan.choice == SleepChoice.END) {
            save(); clear()
        } else check()
    }
    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_ENDED && plan.active) { save(); clear() }
    }
    fun release() {
        clear()
        player.removeListener(this)
        PodcastSleepTimer.select = null
    }
}
