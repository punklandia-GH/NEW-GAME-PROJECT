package com.blockcrush.puzzle.logic

/** Everything that happened when a piece was placed. */
data class MoveResult(
    val trayIndex: Int,
    val shape: Shape,
    val row: Int,
    val col: Int,
    val placedCells: List<Cell>,
    val clearedRows: List<Int>,
    val clearedCols: List<Int>,
    val clearedCells: List<ClearedCell>,
    val placementPoints: Int,
    val linePoints: Int,
    val boardClearBonus: Int,
    /** Combo count after this move (0 when the move cleared nothing). */
    val combo: Int,
    val praise: Praise,
    val boardCleared: Boolean,
    val trayRefilled: Boolean,
    val gameOver: Boolean,
    val scoreAfter: Int
) {
    val lines: Int get() = clearedRows.size + clearedCols.size
    val totalPoints: Int get() = placementPoints + linePoints + boardClearBonus
}

/** Result of the rewarded "continue": some rows were removed so play can go on. */
data class ContinueResult(
    val clearedRows: List<Int>,
    val clearedCells: List<ClearedCell>,
    val trayRerolled: Boolean,
    val stillGameOver: Boolean
)

/** A serialisable copy of the whole game, used to resume after the app is closed. */
class GameSnapshot(
    val board: IntArray,
    val tray: List<String?>,
    val score: Int,
    val combo: Int,
    val maxCombo: Int,
    val linesCleared: Int,
    val piecesPlaced: Int,
    val boardClears: Int,
    val isGameOver: Boolean,
    val continueUsed: Boolean
)
