package com.blockcrush.puzzle.daily

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class DailyChallengeTest {

    @Test
    fun challengesAreDeterministicAndWithinRange() {
        val seen = HashSet<GoalType>()
        for (day in 19000L..20500L) {
            val a = DailyChallenges.forDay(day)
            val b = DailyChallenges.forDay(day)
            assertEquals(a, b)
            assertTrue(a.seed >= 0)
            seen += a.type
            when (a.type) {
                GoalType.SCORE -> assertTrue(a.target in 1500..4500 && a.target % 500 == 0)
                GoalType.LINES -> assertTrue(a.target in 15..39)
                GoalType.COMBO -> assertTrue(a.target in 3..5)
            }
        }
        assertEquals(3, seen.size)
        assertTrue(DailyChallenges.forDay(20000).seed != DailyChallenges.forDay(20001).seed)
    }

    @Test
    fun completionUsesTheRightStatistic() {
        val score = Challenge(1, GoalType.SCORE, 2000, 1)
        assertTrue(DailyChallenges.isComplete(score, 2000, 0, 0))
        assertTrue(!DailyChallenges.isComplete(score, 1999, 99, 9))
        val lines = Challenge(1, GoalType.LINES, 20, 1)
        assertTrue(DailyChallenges.isComplete(lines, 0, 20, 0))
        val combo = Challenge(1, GoalType.COMBO, 4, 1)
        assertTrue(DailyChallenges.isComplete(combo, 0, 0, 4))
        assertEquals(7, DailyChallenges.progress(GoalType.LINES, 100, 7, 3))
    }

    @Test
    fun streakRules() {
        assertEquals(1, Streaks.afterCompletion(0, -1, 100))
        assertEquals(1, Streaks.afterCompletion(5, 90, 100))     // missed days: restart
        assertEquals(6, Streaks.afterCompletion(5, 99, 100))     // consecutive day
        assertEquals(5, Streaks.afterCompletion(5, 100, 100))    // already done today
        assertEquals(1, Streaks.afterCompletion(0, 100, 100))
        assertEquals(5, Streaks.displayed(5, 100, 100))
        assertEquals(5, Streaks.displayed(5, 99, 100))           // still alive today
        assertEquals(0, Streaks.displayed(5, 98, 100))           // broken
        assertEquals(0, Streaks.displayed(0, -1, 100))
    }

    @Test
    fun dayClockUsesLocalMidnight() {
        val utc = TimeZone.getTimeZone("UTC")
        val plus10 = TimeZone.getTimeZone("GMT+10:00")
        val minus5 = TimeZone.getTimeZone("GMT-05:00")
        val t = 1_700_000_000_000L // 2023-11-14 22:13:20 UTC
        assertEquals(19675L, DayClock.epochDay(t, utc))
        assertEquals(19676L, DayClock.epochDay(t, plus10))
        assertEquals(19675L, DayClock.epochDay(t, minus5))
        assertEquals(22L * 3600_000L + 13L * 60_000L + 20_000L, DayClock.millisIntoDay(t, utc))
        assertTrue(DayClock.millisIntoDay(t, plus10) < 24L * 3600_000L)
    }
}
