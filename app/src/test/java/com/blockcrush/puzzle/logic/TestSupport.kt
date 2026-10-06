package com.blockcrush.puzzle.logic

import kotlin.random.Random

/** A generator that returns pre-scripted trays first, then falls back to random ones. */
class ScriptedGenerator(
    trays: List<List<Shape>>,
    random: Random = Random(1)
) : PieceGenerator(random) {
    private val script = ArrayDeque(trays)

    override fun nextTray(board: Board, count: Int): List<Shape> =
        if (script.isEmpty()) super.nextTray(board, count) else script.removeFirst()
}

fun shape(id: String): Shape = Shapes.byId(id) ?: error("Unknown shape id: $id")

/** Fills every cell of [row] with the given colour value. */
fun Board.fillRow(row: Int, value: Int = 1, except: Int = -1) {
    for (c in 0 until size) if (c != except) this[row, c] = value
}

fun Board.fillCol(col: Int, value: Int = 1, except: Int = -1) {
    for (r in 0 until size) if (r != except) this[r, col] = value
}

fun Board.fillAll(value: Int = 1) {
    for (r in 0 until size) for (c in 0 until size) this[r, c] = value
}

fun engineWith(vararg trays: List<Shape>, seed: Int = 7): GameEngine =
    GameEngine(random = Random(seed), generator = ScriptedGenerator(trays.toList(), Random(seed)))
