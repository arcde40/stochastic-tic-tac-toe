package agent.qlearning

import agent.Agent
import game.Action
import game.GameState
import kotlin.random.Random

class QLearningAgent(
    override val name: String = "QLearningAgent",
    var epsilon: Double,
    val alpha: Double = 0.1,
    val gamma: Double = 0.9,
) : Agent {

    // S -> (A, V)
    val qTable = mutableMapOf<GameState, MutableMap<Action, Double>>()

    override fun decideMove(gameState: GameState): Action? {
        val actions = gameState.iterateAction()
        if (actions.isEmpty()) return null

        if (Random.nextDouble() < epsilon) {
            return actions.random()
        }

        val policyActionMap = qTable[gameState] ?: return actions.random()

        val maxQ = policyActionMap.values.maxOrNull() ?: 0.0
        val bestActions = actions.filter { action -> (policyActionMap[action] ?: 0.0) >= maxQ }

        return bestActions.random()
    }

    fun learn(lastState: GameState, action: Action, reward: Double, nextState: GameState, gameEnded: Boolean) {
        val qValue = qTable.getOrPut(lastState) { mutableMapOf() }
        val currentQ = qValue[action] ?: 0.0
        val nextQ = if (gameEnded) {
            0.0
        } else {
            val nextQValues = qTable[nextState]
            nextQValues?.values?.maxOrNull() ?: 0.0
        }

        val newQ = currentQ + alpha * (reward + (gamma * nextQ) - currentQ)
        qValue[action] = newQ
    }
}
