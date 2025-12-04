package game

import agent.Agent

class GameController(
    val agent1: Agent,
    val agent2: Agent,
) {
    fun startGame() {
        val drawSequence = listOf(0, 1, 0, 1)
        val initialState = GameManager.initializeGlobalState(drawSequence)

        val finalState = gameLoop(initialState)

        if (finalState.status == GameStatus.WIN) {
            println("${finalState.getCurrentAgent().name} Win!")
        } else if (finalState.status == GameStatus.LOSE) {
            println("${finalState.getOpponentAgent().name} Win!")
        } else println("Draw!")
    }

    private tailrec fun gameLoop(state: GlobalGameState): GlobalGameState {
        if (state.isGameEnded()) return state

        val action = state.getCurrentAgent().decideMove(state.toState())
        if (action == null) {
            println("${state.getCurrentAgent().name} Resigned.")
            return state
        }

        println("${state.getCurrentAgent().name} -> $action")

        val newState = GameManager.applyAction(state, action)
        return gameLoop(newState)
    }


    private fun GlobalGameState.getCurrentAgent() =
        if (currentPlayer == 0) agent1 else agent2


    private fun GlobalGameState.getOpponentAgent() =
        if (currentPlayer == 0) agent2 else agent1

}