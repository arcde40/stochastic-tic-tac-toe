package agent.qlearning

import agent.Agent
import agent.expectimax.ExpectimaxAgent
import agent.random.RandomAgent
import game.Action
import game.GameManager
import game.GameState
import game.GameStatus
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

private const val EPISODES = 10_000_000
private const val EPSILON_DECAY = 0.99995
private const val THREADS = 6

class Gym {
    val win = AtomicInteger(0)
    val lose = AtomicInteger(0)
    val draw = AtomicInteger(0)
    val progress = AtomicInteger(0)

    private var learnerIndex = 1

    private val qAgent = QLearningAgent(epsilon = 0.9, gamma = 0.9, alpha = 0.1)
    private val randomAgent = RandomAgent()
    private val expectimaxAgent = ExpectimaxAgent(name = "", debug = false, maxDepth = 2)

    fun learnParallel() {
        runBlocking {
            val jobs = List(THREADS) { threadId ->
                launch(Dispatchers.Default) {
                    learn(EPISODES / THREADS)
                }
            }
            val monitorJob = launch {
                try {
                    var lastTimeMillis = System.currentTimeMillis()
                    var lastCheckedProgress = 0
                    println("Running $EPISODES episodes in parallel")
                    while (isActive) {
                        delay(500)
                        if (progress.get() - lastCheckedProgress > 50000) {
                            println(
                                "Episode $progress | Win: $win, lose: $lose, Draw: $draw (${
                                    String.format(
                                        "%.2f",
                                        (win.get()).toDouble() / (win.get() + lose.get()) * 100
                                    )
                                }%)"
                            )
                            println("Epsilon: ${String.format("%.3f", qAgent.epsilon)}, Q-Size: ${qAgent.qTable.size}")
                            println(
                                "Took ${System.currentTimeMillis() - lastTimeMillis}ms (${
                                    String.format(
                                        "%.2f",
                                        progress.get().toDouble() / EPISODES * 100
                                    )
                                }%)"
                            )
                            lastCheckedProgress = progress.get()
                            win.set(0)
                            lose.set(0)
                            draw.set(0)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            jobs.joinAll()
            monitorJob.cancelAndJoin()

            println(
                "Training Complete: Win: $win, Lose: $lose, Draw: $draw (${
                    String.format(
                        "%.2f",
                        win.get().toDouble() / (win.get() + lose.get()) * 100
                    )
                }%)"
            )

            QTableStorage.save(qAgent.qTable)
        }
    }


    fun learn(episodes: Int) {

        for (i in 1..episodes) {
            val localProgress = progress.incrementAndGet().toDouble() / EPISODES
            val rand = Random.nextDouble()
            val opponent = when {
                rand < localProgress / 2 -> qAgent
                rand < localProgress -> expectimaxAgent
                else -> randomAgent
            }

            learnerIndex = (0..1).random()
            runGame(if (learnerIndex == 0) listOf(qAgent, opponent) else listOf(opponent, qAgent))

            if (qAgent.epsilon > 0.1) if (i % 10 == 0) qAgent.epsilon *= EPSILON_DECAY

        }
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
            val action = currentAgent.decideMove(viewState)
            if (action == null) {

                val lastState = lastStates[currentPlayer]
                val lastAction = lastActions[currentPlayer]

                if (currentAgent is QLearningAgent) {
                    currentAgent.learn(
                        lastState!!,
                        lastAction!!,
                        -1.0,
                        state.toState(),
                        true
                    )
                }

                val winnerIdx = (currentPlayer + 1) % 2
                val winnerAgent = agents[winnerIdx]
                val winnerState = state.toState(winnerIdx)
                if (winnerAgent is QLearningAgent) {
                    val prevState = lastStates[winnerIdx]
                    val prevAction = lastActions[winnerIdx]
                    if (prevState != null && prevAction != null) {
                        winnerAgent.learn(prevState, prevAction, 1.0, winnerState, true)
                    }
                }
                if (winnerIdx == learnerIndex) win.getAndIncrement()
                else lose.getAndIncrement()
                break
            }

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
                    GameStatus.WIN -> win.getAndIncrement()
                    GameStatus.LOSE -> lose.getAndIncrement()
                    GameStatus.DRAW -> draw.getAndIncrement()
                    else -> {
                        if (!nextState.toState((learnerIndex + 1) % 2).status.isEnded)
                            throw AssertionError("Status cannot be NOT_ENDED")
                    }
                }

            }
            state = nextState
        }

    }

}
