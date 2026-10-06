package com.blockcrush.puzzle.daily

import java.util.TimeZone

/** What today's challenge asks the player to do. */
enum class GoalType { SCORE, LINES, COMBO }

/**
 * One day's challenge. Every player gets the same challenge (and the same piece sequence,
 * through [seed]) on the same calendar day.
 */
data class Challenge(val epochDay: Long, val type: GoalType, val target: Int, val seed: Long)

object DailyChallenges {

    fun forDay(epochDay: Long): Challenge {
        val h = mix(epochDay)
        val type = GoalType.values()[((h ushr 8) % 3).toInt()]
        val variant = ((h ushr 16) % 7).toInt()
        val target = when (type) {
            GoalType.SCORE -> 1500 + variant * 500      // 1,500 .. 4,500 points
            GoalType.LINES -> 15 + variant * 4          // 15 .. 39 lines
            GoalType.COMBO -> 3 + (variant % 3)         // combo x3 .. x5
        }
        return Challenge(epochDay, type, target, h)
    }

    fun progress(type: GoalType, score: Int, linesCleared: Int, maxCombo: Int): Int = when (type) {
        GoalType.SCORE -> score
        GoalType.LINES -> linesCleared
        GoalType.COMBO -> maxCombo
    }

    fun isComplete(challenge: Challenge, score: Int, linesCleared: Int, maxCombo: Int): Boolean =
        progress(challenge.type, score, linesCleared, maxCombo) >= challenge.target

    /** A fast, well-mixed 64-bit hash (SplitMix64 finaliser); always non-negative. */
    private fun mix(input: Long): Long {
        var z = input + -7046029254386353131L
        z = (z xor (z ushr 30)) * -4658895280553007687L
        z = (z xor (z ushr 27)) * -7723592293110705685L
        z = z xor (z ushr 31)
        return z and Long.MAX_VALUE
    }
}

/** Daily streak rules. Days are counted as local calendar days (see [DayClock]). */
object Streaks {

    /** The streak to store after completing the challenge on [today]. */
    fun afterCompletion(storedStreak: Int, lastCompletedDay: Long, today: Long): Int = when {
        lastCompletedDay == today -> maxOf(storedStreak, 1)
        lastCompletedDay == today - 1 -> storedStreak + 1
        else -> 1
    }

    /** The streak to show: it survives until the end of the day after the last completion. */
    fun displayed(storedStreak: Int, lastCompletedDay: Long, today: Long): Int =
        if (lastCompletedDay >= 0 && lastCompletedDay >= today - 1) storedStreak else 0
}

/** Converts wall-clock time into a local calendar day number. */
object DayClock {
    private const val DAY_MILLIS = 86_400_000L

    fun epochDay(nowMillis: Long = System.currentTimeMillis(), zone: TimeZone = TimeZone.getDefault()): Long =
        Math.floorDiv(nowMillis + zone.getOffset(nowMillis), DAY_MILLIS)

    /** Milliseconds since midnight (local time) for the given instant. */
    fun millisIntoDay(nowMillis: Long = System.currentTimeMillis(), zone: TimeZone = TimeZone.getDefault()): Long =
        Math.floorMod(nowMillis + zone.getOffset(nowMillis), DAY_MILLIS)
}
