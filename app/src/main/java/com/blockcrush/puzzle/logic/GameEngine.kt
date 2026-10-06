package com.blockcrush.puzzle.logic

import kotlin.random.Random

/**
 * The complete rules of the game, independent of Android.
 *
 * - An 8x8 board and a tray of three pieces.
 * - A piece can be dropped wherever all of its cells land on empty board cells.
 * - Full rows and columns clear at once; several at once score more.
 * - Consecutive clearing placements build a combo multiplier.
 * - When all three tray pieces are used, three new ones arrive.
 * - The game is over when no remaining tray piece fits anywhere.
 */
class GameEngine(
    random: Random = Random.Default,
    val rules: ScoreRules = ScoreRules(),
    private val generator: PieceGenerator = PieceGenerator(random)
) {
    companion object {
        const val TRAY_SIZE = 3
        const val CONTINUE_ROWS = 3
    }

    private val rng: Random = random

    val board = Board()

    private val trayPieces = arrayOfNulls<Shape>(TRAY_SIZE)

    /** Read-only view of the tray; null means the slot is empty. */
    val tray: List<Shape?> get() = trayPieces.asList()

    var score: Int = 0
        private set
    var combo: Int = 0
        private set
    var maxCombo: Int = 0
        private set
    var linesCleared: Int = 0
        private set
    var piecesPlaced: Int = 0
        private set
    var boardClears: Int = 0
        private set
    var isGameOver: Boolean = false
        private set
    var continueUsed: Boolean = false
        private set

    init {
        newGame()
    }

    fun newGame() {
        board.clearAll()
        score = 0
        combo = 0
        maxCombo = 0
        linesCleared = 0
        piecesPlaced = 0
        boardClears = 0
        isGameOver = false
        continueUsed = false
        refillTray()
    }

    fun trayPiece(index: Int): Shape? = trayPieces[index]

    fun canPlace(index: Int, row: Int, col: Int): Boolean {
        val shape = trayPieces[index] ?: return false
        return board.canPlace(shape, row, col)
    }

    fun pieceFitsAnywhere(index: Int): Boolean {
        val shape = trayPieces[index] ?: return false
        return board.canPlaceAnywhere(shape)
    }

    fun anyPieceFits(): Boolean {
        for (i in 0 until TRAY_SIZE) if (pieceFitsAnywhere(i)) return true
        return false
    }

    /**
     * Without changing anything, marks which rows/columns would clear if the piece were
     * dropped at (row, col). Returns the number of lines, or -1 if the piece does not fit.
     * [outRows] and [outCols] must have [Board.size] entries; they are fully overwritten.
     */
    fun previewLines(index: Int, row: Int, col: Int, outRows: BooleanArray, outCols: BooleanArray): Int {
        outRows.fill(false)
        outCols.fill(false)
        val shape = trayPieces[index] ?: return -1
        if (!board.canPlace(shape, row, col)) return -1
        var lines = 0
        for (r in 0 until shape.height) {
            var added = 0
            for (c in shape.cells) if (c.row == r) added++
            if (board.rowFill(row + r) + added == board.size) {
                outRows[row + r] = true
                lines++
            }
        }
        for (c in 0 until shape.width) {
            var added = 0
            for (cell in shape.cells) if (cell.col == c) added++
            if (board.colFill(col + c) + added == board.size) {
                outCols[col + c] = true
                lines++
            }
        }
        return lines
    }

    /** Drops tray piece [index] with its top-left cell at (row, col). Returns null if it does not fit. */
    fun place(index: Int, row: Int, col: Int): MoveResult? {
        if (isGameOver) return null
        val shape = trayPieces[index] ?: return null
        if (!board.canPlace(shape, row, col)) return null

        val placedCells = board.place(shape, row, col)
        trayPieces[index] = null
        piecesPlaced++

        val fullRows = board.fullRows()
        val fullCols = board.fullCols()
        val lines = fullRows.size + fullCols.size
        val clearedCells = if (lines > 0) board.clearLines(fullRows, fullCols) else emptyList()

        val placementPoints = rules.placementPoints(shape.size)
        var linePoints = 0
        var boardBonus = 0
        var praise = Praise.NONE
        var boardCleared = false
        if (lines > 0) {
            combo += 1
            if (combo > maxCombo) maxCombo = combo
            linesCleared += lines
            linePoints = rules.lineClearPoints(lines, combo)
            praise = Praise.forMove(lines, combo)
            if (board.isEmptyBoard()) {
                boardCleared = true
                boardBonus = rules.boardClearBonus
                boardClears++
            }
        } else {
            combo = 0
        }
        score += placementPoints + linePoints + boardBonus

        val trayRefilled = if (trayPieces.all { it == null }) {
            refillTray()
            true
        } else {
            false
        }
        isGameOver = !anyPieceFits()

        return MoveResult(
            trayIndex = index,
            shape = shape,
            row = row,
            col = col,
            placedCells = placedCells,
            clearedRows = fullRows,
            clearedCols = fullCols,
            clearedCells = clearedCells,
            placementPoints = placementPoints,
            linePoints = linePoints,
            boardClearBonus = boardBonus,
            combo = combo,
            praise = praise,
            boardCleared = boardCleared,
            trayRefilled = trayRefilled,
            gameOver = isGameOver,
            scoreAfter = score
        )
    }

    /**
     * The rewarded "continue": removes up to [CONTINUE_ROWS] random non-empty rows and, if the
     * current tray still cannot be placed, swaps it for pieces that fit. Allowed once per game.
     */
    fun useContinue(rowsToClear: Int = CONTINUE_ROWS): ContinueResult {
        check(!continueUsed) { "Continue was already used in this game" }
        val candidates = (0 until board.size).filter { board.rowFill(it) > 0 }.shuffled(rng)
        val rows = candidates.take(rowsToClear).sorted()
        val cleared = board.clearLines(rows, emptyList())
        combo = 0
        continueUsed = true
        var rerolled = false
        if (!anyPieceFits()) {
            val fresh = generator.fittingTray(board)
            for (i in 0 until TRAY_SIZE) trayPieces[i] = fresh[i]
            rerolled = true
        }
        isGameOver = !anyPieceFits()
        return ContinueResult(rows, cleared, rerolled, isGameOver)
    }

    private fun refillTray() {
        val pieces = generator.nextTray(board, TRAY_SIZE)
        for (i in 0 until TRAY_SIZE) trayPieces[i] = pieces[i]
    }

    fun snapshot(): GameSnapshot = GameSnapshot(
        board = board.toIntArray(),
        tray = trayPieces.map { it?.id },
        score = score,
        combo = combo,
        maxCombo = maxCombo,
        linesCleared = linesCleared,
        piecesPlaced = piecesPlaced,
        boardClears = boardClears,
        isGameOver = isGameOver,
        continueUsed = continueUsed
    )

    fun restore(s: GameSnapshot) {
        board.loadFrom(s.board)
        for (i in 0 until TRAY_SIZE) {
            trayPieces[i] = s.tray.getOrNull(i)?.let { Shapes.byId(it) }
        }
        score = s.score
        combo = s.combo
        maxCombo = s.maxCombo
        linesCleared = s.linesCleared
        piecesPlaced = s.piecesPlaced
        boardClears = s.boardClears
        continueUsed = s.continueUsed
        if (trayPieces.all { it == null }) refillTray()
        isGameOver = s.isGameOver || !anyPieceFits()
    }
}
