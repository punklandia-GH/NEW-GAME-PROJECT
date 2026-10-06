package com.blockcrush.puzzle.achievements

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementsTest {

    @Test
    fun unlocksOnlyNewOnes() {
        val stats = PlayerStats(totalLines = 120, bestScore = 5000, maxCombo = 2, gamesPlayed = 10)
        val fresh = Achievements.newlyUnlocked(stats, emptySet()).map { it.key }
        assertEquals(
            listOf("first_clear", "lines_100", "score_1000", "score_5000", "games_10"),
            fresh
        )
        val later = Achievements.newlyUnlocked(stats, setOf("first_clear", "lines_100", "games_10")).map { it.key }
        assertEquals(listOf("score_1000", "score_5000"), later)
        assertTrue(Achievements.newlyUnlocked(PlayerStats(), emptySet()).isEmpty())
    }

    @Test
    fun progressIsCappedAtTheGoal() {
        val stats = PlayerStats(totalLines = 999)
        assertEquals(100, Achievement.LINES_100.progress(stats))
        assertEquals(500, Achievement.LINES_500.progress(stats))
        assertEquals(999, Achievement.LINES_2000.progress(stats))
        assertTrue(Achievement.values().map { it.key }.toSet().size == Achievement.values().size)
    }
}
