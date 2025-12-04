package agent.qlearning

import agent.Agent
import game.GameManager
import game.GlobalGameState

private const val episodes = 200_000

class Gym {
    fun learn() {
        val qAgent: QLearningAgent
        val opponent: Agent

        for (episode in 0..episodes) {
            gameLoop(GameManager.initializeGlobalState(listOf(0, 1, 0, 1)))
        }
    }

    fun gameLoop(state: GlobalGameState) {

    }
}