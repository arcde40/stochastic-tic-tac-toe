import agent.human.HumanAgent
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

    GameManager.startGame(
        HumanAgent(),
        RandomAgent(name = "Dumb"),
        //ExpectimaxAgent(name = "Expectimax", debug = true),
    )
}