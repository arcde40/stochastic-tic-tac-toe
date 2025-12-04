package game

import util.adjustElement

private const val EMPTY = -1

data class GlobalGameState(
    val deck: List<Int> = listOf(0, 3, 3, 3, 1),
    val hands: List<List<Int>> = listOf(listOf(0, 0, 0, 0, 0), listOf(0, 0, 0, 0, 0)),
    val board: List<Int> = (0..8).map { -1 },
    val currentPlayer: Int = 0
) {
    val status: GameStatus
        get() = GameManager.hasWonByBoard(board.toSquareBoard())

    fun play(action: Action, drawnCard: Int): GlobalGameState {
        assert(getHandOf(currentPlayer)[action.card] > 0) { "Player doesn't have the card!" }
        assert(board[action.positionIdx] == EMPTY) { "Target square is not empty!" }
        assert(deck[drawnCard] > 0) { "Deck is not sufficient!" }

        val newHand = getHandOf(currentPlayer)
            .adjustElement(action.card, -1)
            .let {
                if (drawnCard > 0) it.adjustElement(drawnCard, 1) else it
            }

        return copy(
            deck = deck.adjustElement(drawnCard, -1),
            hands = hands.mapIndexed { idx, originalHand ->
                if (idx == currentPlayer) newHand else originalHand
            },
            board = board.adjustElement(action.positionIdx, currentPlayer + 1),
            currentPlayer = getOpponentIndex()
        )

    }

    private fun getHandOf(playerIndex: Int): List<Int> = hands[playerIndex]

    private fun getDeckWithOpponentHand() =
        deck.zip(getHandOf((currentPlayer + 1) % 2), Int::plus)

    fun toState(): GameState {
        return GameState(
            deck = getDeckWithOpponentHand(),
            hand = getHandOf(currentPlayer),
            board = board.toSquareBoard()
        )
    }

    private fun getOpponentIndex() = (currentPlayer + 1) % 2

    private fun List<Int>.toSquareBoard() =
        map {
            when (it) {
                EMPTY -> Square.EMPTY
                currentPlayer -> Square.MINE
                else -> Square.OPPONENT
            }
        }

    fun isGameEnded() = status.isEnded
}
