package com.blockcrush.puzzle.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShapesTest {

    @Test
    fun catalogContainsEveryRequestedShapeFamily() {
        val ids = Shapes.ALL.map { it.id }
        assertEquals(ids.size, ids.toSet().size) // unique ids
        assertTrue("single" in ids)
        for (n in 2..5) {
            assertTrue("line${n}h" in ids)
            assertTrue("line${n}v" in ids)
        }
        assertTrue("square2" in ids)
        assertTrue("square3" in ids)
        assertEquals(4, ids.count { it.startsWith("l_") })
        assertEquals(4, ids.count { it.startsWith("j_") })
        assertEquals(4, ids.count { it.startsWith("t_") })
        assertEquals(2, ids.count { it.startsWith("s_") })
        assertEquals(2, ids.count { it.startsWith("z_") })
        assertEquals(4, ids.count { it.startsWith("corner_small_") })
        assertEquals(4, ids.count { it.startsWith("corner_large_") })
        assertEquals(35, Shapes.ALL.size)
    }

    @Test
    fun shapesAreNormalisedAndSized() {
        for (s in Shapes.ALL) {
            assertEquals(0, s.cells.minOf { it.row })
            assertEquals(0, s.cells.minOf { it.col })
            assertEquals(s.cells.maxOf { it.row } + 1, s.height)
            assertEquals(s.cells.maxOf { it.col } + 1, s.width)
            assertTrue(s.weight > 0)
            assertTrue(s.colorIndex in 0 until Shapes.COLOR_COUNT)
        }
        assertEquals(1, shape("single").size)
        assertEquals(5, shape("line5h").size)
        assertEquals(1, shape("line5h").height)
        assertEquals(5, shape("line5v").height)
        assertEquals(9, shape("square3").size)
        assertEquals(5, shape("corner_large_0").size)
        assertEquals(3, shape("corner_small_0").size)
        assertEquals(4, shape("t_0").size)
    }

    @Test
    fun rotationProducesTheExpectedCells() {
        assertEquals(listOf("XXX", "X.."), Shapes.rotateClockwise(listOf("X.", "X.", "XX")))
        assertEquals(listOf("XX", ".X", ".X"), Shapes.rotateClockwise(listOf("XXX", "X..")))
        // S and Z only have two distinct rotations and must not be confused with each other.
        val s0 = shape("s_0")
        val z0 = shape("z_0")
        assertTrue(!s0.sameCellsAs(z0))
    }

    @Test
    fun shapeFromPatternIgnoresDotsAndMirroringWorks() {
        val l = Shape.fromPattern("x", 0, 1, listOf("X.", "X.", "XX"))
        assertEquals(listOf(Cell(0, 0), Cell(1, 0), Cell(2, 0), Cell(2, 1)), l.cells)
        assertTrue(l.hasCell(2, 1))
        assertTrue(!l.hasCell(0, 1))
    }
}
