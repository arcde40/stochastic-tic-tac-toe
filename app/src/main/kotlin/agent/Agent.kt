package agent

import game.Action
import game.GameState

interface Agent {
    val name: String
    fun decideMove(gameState: GameState): Action?
}