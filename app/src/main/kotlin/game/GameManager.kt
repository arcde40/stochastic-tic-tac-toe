package game

import agent.Agent
import util.adjustElement

object GameManager {

    private val patterns = listOf(
        0b111_000_000,
        0b000_111_000,
        0b000_000_111,
        0b100_100_100,
        0b010_010_010,
        0b001_001_001,
        0b100_010_001,
        0b001_010_100
    )

    fun hasWonByBoard(board: List<Square>): GameStatus =
        when {
            filter(board.toBitString(Square.MINE)) -> GameStatus.WIN
            filter(board.toBitString(Square.OPPONENT)) -> GameStatus.LOSE
            board.any { it == Square.EMPTY } -> GameStatus.NOT_ENDED
            else -> GameStatus.DRAW
        }

    private fun filter(board: Int) = patterns.any { board and it == it }

    private fun List<Square>.toBitString(squareOption: Square) =
        fold(0) { v, entry ->
            ((v shl 1) or (if (entry == squareOption) 1 else 0))
        }

    fun startGame(agent1: Agent, agent2: Agent) {
        GameController(agent1, agent2).startGame()
    }

    fun rollProbUniform(deck: List<Int>) = rollProb(deck) { sum -> if (sum > 0) (0..<sum).random() else 0 }

    fun rollProb(deck: List<Int>, rand: (Int) -> Int): Int {
        val sum = deck.sum()
        var roll = rand(sum)

        deck.forEachIndexed { cardType, count ->
            if (count > 0) {
                if (roll < count) {
                    return cardType
                }
                roll -= count
            }
        }
        return 0
    }

    fun initializeGlobalState(drawSequence: List<Int>) =
        drawSequence.fold(GlobalGameState()) { state, seq -> state.drawCard(seq) }


    fun initializeGlobalState(drawSequence: List<Int>, globalGameState: GlobalGameState) =
        drawSequence.fold(globalGameState) { state, seq -> state.drawCard(seq) }


    fun GlobalGameState.drawCard(
        playerIdx: Int
    ) =
        rollProbUniform(deck).let { drawnCard ->
            require(deck[drawnCard] > 0)
            giveCardToPlayer(playerIdx, drawnCard)
        }

    fun GlobalGameState.giveCardToPlayer(playerIdx: Int, drawnCard: Int): GlobalGameState {
        require(drawnCard > 0)
        return copy(
            deck = deck.adjustElement(drawnCard, -1),
            hands = hands.mapIndexed { idx, originalHand ->
                if (idx == playerIdx) {
                    originalHand.adjustElement(drawnCard, 1)
                } else originalHand
            }
        )
    }


    fun applyAction(state: GlobalGameState, action: Action) =
        with(state) {
            val drawnCard = rollProbUniform(deck)
            play(action, drawnCard)
        }

}
