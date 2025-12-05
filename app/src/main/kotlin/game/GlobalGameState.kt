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
        get() = if (toState().iterateAction()
                .isEmpty()
        ) {
            GameStatus.LOSE
        } else {
            GameManager.hasWonByBoard(board.toSquareBoard())
        }

    fun play(action: Action, drawnCard: Int): GlobalGameState {
        require(getHandOf(currentPlayer)[action.card] > 0) { "Player doesn't have the card!" }
        require(board[action.positionIdx] == EMPTY) { "Target square is not empty!" }
        if (drawnCard > 0) require(deck[drawnCard] > 0) { "Deck is not sufficient! $drawnCard" }
        require(action.card > 0)
        require(action.positionIdx in 0..<9)

        val newDeck = if (drawnCard > 0) deck.adjustElement(drawnCard, -1) else deck

        val newHand = getHandOf(currentPlayer)
            .adjustElement(action.card, -1)
            .let {
                if (drawnCard > 0) it.adjustElement(drawnCard, 1) else it
            }

        return copy(
            deck = newDeck,
            hands = hands.mapIndexed { idx, originalHand ->
                if (idx == currentPlayer) newHand else originalHand
            },
            board = board.adjustElement(action.positionIdx, currentPlayer + 1),
            currentPlayer = getOpponentIndex()
        )
    }

    private fun getHandOf(playerIndex: Int): List<Int> = hands[playerIndex]

    private fun getDeckWithOpponentHand(playerIndex: Int = currentPlayer) =
        deck.zip(getHandOf((playerIndex + 1) % 2), Int::plus)

    fun toState(): GameState {
        return GameState(
            deck = getDeckWithOpponentHand(),
            hand = getHandOf(currentPlayer),
            board = board.toSquareBoard()
        )
    }

    fun toState(playerIndex: Int): GameState {
        return GameState(
            deck = getDeckWithOpponentHand(playerIndex),
            hand = getHandOf(playerIndex),
            board = board.toSquareBoard(playerIndex)
        )
    }

    private fun getOpponentIndex() = (currentPlayer + 1) % 2

    private fun List<Int>.toSquareBoard(playerIndex: Int = currentPlayer) =
        map {
            when (it) {
                EMPTY -> Square.EMPTY
                playerIndex -> Square.MINE
                else -> Square.OPPONENT
            }
        }

    fun isGameEnded() = status.isEnded
}
