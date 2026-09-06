package com.ainalluna.michimusica.validation

import android.app.Activity
import android.app.NotificationManager
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.TextView
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.ainalluna.michimusica.library.AudioCatalog
import com.ainalluna.michimusica.podcasts.*
import kotlinx.coroutines.*
import java.io.File

/** Native integration entrypoint available exclusively in the isolated validation APK. */
class PodcastRegressionActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val repo by lazy { PodcastRepository.get(this) }
    private val tree by lazy { DocumentsContract.buildTreeDocumentUri("$packageName.documents", "root") }
    private val report by lazy { File(filesDir, "regression-report.txt") }
    private lateinit var label: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        label = TextView(this).apply { textSize = 20f; text = "Validación aislada de Podcasts"; setPadding(32, 80, 32, 32) }
        setContentView(label)
        RegressionNetwork.install()
        scope.launch {
            try {
                withContext(Dispatchers.IO) { repo.load() }
                when (intent.getStringExtra("phase") ?: "suite") {
                    "suite" -> suite()
                    "interrupt" -> interrupt()
                    "recover" -> recover()
                    "news-seed" -> newsSeed()
                    "news-check" -> newsCheck()
                    "news-repeat" -> newsRepeat()
                }
            } catch (failure: Throwable) {
                report.appendText("FAIL ${failure.stackTraceToString()}\n")
                label.text = "FALLO: ${failure.message}"
            }
        }
    }
    private fun pass(name: String) { report.appendText("PASS $name\n"); label.text = name }
    private suspend fun until(label: String, predicate: () -> Boolean) {
        withTimeout(20000) { while (!predicate()) delay(40) }
        check(predicate()) { label }
    }
    private fun receipt(id: Int) = repo.state.value.downloads.lastOrNull { it.episodeId == podcastId("episode-$id") }
    private suspend fun start(id: Int) {
        val show = repo.state.value.shows.single()
        PodcastDownloadService.request(this, show, show.episodes.single { it.id == podcastId("episode-$id") }, tree)
    }
    private suspend fun status(id: Int, value: String) { until("episode $id -> $value") { receipt(id)?.status == value } }
    private fun noPartial() { check(File(filesDir, "regression-audio").listFiles().orEmpty().none { it.extension == "part" }); check(cacheDir.listFiles().orEmpty().none { it.name.startsWith("podcast-") && it.extension == "part" }) }
    private fun verified(id: Int) {
        val d = requireNotNull(receipt(id)); check(d.status == "done")
        check(AudioCatalog(this).isPodcast(d.uri))
        check(contentResolver.openInputStream(d.uri.toUri())!!.use { it.readBytes() }.contentEquals(RegressionNetwork.audio))
    }
    private suspend fun suite() {
        report.writeText("")
        if (repo.state.value.shows.isEmpty()) repo.follow(RegressionNetwork.FEED)
        check(repo.state.value.shows.single().episodes.size == 8)
        check(repo.state.value.shows.single().episodes.none { it.isNew })
        pass("follow without marking historical entries new")

        RegressionNetwork.slow = true
        start(1); status(1, "downloading")
        until("bytes received") { cacheDir.listFiles().orEmpty().any { it.extension == "part" && it.length() > 0 } }
        PodcastDownloadService.cancel(this, receipt(1)!!.key)
        status(1, "cancelled"); delay(200); noPartial()
        pass("cancel active transfer cleans temporary files")
        RegressionNetwork.slow = false
        start(1); status(1, "done"); verified(1)
        pass("retry cancelled transfer saves exact WAV through SAF and classifies podcast")

        PodcastDownloadService.cancel(this, receipt(1)!!.key); delay(500)
        check(receipt(1)?.status == "done") { "Late cancellation downgraded a completed download" }
        pass("late cancellation preserves completed receipt")

        RegressionDocuments.pauseRename = true
        start(8)
        until("commit reached") { RegressionDocuments.renameEntered }
        PodcastDownloadService.cancel(this, receipt(8)!!.key)
        status(8, "done"); delay(150); verified(8); noPartial()
        RegressionDocuments.pauseRename = false
        pass("cancellation during commit preserves finalized file and receipt")

        RegressionNetwork.slow = true
        start(2); status(2, "downloading")
        start(3); status(3, "queued")
        PodcastDownloadService.cancel(this, receipt(3)!!.key); status(3, "cancelled")
        status(2, "done"); verified(2)
        check(receipt(3)?.uri.isNullOrEmpty()); noPartial()
        pass("cancel queued transfer leaves active transfer intact")
        RegressionNetwork.slow = false

        RegressionNetwork.broken = true
        start(4); status(4, "error"); delay(150); noPartial()
        check(receipt(4)!!.error.contains("conexión")) { "Network failure message: ${receipt(4)!!.error}" }
        RegressionNetwork.broken = false
        start(4); status(4, "done"); verified(4)
        pass("connection interrupted mid-body reports error and retry succeeds")

        RegressionDocuments.deny = true
        start(5); status(5, "error"); delay(150); noPartial()
        check(receipt(5)!!.uri.isEmpty())
        RegressionDocuments.deny = false
        start(5); status(5, "done"); verified(5)
        pass("SAF denied access never exposes partial audio and can retry")

        RegressionDocuments.full = true
        start(6); status(6, "error"); delay(150); noPartial()
        check(receipt(6)!!.uri.isEmpty())
        RegressionDocuments.full = false
        start(6); status(6, "done"); verified(6)
        pass("provider create failure for full storage can retry without partial audio")
        pass("SUITE COMPLETE")
    }
    private suspend fun interrupt() {
        RegressionNetwork.slow = true
        start(7); status(7, "downloading")
        until("partial before kill") { cacheDir.listFiles().orEmpty().any { it.extension == "part" && it.length() > 0 } }
        pass("READY FOR PROCESS KILL")
    }
    private suspend fun recover() {
        check(receipt(7)?.status == "error") { "Interrupted receipt was not recovered as retryable" }
        check(receipt(7)!!.error.contains("interrumpió"))
        start(7); status(7, "done"); verified(7); noPartial()
        pass("killed process recovers interruption and retry succeeds")
        pass("RECOVERY COMPLETE")
    }
    private suspend fun newsSeed() {
        getSharedPreferences("michi_preferences", MODE_PRIVATE).edit().putString("music_folder_uri", tree.toString()).commit()
        check(repo.state.value.shows.single().episodes.size == 8)
        repo.settings(automatic = true, notifications = true)
        RegressionNetwork.newEpisode = true
        pass("READY FOR SCHEDULED REFRESH")
    }
    private suspend fun newsCheck() {
        until("news notification") { getSystemService(NotificationManager::class.java).activeNotifications.any { it.id == 1202 } }
        val notices = getSystemService(NotificationManager::class.java).activeNotifications
        val notice = notices.single { it.id == 1202 }
        check(notice.notification.extras.getCharSequence("android.text").toString() == "1 episodio nuevo")
        check(repo.state.value.shows.single().episodes.count { it.isNew } == 1)
        pass("scheduled refresh posts notification for exactly one new episode")
        notice.notification.contentIntent.send()
        pass("NEWS INTENT SENT")
    }
    private suspend fun newsRepeat() {
        RegressionNetwork.newEpisode = true
        check(repo.refresh() == 0)
        repo.markSeen()
        check(repo.state.value.shows.single().episodes.none { it.isNew })
        check(repo.refresh() == 0)
        repo.settings(automatic = false, notifications = false)
        check(getSystemService(android.app.job.JobScheduler::class.java).getPendingJob(1200) == null)
        pass("repeat refresh does not duplicate news; mark seen and disable job work")
        pass("NEWS COMPLETE")
    }
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
