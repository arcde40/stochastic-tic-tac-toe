package agent.qlearning

import agent.Agent
import agent.random.RandomAgent
import game.Action
import game.GameManager
import game.GameState
import game.GameStatus
import kotlin.random.Random

private const val EPISODES = 10_000_000
private const val EPSILON_DECAY = 0.99995

class Gym {
    private var win = 0
    private var lose = 0
    private var draw = 0

    private var learnerIndex = 1

    fun learn() {
        val qAgent = QLearningAgent(epsilon = 0.9, gamma = 0.9, alpha = 0.1)
        val randomAgent = RandomAgent()
        println("Running $EPISODES episodes")
        for (episode in 0..EPISODES) {
            val progress = episode.toDouble() / EPISODES
            val opponent = if (Random.nextDouble() < progress) qAgent else randomAgent

            learnerIndex = (0..1).random()
            runGame(if (learnerIndex == 0) listOf(qAgent, opponent) else listOf(opponent, qAgent))

            if (qAgent.epsilon > 0.1) qAgent.epsilon *= EPSILON_DECAY
            if (episode % 50000 == 0) {
                println(
                    "Episode $episode | Win: $win, lose: $lose, Draw: $draw (${
                        String.format(
                            "%.2f",
                            (win).toDouble() / (win + lose) * 100
                        )
                    }%)"
                )
                println("Epsilon: ${String.format("%.3f", qAgent.epsilon)}, Q-Size: ${qAgent.qTable.size}")
                win = 0; lose = 0; draw = 0;
            }
        }
        println("Learning done! Saving Q-Table...")
        QTableStorage.save(qAgent.qTable)

    }

    fun runGame(agents: List<Agent>) {
        val drawSequence = listOf(0, 1, 0, 1)
        val initialState =
            GameManager.initializeGlobalState(drawSequence)//, GlobalGameState().giveCardToPlayer(playerIdx = 1, 4))


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
                val result = nextState.toState()
                if (currentAgent is QLearningAgent && result.status == GameStatus.WIN) {
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
                val loserState = nextState.toState(loserIdx)
                if (loserAgent is QLearningAgent && loserState.status == GameStatus.LOSE) {
                    val prevState = lastStates[loserIdx]
                    val prevAction = lastActions[loserIdx]
                    if (prevState != null && prevAction != null) {
                        loserAgent.learn(prevState, prevAction, -1.0, nextState.toState(loserIdx), true)
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
