package com.ainalluna.michimusica.validation

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import android.widget.TextView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.ainalluna.michimusica.library.AudioCatalog
import com.ainalluna.michimusica.library.AudioSection
import com.ainalluna.michimusica.playback.*
import kotlinx.coroutines.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Synthetic audio and an injected clock only in this isolated APK; no personal media is touched. */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class SleepTimerRegressionActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var label: TextView
    private val report by lazy { File(filesDir, "sleep-report.txt") }
    private val catalog by lazy { AudioCatalog(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        label = TextView(this).apply { textSize = 20f; setPadding(32, 80, 32, 32) }
        setContentView(label)
        scope.launch {
            try { suite() } catch (failure: Throwable) { report.appendText("FAIL ${failure.stackTraceToString()}\n"); label.text = failure.message }
        }
    }
    private fun pass(text: String) { report.appendText("PASS $text\n"); label.text = text }
    private suspend fun until(predicate: () -> Boolean) { withTimeout(12000) { while (!predicate()) delay(25) } }
    private fun audio(name: String, seconds: Int): MediaItem {
        val file = File(File(filesDir, "regression-audio").apply { mkdirs() }, "$name.wav")
        val tree = android.provider.DocumentsContract.buildTreeDocumentUri("$packageName.documents", "root")
        val uri = android.provider.DocumentsContract.buildDocumentUriUsingTree(tree, file.name)
        val size = seconds * 16000 * 2
        file.writeBytes(ByteBuffer.allocate(size + 44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray()); putInt(size + 36); put("WAVEfmt ".toByteArray()); putInt(16)
            putShort(1); putShort(1); putInt(16000); putInt(32000); putShort(2); putShort(16)
            put("data".toByteArray()); putInt(size)
        }.array())
        catalog.classify(uri.toString(), AudioSection.PODCASTS)
        getSharedPreferences("michi_preferences", MODE_PRIVATE).edit().putString("music_folder_uri", tree.toString()).commit()
        return MediaItem.Builder().setMediaId(uri.toString()).setUri(uri)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(name).setArtist("Validación de temporizador")
                .setExtras(Bundle().apply { putLong("duration_ms", seconds * 1000L) }).build()).build()
    }
    private suspend fun suite() {
        report.writeText("")
        val long = audio("Podcast de prueba", 60)
        val first = audio("Episodio uno", 3)
        val second = audio("Episodio dos", 3)
        val local = ExoPlayer.Builder(this).build()
        var clock = 1000L; var saves = 0
        val timer = PlaybackSleepTimer(local, catalog::isPodcast, { saves++ }, { clock })
        try {
            local.setMediaItem(long); local.prepare(); until { local.playbackState == Player.STATE_READY }
            for (choice in listOf(SleepChoice.FIFTEEN, SleepChoice.THIRTY, SleepChoice.FORTY_FIVE)) {
                local.seekTo(0); local.play(); until { local.isPlaying }
                check(timer.choose(choice)); val oldSaves = saves
                clock += choice.minutes * 60000 - 1; timer.catalogChanged()
                check(local.playWhenReady && PodcastSleepTimer.state.value.remainingMs == 1L)
                clock++; timer.catalogChanged()
                check(!local.playWhenReady && !PodcastSleepTimer.state.value.active && saves == oldSaves + 1)
                pass("${choice.minutes} minute deadline: plays before, pauses and saves exactly at injected elapsed time")
            }
            timer.choose(SleepChoice.FIFTEEN); clock += 5000
            local.seekTo(45000); local.setPlaybackSpeed(2f); timer.catalogChanged()
            check(PodcastSleepTimer.state.value.remainingMs == 895000L)
            local.pause(); clock += 1000; timer.catalogChanged()
            check(PodcastSleepTimer.state.value.remainingMs == 894000L)
            timer.choose(SleepChoice.THIRTY); check(PodcastSleepTimer.state.value.remainingMs == 1800000L)
            timer.choose(SleepChoice.OFF); local.play(); clock += 3000000; timer.catalogChanged()
            check(local.playWhenReady && !PodcastSleepTimer.state.value.active)
            pass("seek, speed and pause do not reset clock; replacing and disabling timer work")
            timer.choose(SleepChoice.END); local.setMediaItem(first); check(!PodcastSleepTimer.state.value.active)
            timer.choose(SleepChoice.FIFTEEN); catalog.classify(first.mediaId, AudioSection.MUSIC); timer.catalogChanged()
            check(!PodcastSleepTimer.state.value.active && !timer.choose(SleepChoice.END))
            catalog.classify(first.mediaId, AudioSection.PODCASTS)
            pass("manual episode change cancels end timer; music classification cancels all timer modes")
        } finally { timer.release(); local.release() }

        val future = MediaController.Builder(this, SessionToken(this, ComponentName(this, PlaybackService::class.java))).buildAsync()
        val player = withContext(Dispatchers.IO) { future.get() }
        try {
            for (repeat in listOf(Player.REPEAT_MODE_OFF, Player.REPEAT_MODE_ONE)) {
                catalog.savePosition(first.mediaId, 0); catalog.savePosition(second.mediaId, 0)
                player.setMediaItems(listOf(first, second)); player.repeatMode = repeat; player.prepare()
                until { player.playbackState == Player.STATE_READY }
                check(PodcastSleepTimer.choose(SleepChoice.END)); player.play()
                until { !player.playWhenReady && !PodcastSleepTimer.state.value.active }
                check(player.currentMediaItem?.mediaId == first.mediaId) { "Advanced into next item at sleep boundary" }
                check(catalog.position(first.mediaId) >= 2800) { "Final podcast position was not saved" }
                pass("real playback service stops at episode end before next/repeat (repeat=$repeat)")
            }
            player.repeatMode = Player.REPEAT_MODE_OFF
            player.setMediaItems(listOf(long, first)); player.prepare(); until { player.playbackState == Player.STATE_READY }
            PodcastSleepTimer.choose(SleepChoice.END); player.seekToNextMediaItem()
            until { !PodcastSleepTimer.state.value.active }
            PodcastSleepTimer.choose(SleepChoice.FIFTEEN)
            catalog.classify(first.mediaId, AudioSection.MUSIC)
            until { !PodcastSleepTimer.state.value.active }
            catalog.classify(first.mediaId, AudioSection.PODCASTS)
            pass("service handles manual next and classification change")
            catalog.savePosition(first.mediaId, 0)
            player.setMediaItems(listOf(first, second)); player.prepare()
            until { player.playbackState == Player.STATE_READY }
            PodcastSleepTimer.choose(SleepChoice.END); player.play(); until { player.isPlaying }
            moveTaskToBack(true)
            until { !player.playWhenReady && !PodcastSleepTimer.state.value.active }
            check(player.currentMediaItem?.mediaId == first.mediaId)
            pass("service pauses before next episode with the Activity in background")
            player.setMediaItem(long); player.prepare(); until { player.playbackState == Player.STATE_READY }
            PodcastSleepTimer.choose(SleepChoice.FIFTEEN)
            // Leave an actual prepared podcast/timer for the production Compose UI review.
            getSharedPreferences("michi_preferences", MODE_PRIVATE).edit().putString("last_song_id", long.mediaId).commit()
            pass("SLEEP SUITE COMPLETE")
        } finally { player.release() }
    }
}
