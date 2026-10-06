package com.blockcrush.puzzle.logic

import kotlin.random.Random

/**
 * Hands out random trays of pieces with weighted rarity and a fairness rule:
 * when the board is getting full it becomes more likely (never guaranteed) that at
 * least one piece of a fresh tray can be placed somewhere.
 */
open class PieceGenerator(
    private val random: Random,
    private val catalog: List<Shape> = Shapes.ALL,
    private val fairness: Fairness = Fairness()
) {

    /**
     * @param startFillRatio board fill ratio (0..1) from which the fairness rule kicks in
     * @param minChance chance of a rescue piece right at [startFillRatio]
     * @param maxChance chance of a rescue piece on a completely full board
     */
    data class Fairness(
        val startFillRatio: Float = 0.45f,
        val minChance: Float = 0.55f,
        val maxChance: Float = 0.90f
    )

    private val totalWeight = catalog.sumOf { it.weight }

    fun randomShape(): Shape = weightedPick(catalog, totalWeight)

    open fun nextTray(board: Board, count: Int = 3): List<Shape> {
        val tray = MutableList(count) { randomShape() }
        // A little variety: avoid a tray of three identical pieces.
        if (count >= 3 && tray.all { it.id == tray[0].id }) tray[count - 1] = randomShape()

        val fill = board.filledCount().toFloat() / board.cellCount
        if (fill >= fairness.startFillRatio && tray.none { board.canPlaceAnywhere(it) }) {
            val t = ((fill - fairness.startFillRatio) / (1f - fairness.startFillRatio)).coerceIn(0f, 1f)
            val chance = fairness.minChance + (fairness.maxChance - fairness.minChance) * t
            if (random.nextFloat() < chance) {
                val fitting = catalog.filter { board.canPlaceAnywhere(it) }
                if (fitting.isNotEmpty()) {
                    tray[random.nextInt(count)] = weightedPick(fitting, fitting.sumOf { it.weight })
                }
            }
        }
        return tray
    }

    /** A tray where every piece fits the board right now (used after a rewarded continue). */
    open fun fittingTray(board: Board, count: Int = 3): List<Shape> {
        val fitting = catalog.filter { board.canPlaceAnywhere(it) }
        if (fitting.isEmpty()) return List(count) { randomShape() }
        val weight = fitting.sumOf { it.weight }
        return List(count) { weightedPick(fitting, weight) }
    }

    private fun weightedPick(list: List<Shape>, total: Int): Shape {
        var r = random.nextInt(total)
        for (s in list) {
            r -= s.weight
            if (r < 0) return s
        }
        return list.last()
    }
}
