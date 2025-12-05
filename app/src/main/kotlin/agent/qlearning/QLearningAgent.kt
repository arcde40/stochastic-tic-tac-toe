package agent.qlearning

import agent.Agent
import game.Action
import game.GameState
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class QLearningAgent(
    override val name: String = "QLearningAgent",
    var epsilon: Double,
    val alpha: Double = 0.1,
    val gamma: Double = 0.9,
) : Agent {

    // S -> (A, V)
    val qTable = ConcurrentHashMap<GameState, ConcurrentHashMap<Action, Double>>()

    override fun decideMove(gameState: GameState): Action? {
        val actions = gameState.iterateAction()
        if (actions.isEmpty()) return null

        if (Random.nextDouble() < epsilon) {
            return actions.random()
        }

        val policyActionMap = qTable[gameState] ?: return actions.random()


        val currentScores = actions.associateWith { policyActionMap[it] ?: 0.0 }
        val maxQ = currentScores.values.maxOrNull() ?: 0.0

        // 최고 점수를 가진 행동들만 추림
        val bestActions = currentScores.filter { it.value == maxQ }.keys

        // [안전 장치] 혹시라도 비었으면(그럴 리 없지만) 전체 랜덤 반환
        return bestActions.randomOrNull() ?: actions.random()
    }

    fun learn(lastState: GameState, action: Action, reward: Double, nextState: GameState, gameEnded: Boolean) {
        val qValue = qTable.computeIfAbsent(lastState) { ConcurrentHashMap() }
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
