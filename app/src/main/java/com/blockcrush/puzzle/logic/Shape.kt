package com.blockcrush.puzzle.logic

/** A single cell position, on the board or inside a shape. Row first, then column. */
data class Cell(val row: Int, val col: Int)

/**
 * An immutable piece shape.
 *
 * Cells are normalised so the top-left corner of the bounding box is (0,0) and sorted
 * row-major, which means two shapes with the same cells compare equal cell-for-cell.
 */
class Shape(val id: String, val colorIndex: Int, val weight: Int, cells: List<Cell>) {

    val cells: List<Cell> = normalize(cells)
    val width: Int = this.cells.maxOf { it.col } + 1
    val height: Int = this.cells.maxOf { it.row } + 1
    val size: Int get() = cells.size

    fun hasCell(row: Int, col: Int): Boolean = cells.any { it.row == row && it.col == col }

    fun sameCellsAs(other: Shape): Boolean = cells == other.cells

    override fun toString(): String = id

    companion object {
        fun normalize(cells: List<Cell>): List<Cell> {
            require(cells.isNotEmpty()) { "A shape needs at least one cell" }
            val minRow = cells.minOf { it.row }
            val minCol = cells.minOf { it.col }
            return cells.map { Cell(it.row - minRow, it.col - minCol) }
                .distinct()
                .sortedWith(compareBy({ it.row }, { it.col }))
        }

        /** Builds a shape from rows of text where 'X' is a filled cell and '.' is empty. */
        fun fromPattern(id: String, colorIndex: Int, weight: Int, pattern: List<String>): Shape {
            val cells = ArrayList<Cell>()
            pattern.forEachIndexed { r, line ->
                line.forEachIndexed { c, ch -> if (ch == 'X') cells.add(Cell(r, c)) }
            }
            return Shape(id, colorIndex, weight, cells)
        }
    }
}
