package com.blockcrush.puzzle.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTest {

    @Test
    fun placementRespectsBoundsAndOverlap() {
        val b = Board()
        val sq = shape("square2")
        assertTrue(b.canPlace(sq, 0, 0))
        assertTrue(b.canPlace(sq, 6, 6))
        assertFalse(b.canPlace(sq, 7, 7))
        assertFalse(b.canPlace(sq, -1, 0))
        assertFalse(b.canPlace(sq, 0, 7))
        b.place(sq, 0, 0)
        assertEquals(Shapes.GREEN + 1, b[0, 0])
        assertEquals(Shapes.GREEN + 1, b[1, 1])
        assertFalse(b.canPlace(sq, 1, 1))
        assertTrue(b.canPlace(sq, 2, 2))
        assertEquals(4, b.filledCount())
    }

    @Test
    fun placementCountsOnEmptyBoard() {
        val b = Board()
        assertEquals(64, b.countPlacements(shape("single")))
        assertEquals(36, b.countPlacements(shape("square3")))
        assertEquals(32, b.countPlacements(shape("line5h")))
        assertEquals(32, b.countPlacements(shape("line5v")))
        assertTrue(b.canPlaceAnywhere(shape("square3")))
    }

    @Test
    fun fullLinesAreDetectedAndCleared() {
        val b = Board()
        b.fillRow(3)
        b.fillCol(5)
        assertEquals(listOf(3), b.fullRows())
        assertEquals(listOf(5), b.fullCols())
        val cleared = b.clearLines(b.fullRows(), b.fullCols())
        assertEquals(15, cleared.size) // 8 + 8 - 1 shared cell
        assertEquals(0, b.filledCount())
        assertTrue(b.isEmptyBoard())
    }

    @Test
    fun clearingPartialRowsOnlyReportsFilledCells() {
        val b = Board()
        b[2, 0] = 1
        b[2, 1] = 2
        val cleared = b.clearLines(listOf(2), emptyList())
        assertEquals(listOf(ClearedCell(2, 0, 1), ClearedCell(2, 1, 2)), cleared)
        assertEquals(0, b.rowFill(2))
    }

    @Test
    fun canPlaceAnywhereIsFalseOnFullBoard() {
        val b = Board()
        b.fillAll()
        assertFalse(b.canPlaceAnywhere(shape("single")))
        b[7, 7] = 0
        assertTrue(b.canPlaceAnywhere(shape("single")))
        assertFalse(b.canPlaceAnywhere(shape("line2h")))
    }

    @Test
    fun copyAndLoadRoundTrip() {
        val b = Board()
        b.fillRow(0, 3)
        val copy = b.copy()
        assertEquals(3, copy[0, 7])
        copy[0, 7] = 0
        assertEquals(3, b[0, 7])
        val other = Board()
        other.loadFrom(b.toIntArray())
        assertEquals(8, other.rowFill(0))
    }
}
