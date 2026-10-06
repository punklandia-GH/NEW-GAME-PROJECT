package com.blockcrush.puzzle.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PieceGeneratorTest {

    @Test
    fun traysHaveThreePiecesFromTheCatalog() {
        val g = PieceGenerator(Random(5))
        val b = Board()
        repeat(200) {
            val t = g.nextTray(b)
            assertEquals(3, t.size)
            assertTrue(t.all { Shapes.byId(it.id) === it })
        }
    }

    @Test
    fun everyShapeEventuallyAppearsAndRareOnesAreRarer() {
        val g = PieceGenerator(Random(11))
        val counts = HashMap<String, Int>()
        repeat(20000) { counts.merge(g.randomShape().id, 1, Int::plus) }
        assertEquals(Shapes.ALL.size, counts.size)
        assertTrue(counts.getValue("square2") > counts.getValue("square3"))
        assertTrue(counts.getValue("single") > counts.getValue("line5h"))
    }

    @Test
    fun fairnessMakesAFittingPieceMuchMoreLikelyOnAFullBoard() {
        val board = Board()
        board.fillAll()
        board[7, 7] = 0 // only a single block fits

        fun rateWith(fairness: PieceGenerator.Fairness): Double {
            val g = PieceGenerator(Random(42), fairness = fairness)
            var fitting = 0
            val trials = 2000
            repeat(trials) { if (g.nextTray(board).any { board.canPlaceAnywhere(it) }) fitting++ }
            return fitting.toDouble() / trials
        }

        val withFairness = rateWith(PieceGenerator.Fairness())
        val withoutFairness = rateWith(PieceGenerator.Fairness(startFillRatio = 2f))
        assertTrue("fair rate $withFairness should be high", withFairness > 0.75)
        assertTrue("unfair rate $withoutFairness should be low", withoutFairness < 0.30)
        assertTrue("fairness must not guarantee a fit", withFairness < 0.99)
    }

    @Test
    fun fairnessDoesNothingOnAnEmptyBoard() {
        val board = Board()
        val a = PieceGenerator(Random(3)).nextTray(board).map { it.id }
        val b = PieceGenerator(Random(3), fairness = PieceGenerator.Fairness(startFillRatio = 2f)).nextTray(board).map { it.id }
        assertEquals(a, b)
    }

    @Test
    fun fittingTrayOnlyReturnsPiecesThatFit() {
        val board = Board()
        board.fillAll()
        board[0, 0] = 0
        board[0, 1] = 0
        val g = PieceGenerator(Random(9))
        repeat(100) {
            val t = g.fittingTray(board)
            assertEquals(3, t.size)
            assertTrue(t.all { board.canPlaceAnywhere(it) })
        }
    }
}
