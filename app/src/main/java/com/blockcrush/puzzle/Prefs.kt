package com.blockcrush.puzzle

import android.content.Context
import android.content.SharedPreferences
import com.blockcrush.puzzle.achievements.PlayerStats

/** All persisted player data: settings, scores, statistics, streaks, ad counters and the saved game. */
class Prefs(context: Context) {

    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("block_crush_prefs", Context.MODE_PRIVATE)

    // ---- Settings -------------------------------------------------------------------------

    var soundOn: Boolean
        get() = sp.getBoolean("sound_on", true)
        set(value) = sp.edit().putBoolean("sound_on", value).apply()

    var musicOn: Boolean
        get() = sp.getBoolean("music_on", true)
        set(value) = sp.edit().putBoolean("music_on", value).apply()

    var vibrationOn: Boolean
        get() = sp.getBoolean("vibration_on", true)
        set(value) = sp.edit().putBoolean("vibration_on", value).apply()

    var reminderOn: Boolean
        get() = sp.getBoolean("reminder_on", false)
        set(value) = sp.edit().putBoolean("reminder_on", value).apply()

    /** True once the "Remove Ads" purchase is owned. Banner and interstitial ads stop; rewarded ads stay. */
    var adsRemoved: Boolean
        get() = sp.getBoolean("ads_removed", false)
        set(value) = sp.edit().putBoolean("ads_removed", value).apply()

    // ---- Scores and lifetime statistics ---------------------------------------------------

    var bestScore: Int
        get() = sp.getInt("best_score", 0)
        set(value) = sp.edit().putInt("best_score", value).apply()

    var gamesPlayed: Int
        get() = sp.getInt("games_played", 0)
        set(value) = sp.edit().putInt("games_played", value).apply()

    var totalLines: Int
        get() = sp.getInt("total_lines", 0)
        set(value) = sp.edit().putInt("total_lines", value).apply()

    var totalPieces: Int
        get() = sp.getInt("total_pieces", 0)
        set(value) = sp.edit().putInt("total_pieces", value).apply()

    var maxCombo: Int
        get() = sp.getInt("max_combo", 0)
        set(value) = sp.edit().putInt("max_combo", value).apply()

    var boardClears: Int
        get() = sp.getInt("board_clears", 0)
        set(value) = sp.edit().putInt("board_clears", value).apply()

    /** Local calendar day (see DayClock) on which the player last played; -1 if never. */
    var lastPlayedDay: Long
        get() = sp.getLong("last_played_day", -1L)
        set(value) = sp.edit().putLong("last_played_day", value).apply()

    val stats: PlayerStats
        get() = PlayerStats(
            totalLines = totalLines,
            bestScore = bestScore,
            maxCombo = maxCombo,
            boardClears = boardClears,
            gamesPlayed = gamesPlayed,
            bestStreak = bestStreak,
            totalPieces = totalPieces
        )

    // ---- Daily challenge ------------------------------------------------------------------

    var dailyStreak: Int
        get() = sp.getInt("daily_streak", 0)
        set(value) = sp.edit().putInt("daily_streak", value).apply()

    var bestStreak: Int
        get() = sp.getInt("best_streak", 0)
        set(value) = sp.edit().putInt("best_streak", value).apply()

    /** Local calendar day of the last completed daily challenge; -1 if none. */
    var dailyLastCompletedDay: Long
        get() = sp.getLong("daily_last_completed_day", -1L)
        set(value) = sp.edit().putLong("daily_last_completed_day", value).apply()

    fun dailyBestScore(epochDay: Long): Int =
        if (sp.getLong("daily_best_day", -1L) == epochDay) sp.getInt("daily_best_score", 0) else 0

    fun setDailyBestScore(epochDay: Long, score: Int) {
        if (score > dailyBestScore(epochDay)) {
            sp.edit().putLong("daily_best_day", epochDay).putInt("daily_best_score", score).apply()
        }
    }

    // ---- Ads ------------------------------------------------------------------------------

    var gamesSinceInterstitial: Int
        get() = sp.getInt("games_since_interstitial", 0)
        set(value) = sp.edit().putInt("games_since_interstitial", value).apply()

    var lastInterstitialAt: Long
        get() = sp.getLong("last_interstitial_at", 0L)
        set(value) = sp.edit().putLong("last_interstitial_at", value).apply()

    // ---- Saved game (classic mode) --------------------------------------------------------

    var savedGame: String?
        get() = sp.getString("saved_game", null)
        set(value) {
            if (value == null) sp.edit().remove("saved_game").apply()
            else sp.edit().putString("saved_game", value).apply()
        }

    // ---- Achievements ---------------------------------------------------------------------

    val unlockedAchievements: Set<String>
        get() = sp.getStringSet("achievements", emptySet())?.toSet() ?: emptySet()

    fun unlockAchievements(keys: Collection<String>) {
        if (keys.isEmpty()) return
        val merged = HashSet(unlockedAchievements)
        merged.addAll(keys)
        sp.edit().putStringSet("achievements", merged).apply()
    }
}
