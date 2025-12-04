package agent.expectimax

import agent.Agent
import game.Action
import game.GameState
import game.Square

class ExpectimaxAgent(
    private val debug: Boolean,
    override val name: String
) : Agent {
    private val memo = mutableMapOf<GameState, Double>()

    override fun decideMove(gameState: GameState): Action? {
        memo.clear()
        DebugObject.reset()
        val actions = gameState.iterateAction()

        if (actions.isEmpty()) {
            if (debug) {
                println("No actions were found")
            }
            return null
        }

        val scores = actions.map { action ->
            val nextState = gameState.play(action, Square.MINE)
            val score = Node.Chance(nextState).go(memo)
            action to score
        }

        if (debug) {
            println(scores)
            println(gameState)
            val maxAction = scores.maxBy { it.second }
            println("${maxAction.first} - ${maxAction.second}")
            println("Visited ${DebugObject.visitedNode}")
        }

        return scores.maxByOrNull { it.second }?.first
    }
}