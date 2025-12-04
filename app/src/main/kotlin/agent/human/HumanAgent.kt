package agent.human

import agent.Agent
import game.Action
import game.GameState
import java.util.*

class HumanAgent(
    override val name: String = "Human"
) : Agent {
    private val scanner = Scanner(System.`in`)

    override fun decideMove(gameState: GameState): Action {
        println("\n\n\nYour Turn!")
        println(gameState)
        println("- Input Action (Ex. 2 8)")
        while (true) {
            try {
                val input = scanner.nextLine().split(" ")
                if (input.size != 2) throw IllegalArgumentException()

                val card = input[0].toInt()
                val pos = input[1].toInt()
                val action = Action(card, pos)

                if (gameState.iterateAction().none { it == action }) throw IllegalArgumentException()
                return action

            } catch (e: IllegalArgumentException) {
                println("Wrong Input. Please enter a valid action.")
            }

        }


    }
}