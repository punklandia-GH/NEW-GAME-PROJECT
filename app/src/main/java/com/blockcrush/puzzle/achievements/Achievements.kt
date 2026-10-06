package com.blockcrush.puzzle.achievements

/** The lifetime statistics that achievements are measured against. */
data class PlayerStats(
    val totalLines: Int = 0,
    val bestScore: Int = 0,
    val maxCombo: Int = 0,
    val boardClears: Int = 0,
    val gamesPlayed: Int = 0,
    val bestStreak: Int = 0,
    val totalPieces: Int = 0
) {
    fun value(type: StatType): Int = when (type) {
        StatType.TOTAL_LINES -> totalLines
        StatType.BEST_SCORE -> bestScore
        StatType.MAX_COMBO -> maxCombo
        StatType.BOARD_CLEARS -> boardClears
        StatType.GAMES_PLAYED -> gamesPlayed
        StatType.BEST_STREAK -> bestStreak
        StatType.TOTAL_PIECES -> totalPieces
    }
}

enum class StatType { TOTAL_LINES, BEST_SCORE, MAX_COMBO, BOARD_CLEARS, GAMES_PLAYED, BEST_STREAK, TOTAL_PIECES }

/** Simple milestones. Titles and descriptions live in strings.xml (see AchievementsActivity). */
enum class Achievement(val key: String, val stat: StatType, val goal: Int) {
    FIRST_CLEAR("first_clear", StatType.TOTAL_LINES, 1),
    LINES_100("lines_100", StatType.TOTAL_LINES, 100),
    LINES_500("lines_500", StatType.TOTAL_LINES, 500),
    LINES_2000("lines_2000", StatType.TOTAL_LINES, 2000),
    SCORE_1000("score_1000", StatType.BEST_SCORE, 1000),
    SCORE_5000("score_5000", StatType.BEST_SCORE, 5000),
    SCORE_10000("score_10000", StatType.BEST_SCORE, 10000),
    SCORE_25000("score_25000", StatType.BEST_SCORE, 25000),
    COMBO_3("combo_3", StatType.MAX_COMBO, 3),
    COMBO_6("combo_6", StatType.MAX_COMBO, 6),
    BOARD_CLEAR_1("board_clear_1", StatType.BOARD_CLEARS, 1),
    BOARD_CLEAR_10("board_clear_10", StatType.BOARD_CLEARS, 10),
    GAMES_10("games_10", StatType.GAMES_PLAYED, 10),
    GAMES_100("games_100", StatType.GAMES_PLAYED, 100),
    STREAK_3("streak_3", StatType.BEST_STREAK, 3),
    STREAK_7("streak_7", StatType.BEST_STREAK, 7),
    STREAK_30("streak_30", StatType.BEST_STREAK, 30),
    PIECES_1000("pieces_1000", StatType.TOTAL_PIECES, 1000);

    fun isUnlockedBy(stats: PlayerStats): Boolean = stats.value(stat) >= goal

    fun progress(stats: PlayerStats): Int = minOf(stats.value(stat), goal)
}

object Achievements {
    /** Achievements that [stats] now satisfy and that were not unlocked before. */
    fun newlyUnlocked(stats: PlayerStats, alreadyUnlocked: Set<String>): List<Achievement> =
        Achievement.values().filter { it.key !in alreadyUnlocked && it.isUnlockedBy(stats) }
}
