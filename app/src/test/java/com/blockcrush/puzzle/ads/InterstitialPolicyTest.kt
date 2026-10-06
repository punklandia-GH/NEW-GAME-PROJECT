package com.blockcrush.puzzle.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialPolicyTest {

    private val policy = InterstitialPolicy()
    private val minute = 60_000L

    @Test
    fun needsThreeGamesAndThreeMinutes() {
        val now = 100 * minute
        assertFalse(policy.shouldShow(2, 0, now, adsRemoved = false, adReady = true))
        assertTrue(policy.shouldShow(3, 0, now, adsRemoved = false, adReady = true))
        assertTrue(policy.shouldShow(7, 0, now, adsRemoved = false, adReady = true))
        assertFalse(policy.shouldShow(3, now - 2 * minute, now, adsRemoved = false, adReady = true))
        assertTrue(policy.shouldShow(3, now - 3 * minute, now, adsRemoved = false, adReady = true))
    }

    @Test
    fun neverWhenAdsRemovedOrNotLoaded() {
        val now = 100 * minute
        assertFalse(policy.shouldShow(10, 0, now, adsRemoved = true, adReady = true))
        assertFalse(policy.shouldShow(10, 0, now, adsRemoved = false, adReady = false))
    }
}
