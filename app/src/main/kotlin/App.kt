import agent.expectimax.ExpectimaxAgent
import agent.human.HumanAgent
import agent.qlearning.QLearntAgent
import agent.random.RandomAgent
import game.GameManager

fun main(args: Array<String>) {

    /*val agent = ExpectimaxAgent(debug = true)
    val state = GameState(
        board = listOf(
            Square.EMPTY,
            Square.EMPTY,
            Square.EMPTY,
            Square.EMPTY,
            Square.EMPTY,
            Square.EMPTY,
            Square.EMPTY,
            Square.EMPTY,
            Square.EMPTY,
        ),
        hand = listOf(0, 0, 1, 1, 0), // 3, Joker
        deck = listOf(0, 3, 2, 2, 1)
    )

    println(state)

    val action = agent.getBestAction(state)
    println("Best: $action")
    action?.let { println(state.play(action, Square.MINE)) }
    println("Visited Nodes: ${DebugObject.visitedNode}")
    println("Cache Hits: ${DebugObject.cacheHits} (${DebugObject.cacheHits.toDouble() / DebugObject.visitedNode})") */

    /*GameManager.startGame(
        QLearntAgent(name = "Q-Learning Agent 1"),
        QLearntAgent(name = "Ex"),
        //ExpectimaxAgent(name = "Expectimax", debug = true),
    )*/

    // Gym().learnParallel()

    var win = 0;
    var lose = 0;
    var draw = 0;
    var targetAgent = 1;
    val agent1 = QLearntAgent(name = "QLearnt Agent 1")
    val agent2 = QLearntAgent(name = "QLearnt Agent 2", debug = true)
    val agent3 = ExpectimaxAgent(name = "Expectimax Agent 1", debug = false, maxDepth = 3)
    val agent4 = RandomAgent(name = "Random Agent 4")
    val agent5 = HumanAgent(name = "Human Agent 5")

    GameManager.startGame(agent2, agent5, silent = false)


    for (i in 1..10000) {
        val result = GameManager.startGame(
            agent3, agent1,
        )
        when (result) {
            targetAgent -> win++
            -1 -> draw++
            else -> lose++
        }
        if (i % 500 == 0) println("Iteration $i of 10000")
    }

    println("Win: $win, Draw: $draw, Lose: $lose (${String.format("%.2f", win.toDouble() / (win + lose) * 100)}%)")


}