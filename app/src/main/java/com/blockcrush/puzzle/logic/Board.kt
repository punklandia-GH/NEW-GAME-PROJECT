package com.blockcrush.puzzle.logic

/** A cell that was cleared, with the colour value it held (colorIndex + 1). */
data class ClearedCell(val row: Int, val col: Int, val value: Int)

/**
 * The 8x8 playing field. Each cell holds 0 (empty) or colorIndex + 1.
 */
class Board(val size: Int = SIZE) {

    companion object {
        const val SIZE = 8
    }

    private val cells = IntArray(size * size)

    val cellCount: Int get() = size * size

    operator fun get(row: Int, col: Int): Int = cells[row * size + col]

    operator fun set(row: Int, col: Int, value: Int) {
        cells[row * size + col] = value
    }

    fun isEmptyAt(row: Int, col: Int): Boolean = cells[row * size + col] == 0

    fun inBounds(row: Int, col: Int): Boolean = row in 0 until size && col in 0 until size

    /** True if the shape, anchored with its top-left cell at (row, col), fits on empty cells. */
    fun canPlace(shape: Shape, row: Int, col: Int): Boolean {
        if (row < 0 || col < 0 || row + shape.height > size || col + shape.width > size) return false
        for (c in shape.cells) {
            if (cells[(row + c.row) * size + (col + c.col)] != 0) return false
        }
        return true
    }

    /** Places the shape and returns the board cells it now occupies. */
    fun place(shape: Shape, row: Int, col: Int): List<Cell> {
        require(canPlace(shape, row, col)) { "Shape ${shape.id} does not fit at ($row,$col)" }
        val value = shape.colorIndex + 1
        val placed = ArrayList<Cell>(shape.size)
        for (c in shape.cells) {
            val r = row + c.row
            val cc = col + c.col
            cells[r * size + cc] = value
            placed += Cell(r, cc)
        }
        return placed
    }

    fun canPlaceAnywhere(shape: Shape): Boolean {
        for (r in 0..size - shape.height) {
            for (c in 0..size - shape.width) {
                if (canPlace(shape, r, c)) return true
            }
        }
        return false
    }

    fun countPlacements(shape: Shape): Int {
        var n = 0
        for (r in 0..size - shape.height) {
            for (c in 0..size - shape.width) {
                if (canPlace(shape, r, c)) n++
            }
        }
        return n
    }

    fun rowFill(row: Int): Int {
        var n = 0
        for (c in 0 until size) if (cells[row * size + c] != 0) n++
        return n
    }

    fun colFill(col: Int): Int {
        var n = 0
        for (r in 0 until size) if (cells[r * size + col] != 0) n++
        return n
    }

    fun fullRows(): List<Int> = (0 until size).filter { rowFill(it) == size }

    fun fullCols(): List<Int> = (0 until size).filter { colFill(it) == size }

    /**
     * Empties every cell in the given rows and columns.
     * Returns the distinct cells that actually held a block, with their colour values.
     */
    fun clearLines(rows: List<Int>, cols: List<Int>): List<ClearedCell> {
        val cleared = ArrayList<ClearedCell>()
        val seen = BooleanArray(size * size)
        for (r in rows) {
            for (c in 0 until size) takeCell(r, c, seen, cleared)
        }
        for (c in cols) {
            for (r in 0 until size) takeCell(r, c, seen, cleared)
        }
        return cleared
    }

    private fun takeCell(r: Int, c: Int, seen: BooleanArray, out: MutableList<ClearedCell>) {
        val i = r * size + c
        if (seen[i]) return
        seen[i] = true
        val v = cells[i]
        if (v != 0) {
            out += ClearedCell(r, c, v)
            cells[i] = 0
        }
    }

    fun filledCount(): Int {
        var n = 0
        for (v in cells) if (v != 0) n++
        return n
    }

    fun isEmptyBoard(): Boolean = filledCount() == 0

    fun clearAll() {
        cells.fill(0)
    }

    fun copy(): Board {
        val b = Board(size)
        cells.copyInto(b.cells)
        return b
    }

    fun toIntArray(): IntArray = cells.copyOf()

    fun loadFrom(values: IntArray) {
        require(values.size == cells.size) { "Expected ${cells.size} cells, got ${values.size}" }
        values.copyInto(cells)
    }
}
