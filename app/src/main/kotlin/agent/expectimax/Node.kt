package agent.expectimax

import game.GameState
import game.GameStatus
import game.Square
import util.adjustElement

private const val WIN_VALUE = 1.0
private const val LOSE_VALUE = -1.0
private const val DRAW_VALUE = 0.0

sealed class Node(val state: GameState) {
    abstract fun go(memo: MutableMap<GameState, Double>): Double

    class Max(state: GameState) : Node(state) {
        override fun go(memo: MutableMap<GameState, Double>): Double {
            getCachedOrTerminalScore(memo)?.let { return it }
            val score = state.iterateAction().maxOfOrNull { action ->
                Chance(state.play(action, Square.MINE)).go(memo)
            } ?: LOSE_VALUE
            memo[state] = score
            return score
        }
    }

    class Min(state: GameState) : Node(state) {
        override fun go(memo: MutableMap<GameState, Double>): Double {
            getCachedOrTerminalScore(memo)?.let { return it }
            val deckCount = state.deck.sum()

            /**
             * There are 10 cards, only 9 of them will be used.
             * So at least there is 1 card left in the deck.
             */
            if (deckCount < 2) {
                val leftCard = state.deck.indexOfFirst { it > 0 }
                val newState = state.copy(hand = listOf(0, 0, 0, 0, 0).adjustElement(leftCard, 1))
                return newState.iterateAction().minOfOrNull { action ->
                    Max(
                        newState
                            .play(action, Square.OPPONENT)
                            .draw(action.card)
                            .copy(hand = state.hand)
                    ).go(memo)
                } ?: WIN_VALUE
            }

            // Generate all possible card combination
            val combinations = generateAllHandsCombination()

            val result = combinations.sumOf { (card1, card2) ->
                val count1 = state.deck[card1]
                val count2 = state.deck[card2]

                val combinationCount = when {
                    card1 == card2 -> count1 * (count1 - 1) / 2
                    else -> count1 * count2
                }

                if (combinationCount == 0) {
                    0.0
                } else {
                    val probability = combinationCount.toDouble() / (deckCount * (deckCount - 1) / 2).toDouble()
                    val opponentState =
                        state.copy(
                            hand = listOf(0, 0, 0, 0, 0)
                                .adjustElement(card1, 1)
                                .adjustElement(card2, 1)
                        )

                    val bestScoreForOpponent = opponentState.iterateAction().minOfOrNull { action ->
                        Max(
                            opponentState
                                .play(action, Square.OPPONENT)
                                .draw(action.card)
                                .copy(hand = state.hand)
                        ).go(
                            memo
                        )
                    } ?: WIN_VALUE

                    probability * bestScoreForOpponent
                }
            }

            memo[state] = result
            return result
        }
    }

    class Chance(state: GameState) : Node(state) {
        override fun go(memo: MutableMap<GameState, Double>): Double {
            getCachedOrTerminalScore(memo)?.let { return it }
            val deckCount = state.deck.sum()

            val result = state.deck.mapIndexed { cardType, cardCount ->
                if (cardCount == 0) 0.0
                else {
                    val probability = cardCount.toDouble() / deckCount.toDouble()
                    val nextState = state.draw(cardType)
                    probability * Min(nextState).go(memo)
                }
            }.sum()

            return result
        }
    }

    protected fun getCachedOrTerminalScore(memo: MutableMap<GameState, Double>): Double? {
        DebugObject.visitedNode++
        if (memo.containsKey(state)) {
            DebugObject.cacheHits++
            //return memo[state]
        }
        if (state.isGameEnded()) {
            return getTerminalValue(state.status)
        }
        return null
    }

    protected fun getTerminalValue(gameStatus: GameStatus) = when (gameStatus) {
        GameStatus.WIN -> WIN_VALUE
        GameStatus.LOSE -> LOSE_VALUE
        else -> DRAW_VALUE
    }

    protected fun generateAllHandsCombination() =
        (1..4).flatMap { first -> (first..4).map { second -> first to second } }
}
