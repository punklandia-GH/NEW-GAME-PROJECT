package com.blockcrush.puzzle.logic

/** The catalogue of every piece the game can hand out, plus the colour palette indices. */
object Shapes {
    const val RED = 0
    const val ORANGE = 1
    const val YELLOW = 2
    const val GREEN = 3
    const val CYAN = 4
    const val BLUE = 5
    const val PURPLE = 6
    const val PINK = 7
    const val COLOR_COUNT = 8

    val ALL: List<Shape> = buildCatalog()

    private val index: Map<String, Shape> = ALL.associateBy { it.id }

    fun byId(id: String): Shape? = index[id]

    private fun buildCatalog(): List<Shape> {
        val list = ArrayList<Shape>()
        list += Shape.fromPattern("single", YELLOW, 7, listOf("X"))
        for (n in 2..5) {
            val weight = when (n) { 2 -> 6; 3 -> 7; 4 -> 5; else -> 3 }
            val color = when (n) { 2 -> CYAN; 3 -> ORANGE; 4 -> BLUE; else -> RED }
            list += Shape.fromPattern("line${n}h", color, weight, listOf("X".repeat(n)))
            list += Shape.fromPattern("line${n}v", color, weight, List(n) { "X" })
        }
        list += Shape.fromPattern("square2", GREEN, 9, listOf("XX", "XX"))
        list += Shape.fromPattern("square3", PURPLE, 3, listOf("XXX", "XXX", "XXX"))
        list += rotations("l", PINK, 3, listOf("X.", "X.", "XX"))
        list += rotations("j", CYAN, 3, listOf(".X", ".X", "XX"))
        list += rotations("t", PURPLE, 3, listOf("XXX", ".X."))
        list += rotations("s", GREEN, 3, listOf(".XX", "XX."))
        list += rotations("z", RED, 3, listOf("XX.", ".XX"))
        list += rotations("corner_small", YELLOW, 5, listOf("X.", "XX"))
        list += rotations("corner_large", ORANGE, 3, listOf("X..", "X..", "XXX"))
        return list
    }

    /** All distinct 90-degree rotations of a pattern, named id_0 .. id_3. */
    private fun rotations(id: String, color: Int, weight: Int, pattern: List<String>): List<Shape> {
        val result = ArrayList<Shape>()
        var current = pattern
        for (i in 0 until 4) {
            val shape = Shape.fromPattern("${id}_$i", color, weight, current)
            if (result.none { it.sameCellsAs(shape) }) result += shape
            current = rotateClockwise(current)
        }
        return result
    }

    internal fun rotateClockwise(pattern: List<String>): List<String> {
        val h = pattern.size
        val w = pattern[0].length
        return List(w) { c -> buildString { for (r in h - 1 downTo 0) append(pattern[r][c]) } }
    }
}
