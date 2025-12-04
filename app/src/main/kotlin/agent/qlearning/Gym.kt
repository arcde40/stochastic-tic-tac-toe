package agent.qlearning

import agent.Agent
import agent.random.RandomAgent
import game.Action
import game.GameManager
import game.GameState
import game.GameStatus

private const val EPISODES = 1_000_000
private const val EPSILON_DECAY = 0.999995

class Gym {
    private var win = 0
    private var lose = 0
    private var draw = 0

    private var learnerIndex = 1

    fun learn() {
        val qAgent = QLearningAgent(epsilon = 0.9)
        val opponent: Agent = RandomAgent()
        println("Running $EPISODES episodes")
        for (episode in 0..EPISODES) {
            runGame(listOf(opponent, qAgent))

            if (qAgent.epsilon > 0.1) qAgent.epsilon *= EPSILON_DECAY
            if (episode % 50000 == 0) {
                println(
                    "Episode $episode | Win: $win, lose: $lose, Draw: $draw (${
                        String.format(
                            "%.2f",
                            (win).toDouble() / (win + lose + draw) * 100
                        )
                    }%)"
                )
                println("Epsilon: ${String.format("%.3f", qAgent.epsilon)}, Q-Size: ${qAgent.qTable.size}")
                win = 0; lose = 0; draw = 0;
            }
        }
        println("Learning done! Saving Q-Table...")
    }

    fun runGame(agents: List<Agent>) {
        val drawSequence = listOf(0, 1, 0, 1)
        val initialState = GameManager.initializeGlobalState(drawSequence)

        var state = initialState
        var lastStates = arrayOfNulls<GameState>(2)
        var lastActions = arrayOfNulls<Action>(2)

        while (!state.isGameEnded()) {
            val currentPlayer = state.currentPlayer
            val currentAgent = agents[currentPlayer]

            val viewState = state.toState()
            val action = currentAgent.decideMove(viewState) ?: break

            // Delayed Reward (Rewarding 0.0 if game is not ended)
            if (currentAgent is QLearningAgent) {
                val lastState = lastStates[currentPlayer]
                val lastAction = lastActions[currentPlayer]

                if (lastState != null && lastAction != null) {
                    currentAgent.learn(lastState, lastAction, 0.0, viewState, false)
                }

                lastStates[currentPlayer] = viewState
                lastActions[currentPlayer] = action
            }

            val nextState = GameManager.applyAction(state, action)

            // Game Reward
            if (nextState.isGameEnded()) {
                val result = nextState.status
                if (currentAgent is QLearningAgent && result == GameStatus.WIN) {
                    currentAgent.learn(
                        viewState,
                        action,
                        1.0,
                        nextState.toState(),
                        true
                    )
                }

                val loserIdx = (currentPlayer + 1) % 2
                val loserAgent = agents[loserIdx]
                if (loserAgent is QLearningAgent && result == GameStatus.WIN) {
                    val prevState = lastStates[loserIdx]
                    val prevAction = lastActions[loserIdx]
                    if (prevState != null && prevAction != null) {
                        loserAgent.learn(prevState, prevAction, -1.0, nextState.toState(loserIdx), false)
                    }
                }

                when (nextState.toState(learnerIndex).status) {
                    GameStatus.WIN -> win++
                    GameStatus.LOSE -> lose++
                    GameStatus.DRAW -> draw++
                    else -> {}
                }

            }
            state = nextState
        }

    }

}
