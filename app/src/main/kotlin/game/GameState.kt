package game

import util.adjustElement

enum class Square {
    EMPTY, MINE, OPPONENT
}

data class Action(
    val card: Int,
    val positionIdx: Int
) {
    override fun toString(): String {
        val cardName = if (card == 4) "J" else "$card"
        val row = positionIdx / 3
        val col = positionIdx % 3
        return "[$cardName] -> ($row, $col)"
    }
}

data class GameState(
    val board: List<Square>,
    val hand: List<Int>,
    val deck: List<Int>
) {
    val status: GameStatus
        get() = GameManager.hasWonByBoard(board)

    fun isGameEnded() = status.isEnded || iterateAction().isEmpty()

    fun iterateAction() =
        hand.mapIndexed { cardType, cardCount -> cardType to cardCount }
            .filter { it.second > 0 }
            .flatMap { (cardType, _) ->
                board.indices
                    .filter { idx -> isValidMove(cardType, idx) }
                    .map { idx -> Action(cardType, idx) }
            }

    private fun isValidMove(card: Int, positionIdx: Int): Boolean {
        if (board[positionIdx] != Square.EMPTY) return false
        return card == 4 || positionIdx / 3 == (card - 1) || (positionIdx + 1) % 3 == card % 3
    }

    /**
     * card: Number Card (1~3), Joker Card (4)
     * square: target square index of the board (0-indexed)
     */
    fun play(action: Action, square: Square) =
        copy(
            board = board.mapIndexed { idx, entry -> if (idx == action.positionIdx) square else entry },
            hand = hand.adjustElement(action.card, -1)
        )


    fun draw(drawnCard: Int) =
        copy(
            board = board,
            hand = hand.adjustElement(drawnCard, 1),
            deck = deck.adjustElement(drawnCard, -1)
        )

    override fun toString(): String {
        val sb = StringBuilder()
        sb.appendLine("\n===== Game State =====")

        // 1. 보드 그리기 (ASCII Art)
        for (row in 0..2) {
            sb.append("  ") // 들여쓰기
            for (col in 0..2) {
                val idx = row * 3 + col
                val symbol = when (board[idx]) {
                    Square.MINE -> "O" // 또는 " O "
                    Square.OPPONENT -> "X" // 또는 " X "
                    Square.EMPTY -> "." // 또는 " . "
                }
                sb.append("$symbol ")
            }
            sb.append("\n")
        }

        // 2. 내 손패 요약 (Count -> List 변환)
        val handStr = hand.formatCards()
        sb.appendLine("* Hand: $handStr")

        // 3. 남은 덱 요약
        val deckStr = deck.formatCards()
        sb.appendLine("* Deck: $deckStr")

        return sb.toString()
    }

    // 헬퍼: [0, 0, 1, 1] -> "Card 2(x1), Card 3(x1)" 형태로 변환
    private fun List<Int>.formatCards(): String {
        val items = this.mapIndexedNotNull { idx, count ->
            if (idx > 0 && count > 0) {
                val name = if (idx == 4) "Joker" else "$idx"
                "$name(x$count)"
            } else null
        }
        return if (items.isEmpty()) "(Empty)" else items.joinToString(", ")
    }
}
