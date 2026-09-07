package com.ainalluna.michimusica

import com.ainalluna.michimusica.playback.*
import org.junit.Assert.*
import org.junit.Test

class PodcastSleepTimerTest {
    @Test fun presetsExpireAtExactElapsedDeadline() {
        for (choice in listOf(SleepChoice.FIFTEEN, SleepChoice.THIRTY, SleepChoice.FORTY_FIVE)) {
            val timer = SleepPlan.start(choice, "episode", 1000)
            val deadline = 1000 + choice.minutes * 60_000L
            assertEquals(deadline, timer.deadline)
            assertFalse(timer.expired(deadline - 1)); assertTrue(timer.expired(deadline))
            assertEquals(0L, timer.remaining(deadline + 100))
        }
    }
    @Test fun endIsTiedToChosenEpisodeAndHasNoTimeDeadline() {
        val timer = SleepPlan.start(SleepChoice.END, "one", 0)
        assertFalse(timer.expired(Long.MAX_VALUE))
        assertFalse(timer.cancelFor("one", true))
        assertTrue(timer.cancelFor("two", true)); assertTrue(timer.cancelFor("one", false))
        assertTrue(timer.cancelFor(null, false))
    }
    @Test fun minutesContinueAcrossPodcastEpisodesAndCancelForMusic() {
        val timer = SleepPlan.start(SleepChoice.THIRTY, "one", 0)
        assertFalse(timer.cancelFor("two", true)); assertTrue(timer.cancelFor("music", false))
        assertTrue(timer.cancelFor(null, false))
    }
    @Test fun cancellationAndReplacementDoNotReuseDeadline() {
        assertFalse(SleepPlan.start(SleepChoice.OFF, "", 0).active)
        val timer = SleepPlan.start(SleepChoice.FIFTEEN, "one", 300_000)
        assertEquals(1_200_000L, timer.deadline)
        assertFalse(SleepPlan().expired(Long.MAX_VALUE))
    }
    @Test fun countdownRoundsUpToAvoidPrematureZero() {
        assertEquals("15:00", SleepTimerDisplay(SleepChoice.FIFTEEN, 900_000).label)
        assertEquals("0:01", SleepTimerDisplay(SleepChoice.FIFTEEN, 1).label)
        assertEquals("0:00", SleepTimerDisplay(SleepChoice.FIFTEEN, 0).label)
        assertEquals("Fin del episodio", SleepTimerDisplay(SleepChoice.END).label)
    }
}
