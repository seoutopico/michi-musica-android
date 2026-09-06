package com.ainalluna.michimusica.podcasts

import android.app.*
import android.app.job.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.ainalluna.michimusica.MainActivity
import com.ainalluna.michimusica.R
import kotlinx.coroutines.*

class PodcastRefreshService : JobService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var work: Job? = null
    override fun onStartJob(params: JobParameters): Boolean {
        work = scope.launch {
            val repo = PodcastRepository.get(this@PodcastRefreshService)
            try {
                withContext(Dispatchers.IO) { repo.load() }
                if (repo.state.value.automatic) {
                    val count = repo.refresh()
                    if (count > 0 && repo.state.value.notifications) notifyNew(count)
                }
                jobFinished(params, false)
            } catch (cancel: CancellationException) { throw cancel }
            catch (_: Exception) { jobFinished(params, true) }
        }
        return true
    }
    override fun onStopJob(params: JobParameters): Boolean { work?.cancel(); return true }
    override fun onDestroy() { scope.cancel(); super.onDestroy() }

    private fun notifyNew(count: Int) {
        val manager = getSystemService(NotificationManager::class.java)
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) return
        manager.createNotificationChannel(NotificationChannel("podcast_news", "Nuevos episodios", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(this, 13, Intent(this, MainActivity::class.java).putExtra("podcast_news", true), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(this, "podcast_news").setSmallIcon(R.drawable.ic_home_library_music)
            .setContentTitle("Novedades en tus podcasts").setContentText("$count ${if (count == 1) "episodio nuevo" else "episodios nuevos"}")
            .setAutoCancel(true).setContentIntent(open).build()
        try { manager.notify(1202, notification) } catch (_: SecurityException) { /* Permission can change between check and notification. */ }
    }

    companion object {
        private const val JOB = 1200
        fun schedule(context: Context) {
            val state = PodcastRepository.get(context).state.value
            val scheduler = context.getSystemService(JobScheduler::class.java)
            if (!state.automatic || state.shows.isEmpty()) { scheduler.cancel(JOB); return }
            if (scheduler.getPendingJob(JOB) != null) return
            check(scheduler.schedule(JobInfo.Builder(JOB, ComponentName(context, PodcastRefreshService::class.java))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPeriodic(6 * 60 * 60 * 1000L)
                .setPersisted(true).build()) == JobScheduler.RESULT_SUCCESS) { "Android no pudo programar la comprobación automática." }
        }
    }
}
