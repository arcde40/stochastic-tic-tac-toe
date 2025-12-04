package agent.random

import agent.Agent
import game.Action
import game.GameState

class RandomAgent(override val name: String = "RandomAgent") : Agent {
    override fun decideMove(gameState: GameState): Action? {
        return gameState.iterateAction().randomOrNull()
    }
}