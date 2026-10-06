package com.blockcrush.puzzle.logic

/** Turns a [GameSnapshot] into a compact string for SharedPreferences and back. */
object GameStateCodec {
    private const val VERSION = 1
    private const val SEPARATOR = "|"

    fun encode(s: GameSnapshot): String = listOf(
        VERSION.toString(),
        s.score.toString(),
        s.combo.toString(),
        s.maxCombo.toString(),
        s.linesCleared.toString(),
        s.piecesPlaced.toString(),
        s.boardClears.toString(),
        if (s.isGameOver) "1" else "0",
        if (s.continueUsed) "1" else "0",
        s.tray.joinToString(",") { it ?: "-" },
        s.board.joinToString("")
    ).joinToString(SEPARATOR)

    /** Returns null for anything that is not a valid snapshot written by [encode]. */
    fun decode(text: String?): GameSnapshot? {
        if (text.isNullOrBlank()) return null
        return try {
            val parts = text.split(SEPARATOR)
            if (parts.size != 11 || parts[0].toInt() != VERSION) return null
            val tray = parts[9].split(",").map { if (it == "-") null else it }
            if (tray.size != GameEngine.TRAY_SIZE) return null
            if (tray.any { it != null && Shapes.byId(it) == null }) return null
            val boardText = parts[10]
            if (boardText.length != Board.SIZE * Board.SIZE) return null
            val board = IntArray(boardText.length)
            for (i in boardText.indices) {
                val v = boardText[i] - '0'
                if (v < 0 || v > Shapes.COLOR_COUNT) return null
                board[i] = v
            }
            GameSnapshot(
                board = board,
                tray = tray,
                score = parts[1].toInt(),
                combo = parts[2].toInt(),
                maxCombo = parts[3].toInt(),
                linesCleared = parts[4].toInt(),
                piecesPlaced = parts[5].toInt(),
                boardClears = parts[6].toInt(),
                isGameOver = parts[7] == "1",
                continueUsed = parts[8] == "1"
            )
        } catch (e: NumberFormatException) {
            null
        }
    }
}
