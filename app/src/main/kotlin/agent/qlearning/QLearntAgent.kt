package agent.qlearning

import agent.Agent
import game.Action
import game.GameState
import kotlin.random.Random

class QLearntAgent(
    override val name: String = "QLearntAgent",
) : Agent {
    val qTable: MutableMap<GameState, MutableMap<Action, Double>> = QTableStorage.load()

    override fun decideMove(gameState: GameState): Action? {
        val actions = gameState.iterateAction()
        if (actions.isEmpty()) return null

        val policyActionMap = qTable[gameState] ?: return actions.random()

        val maxQ = policyActionMap.values.maxOrNull() ?: 0.0
        val bestActions = actions.filter { action -> (policyActionMap[action] ?: 0.0) >= maxQ }
        println(gameState)
        println("maxQ = ${String.format("%.4f", maxQ)}")

        return bestActions.random()
    }
}