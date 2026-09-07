package com.ainalluna.michimusica.playback

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SleepChoice(val label: String, val minutes: Int = 0) {
    OFF("Desactivado"), END("Fin del episodio"), FIFTEEN("15 minutos", 15),
    THIRTY("30 minutos", 30), FORTY_FIVE("45 minutos", 45),
}

data class SleepPlan(val choice: SleepChoice = SleepChoice.OFF, val deadline: Long = 0, val episodeId: String = "") {
    val active get() = choice != SleepChoice.OFF
    fun remaining(now: Long): Long = (deadline - now).coerceAtLeast(0)
    fun expired(now: Long): Boolean = choice.minutes > 0 && now >= deadline
    fun cancelFor(id: String?, podcast: Boolean): Boolean = active &&
        (!podcast || id == null || (choice == SleepChoice.END && id != episodeId))

    companion object {
        fun start(choice: SleepChoice, episodeId: String, now: Long): SleepPlan {
            if (choice == SleepChoice.OFF) return SleepPlan()
            require(episodeId.isNotBlank())
            return SleepPlan(choice, if (choice.minutes > 0) now + choice.minutes * 60_000L else 0L, episodeId)
        }
    }
}

data class SleepTimerDisplay(val choice: SleepChoice = SleepChoice.OFF, val remainingMs: Long = 0) {
    val active get() = choice != SleepChoice.OFF
    val label: String get() = when (choice) {
        SleepChoice.OFF -> "Temporizador"
        SleepChoice.END -> "Fin del episodio"
        else -> ((remainingMs + 999) / 1000).let { "${it / 60}:${(it % 60).toString().padStart(2, '0')}" }
    }
}

/** Same-process UI bridge; the playback service owns the clock and every player mutation. */
object PodcastSleepTimer {
    private val mutable = MutableStateFlow(SleepTimerDisplay())
    val state = mutable.asStateFlow()
    internal var select: ((SleepChoice) -> Boolean)? = null
    fun choose(choice: SleepChoice): Boolean = select?.invoke(choice) ?: false
    internal fun publish(plan: SleepPlan, now: Long) {
        mutable.value = SleepTimerDisplay(plan.choice, if (plan.choice.minutes > 0) plan.remaining(now) else 0)
    }
}
