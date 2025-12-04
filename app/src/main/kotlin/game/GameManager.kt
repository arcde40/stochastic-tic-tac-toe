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
        val deck = mutableListOf(0, 3, 3, 3, 1);

        val agent1Hand = listOf(0, 0, 0, 0, 0)
            .adjustElement(rollProbUniform(deck).also { deck[it]-- }, 1)
            .adjustElement(rollProbUniform(deck).also { deck[it]-- }, 1)

        val agent2Hand = listOf(0, 0, 0, 0, 0)
            .adjustElement(rollProbUniform(deck).also { deck[it]-- }, 1)
            .adjustElement(rollProbUniform(deck).also { deck[it]-- }, 1)

        val agent1State = GameState(
            board = (1..9).map { Square.EMPTY },
            hand = agent1Hand,
            deck = deck.toList(),
        )

        val agent2State = GameState(
            board = (1..9).map { Square.EMPTY },
            hand = agent2Hand,
            deck = deck.toList(),
        )

        GameController(agent1, agent2).startGame(agent1State, agent2State)
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
}
