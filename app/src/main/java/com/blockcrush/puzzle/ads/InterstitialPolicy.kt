package com.blockcrush.puzzle.ads

/**
 * When an interstitial may be shown after a game over:
 * at most once every [minGamesBetween] games AND at most once every [minIntervalMillis].
 */
class InterstitialPolicy(
    val minGamesBetween: Int = 3,
    val minIntervalMillis: Long = 3 * 60_000L
) {
    fun shouldShow(
        gamesSinceLastAd: Int,
        lastShownAtMillis: Long,
        nowMillis: Long,
        adsRemoved: Boolean,
        adReady: Boolean
    ): Boolean {
        if (adsRemoved || !adReady) return false
        if (gamesSinceLastAd < minGamesBetween) return false
        if (lastShownAtMillis > 0 && nowMillis - lastShownAtMillis < minIntervalMillis) return false
        return true
    }
}
