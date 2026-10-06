package com.blockcrush.puzzle.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {

    private val single = shape("single")
    private val line2h = shape("line2h")
    private val square3 = shape("square3")
    private val line5h = shape("line5h")

    @Test
    fun newGameStartsEmptyWithThreePieces() {
        val e = GameEngine(Random(3))
        assertEquals(0, e.score)
        assertTrue(e.board.isEmptyBoard())
        assertEquals(3, e.tray.size)
        assertTrue(e.tray.all { it != null })
        assertFalse(e.isGameOver)
        assertFalse(e.continueUsed)
    }

    @Test
    fun placingAPieceScoresOnePointPerBlockAndEmptiesTheSlot() {
        val e = engineWith(listOf(square3, single, line2h))
        val r = e.place(0, 0, 0)
        assertNotNull(r)
        r!!
        assertEquals(9, r.placementPoints)
        assertEquals(0, r.linePoints)
        assertEquals(9, e.score)
        assertEquals(0, r.combo)
        assertEquals(Praise.NONE, r.praise)
        assertNull(e.trayPiece(0))
        assertEquals(9, r.placedCells.size)
        assertEquals(1, e.piecesPlaced)
        assertFalse(r.trayRefilled)
    }

    @Test
    fun invalidPlacementIsRejectedAndChangesNothing() {
        val e = engineWith(listOf(square3, single, line2h))
        assertNull(e.place(0, 6, 6)) // out of bounds
        e.place(0, 0, 0)
        assertNull(e.place(1, 1, 1)) // overlaps
        assertNull(e.place(0, 5, 5)) // slot already empty
        assertEquals(9, e.score)
        assertEquals(1, e.piecesPlaced)
    }

    @Test
    fun completingARowClearsItAndScoresTen() {
        val e = engineWith(listOf(single, line2h, square3))
        e.board.fillRow(0, except = 4)
        val r = e.place(0, 0, 4)!!
        assertEquals(listOf(0), r.clearedRows)
        assertTrue(r.clearedCols.isEmpty())
        assertEquals(8, r.clearedCells.size)
        assertEquals(10, r.linePoints)
        assertEquals(1, r.combo)
        assertEquals(Praise.NICE, r.praise)
        assertEquals(11, e.score)
        assertEquals(1, e.linesCleared)
        assertEquals(0, e.board.rowFill(0))
    }

    @Test
    fun rowAndColumnClearTogetherScoreAsTwoLines() {
        val e = engineWith(listOf(single, line2h, square3))
        e.board.fillRow(2, except = 3)
        e.board.fillCol(3, except = 2)
        val r = e.place(0, 2, 3)!!
        assertEquals(2, r.lines)
        assertEquals(15, r.clearedCells.size)
        assertEquals(40, r.linePoints) // 10 * 2 * 2 * combo 1
        assertEquals(Praise.GREAT, r.praise)
        assertEquals(41, e.score)
    }

    @Test
    fun threeAndFourLinesScoreQuadratically() {
        val e = engineWith(listOf(line5h, single, line2h))
        // Columns 0..4 full except row 7, where the 5-long line will land.
        for (c in 0..4) e.board.fillCol(c, except = 7)
        val r = e.place(0, 7, 0)!!
        assertEquals(5, r.clearedCols.size)
        assertEquals(10 * 5 * 5, r.linePoints)
        assertEquals(Praise.UNBELIEVABLE, r.praise)
    }

    @Test
    fun comboBuildsOnConsecutiveClearsAndResetsOnAMiss() {
        val e = engineWith(listOf(single, single, single), listOf(single, single, single))
        e.board.fillRow(0, except = 0)
        e.board.fillRow(1, except = 0)
        val first = e.place(0, 0, 0)!!
        assertEquals(1, first.combo)
        assertEquals(10, first.linePoints)
        val second = e.place(1, 1, 0)!!
        assertEquals(2, second.combo)
        assertEquals(20, second.linePoints) // 10 * 1 * 1 * combo 2
        assertEquals(Praise.GREAT, second.praise)
        assertEquals(2, e.maxCombo)
        val miss = e.place(2, 5, 5)!!      // no clear: combo resets
        assertEquals(0, miss.combo)
        assertEquals(0, e.combo)
        assertEquals(2, e.maxCombo)
        assertTrue(miss.trayRefilled)
        // Next clear starts again at combo 1.
        e.board.fillRow(7, except = 7)
        val again = e.place(0, 7, 7)!!
        assertEquals(1, again.combo)
        assertEquals(10, again.linePoints)
    }

    @Test
    fun clearingTheWholeBoardAwardsTheBonus() {
        val e = engineWith(listOf(single, line2h, square3))
        e.board.fillRow(5, except = 2)
        val r = e.place(0, 5, 2)!!
        assertTrue(r.boardCleared)
        assertEquals(300, r.boardClearBonus)
        assertEquals(1 + 10 + 300, r.totalPoints)
        assertEquals(1, e.boardClears)
        assertTrue(e.board.isEmptyBoard())
    }

    @Test
    fun trayRefillsOnlyWhenAllThreePiecesAreUsed() {
        val e = engineWith(listOf(single, single, single), listOf(line2h, line2h, line2h))
        assertFalse(e.place(0, 0, 0)!!.trayRefilled)
        assertFalse(e.place(1, 0, 2)!!.trayRefilled)
        assertEquals(1, e.tray.count { it != null })
        val last = e.place(2, 0, 4)!!
        assertTrue(last.trayRefilled)
        assertTrue(e.tray.all { it?.id == "line2h" })
    }

    @Test
    fun gameOverWhenNoRemainingPieceFits() {
        val e = engineWith(listOf(single, square3, line5h))
        e.board.fillAll()
        e.board[0, 0] = 0
        e.board[7, 7] = 0
        assertFalse(e.isGameOver) // the single still fits
        val r = e.place(0, 0, 0)!!
        assertTrue(r.gameOver)
        assertTrue(e.isGameOver)
        assertNull(e.place(1, 0, 0)) // no moves accepted after game over
    }

    @Test
    fun notGameOverWhileAnyPieceFits() {
        val e = engineWith(listOf(single, square3, line2h))
        e.board.fillAll()
        e.board[3, 3] = 0
        e.board[3, 4] = 0
        e.board[6, 6] = 0
        e.place(0, 6, 6)
        assertFalse(e.isGameOver) // line2h fits at (3,3)
        assertTrue(e.pieceFitsAnywhere(2))
        assertFalse(e.pieceFitsAnywhere(1))
    }

    @Test
    fun gameOverIsCheckedAgainstTheFreshTray() {
        // After the last of three pieces is placed on a packed board, the new tray decides.
        val e = engineWith(listOf(single, single, single), listOf(square3, square3, square3))
        e.board.fillAll()
        e.board[0, 0] = 0
        e.board[0, 1] = 0
        e.board[0, 2] = 0
        e.place(0, 0, 0)
        e.place(1, 0, 1)
        val r = e.place(2, 0, 2)!!
        assertTrue(r.trayRefilled)
        assertTrue(r.gameOver)
    }

    @Test
    fun continueClearsRowsAndResumesPlay() {
        val e = engineWith(listOf(square3, square3, square3))
        e.board.fillAll()
        e.board[0, 0] = 0
        assertFalse(e.isGameOver) // not checked until a move; force the check by restoring
        e.restore(e.snapshot())
        assertTrue(e.isGameOver)
        val c = e.useContinue()
        assertEquals(3, c.clearedRows.size)
        assertEquals(c.clearedRows.sumOf { row -> if (row == 0) 7 else 8 }, c.clearedCells.size)
        assertTrue(e.continueUsed)
        assertFalse(e.isGameOver)
        assertFalse(c.stillGameOver)
        assertTrue(e.anyPieceFits())
        assertEquals(0, e.combo)
        var threw = false
        try {
            e.useContinue()
        } catch (ex: IllegalStateException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun continueRerollsTrayWhenNothingFitsAfterClearing() {
        val e = engineWith(listOf(square3, square3, square3))
        // Leave only rows 0 and 7 partly filled so that at most two rows can be cleared.
        e.board.fillRow(0)
        e.board.fillRow(7)
        e.board[0, 0] = 0
        // Make the middle rows such that no 3x3 fits anywhere: checkerboard columns.
        for (r in 1..6) for (c in 0 until 8 step 2) e.board[r, c] = 1
        e.restore(e.snapshot())
        assertTrue(e.isGameOver)
        val c = e.useContinue()
        assertTrue(c.trayRerolled || e.anyPieceFits())
        assertFalse(e.isGameOver)
    }

    @Test
    fun previewMatchesTheRealClear() {
        val e = engineWith(listOf(line2h, single, square3))
        e.board.fillRow(4, except = 2)
        e.board[4, 3] = 0
        e.board.fillCol(2, except = 4)
        val rows = BooleanArray(8)
        val cols = BooleanArray(8)
        assertEquals(2, e.previewLines(0, 4, 2, rows, cols))
        assertTrue(rows[4])
        assertTrue(cols[2])
        assertFalse(cols[3])
        assertEquals(-1, e.previewLines(0, 4, 7, rows, cols)) // does not fit
        assertEquals(-1, e.previewLines(0, 0, 2, rows, cols)) // overlaps the column
        val r = e.place(0, 4, 2)!!
        assertEquals(listOf(4), r.clearedRows)
        assertEquals(listOf(2), r.clearedCols)
    }

    @Test
    fun snapshotRoundTripThroughCodec() {
        val e = engineWith(listOf(single, line2h, square3), listOf(single, single, single))
        e.board.fillRow(0, except = 0)
        e.place(0, 0, 0)
        e.place(1, 3, 3)
        val text = GameStateCodec.encode(e.snapshot())
        val restored = GameEngine(Random(99))
        restored.restore(GameStateCodec.decode(text)!!)
        assertEquals(e.score, restored.score)
        assertEquals(e.combo, restored.combo)
        assertEquals(e.linesCleared, restored.linesCleared)
        assertEquals(e.piecesPlaced, restored.piecesPlaced)
        assertEquals(e.tray.map { it?.id }, restored.tray.map { it?.id })
        assertTrue(e.board.toIntArray().contentEquals(restored.board.toIntArray()))
        assertEquals(e.isGameOver, restored.isGameOver)
    }

    @Test
    fun aFullRandomGameAlwaysEndsAndStaysConsistent() {
        for (seed in 1..40) {
            val e = GameEngine(Random(seed))
            var moves = 0
            while (!e.isGameOver && moves < 5000) {
                var placed = false
                outer@ for (i in 0 until GameEngine.TRAY_SIZE) {
                    val s = e.trayPiece(i) ?: continue
                    for (r in 0 until 8) for (c in 0 until 8) {
                        if (e.canPlace(i, r, c)) {
                            val before = e.score
                            val res = e.place(i, r, c)!!
                            assertEquals(before + res.totalPoints, e.score)
                            assertEquals(e.board.fullRows().size, 0)
                            assertEquals(e.board.fullCols().size, 0)
                            placed = true
                            break@outer
                        }
                    }
                }
                assertTrue("engine said not game over but nothing could be placed (seed $seed)", placed)
                moves++
            }
            assertTrue("game did not end for seed $seed", e.isGameOver)
            assertFalse(e.anyPieceFits())
            assertTrue(e.score >= moves)
        }
    }
}
