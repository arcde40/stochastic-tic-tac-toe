package agent.qlearning

import agent.Agent
import game.Action
import game.GameState

class QLearningAgent(
    override val name: String,
    val epsilon: Double
) : Agent {

    // S -> (A, V)
    val qTable = mutableMapOf<GameState, MutableMap<Action, Double>>()

    override fun decideMove(gameState: GameState): Action? {
        val actions = gameState.iterateAction()
        
    }

    fun learn() {

    }


}