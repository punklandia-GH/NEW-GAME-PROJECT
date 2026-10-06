package com.blockcrush.puzzle.logic

/**
 * Scoring:
 *  - 1 point per block placed.
 *  - Line clears: 10 x lines x lines, so 1 line = 10, 2 lines = 40, 3 lines = 90, 4 lines = 160 ...
 *  - The line points are multiplied by the combo (consecutive placements that cleared lines).
 *  - Clearing the entire board adds a flat bonus.
 */
class ScoreRules(
    val pointsPerBlock: Int = 1,
    val lineBase: Int = 10,
    val boardClearBonus: Int = 300
) {
    fun placementPoints(blocks: Int): Int = blocks * pointsPerBlock

    fun comboMultiplier(combo: Int): Int = if (combo < 1) 1 else combo

    fun lineClearPoints(lines: Int, combo: Int): Int =
        if (lines <= 0) 0 else lineBase * lines * lines * comboMultiplier(combo)
}

/** Encouraging words shown after a clear. Bigger clears and longer combos earn bigger words. */
enum class Praise(val level: Int) {
    NONE(0), NICE(1), GREAT(2), AMAZING(3), FANTASTIC(4), UNBELIEVABLE(5);

    companion object {
        fun forMove(lines: Int, combo: Int): Praise {
            if (lines <= 0) return NONE
            val byLines = when (lines) {
                1 -> NICE
                2 -> GREAT
                3 -> AMAZING
                4 -> FANTASTIC
                else -> UNBELIEVABLE
            }
            val byCombo = when {
                combo >= 8 -> UNBELIEVABLE
                combo >= 6 -> FANTASTIC
                combo >= 4 -> AMAZING
                combo >= 2 -> GREAT
                else -> NICE
            }
            return if (byLines.level >= byCombo.level) byLines else byCombo
        }
    }
}
