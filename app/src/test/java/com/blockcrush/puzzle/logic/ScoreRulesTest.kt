package com.blockcrush.puzzle.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class ScoreRulesTest {

    private val rules = ScoreRules()

    @Test
    fun lineClearPointsGrowWithLinesAndCombo() {
        assertEquals(0, rules.lineClearPoints(0, 1))
        assertEquals(10, rules.lineClearPoints(1, 1))
        assertEquals(40, rules.lineClearPoints(2, 1))
        assertEquals(90, rules.lineClearPoints(3, 1))
        assertEquals(160, rules.lineClearPoints(4, 1))
        assertEquals(30, rules.lineClearPoints(1, 3))
        assertEquals(120, rules.lineClearPoints(2, 3))
        assertEquals(10, rules.lineClearPoints(1, 0)) // combo below 1 behaves like 1
        assertEquals(4, rules.placementPoints(4))
        assertEquals(300, rules.boardClearBonus)
    }

    @Test
    fun praiseWordsFollowLinesAndCombo() {
        assertEquals(Praise.NONE, Praise.forMove(0, 5))
        assertEquals(Praise.NICE, Praise.forMove(1, 1))
        assertEquals(Praise.GREAT, Praise.forMove(2, 1))
        assertEquals(Praise.AMAZING, Praise.forMove(3, 1))
        assertEquals(Praise.FANTASTIC, Praise.forMove(4, 1))
        assertEquals(Praise.UNBELIEVABLE, Praise.forMove(5, 1))
        assertEquals(Praise.GREAT, Praise.forMove(1, 2))
        assertEquals(Praise.AMAZING, Praise.forMove(1, 4))
        assertEquals(Praise.FANTASTIC, Praise.forMove(1, 6))
        assertEquals(Praise.UNBELIEVABLE, Praise.forMove(1, 8))
        assertEquals(Praise.AMAZING, Praise.forMove(3, 2)) // lines win when bigger
    }

    @Test
    fun codecRejectsGarbage() {
        assertNull(GameStateCodec.decode(null))
        assertNull(GameStateCodec.decode(""))
        assertNull(GameStateCodec.decode("hello"))
        assertNull(GameStateCodec.decode("1|0|0|0|0|0|0|0|0|single,-,-|123"))
        assertNull(GameStateCodec.decode("2|0|0|0|0|0|0|0|0|single,-,-|" + "0".repeat(64)))
        assertNull(GameStateCodec.decode("1|0|0|0|0|0|0|0|0|nope,-,-|" + "0".repeat(64)))
        assertNull(GameStateCodec.decode("1|0|0|0|0|0|0|0|0|single,-,-|" + "9".repeat(64)))
        assertNotNull(GameStateCodec.decode("1|12|1|2|3|4|0|0|1|single,-,line2h|" + "0".repeat(64)))
    }
}
