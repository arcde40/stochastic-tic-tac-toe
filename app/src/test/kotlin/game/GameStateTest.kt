package game

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class GameStateTest : FunSpec({

    context("starts from empty board") {
        test("Hand is 1, 2") {
            val state = GameState(
                board = ".........".toBoard(),
                hand = listOf(0, 1, 1, 0, 0),
                deck = listOf(0, 2, 2, 3, 1),
            )
            state.iterateAction() shouldContainExactlyInAnyOrder listOf(
                Action(1, 0),
                Action(1, 1),
                Action(1, 2),
                Action(1, 3),
                Action(1, 6),

                Action(2, 1),
                Action(2, 3),
                Action(2, 4),
                Action(2, 5),
                Action(2, 7),
            )
        }

        test("Hand is 3, J") {
            val state = GameState(
                board = ".........".toBoard(),
                hand = listOf(0, 0, 0, 1, 1),
                deck = listOf(0, 3, 3, 2, 0),
            )
            state.iterateAction() shouldContainExactlyInAnyOrder listOf(
                Action(3, 2),
                Action(3, 5),
                Action(3, 6),
                Action(3, 7),
                Action(3, 8),

                Action(4, 0),
                Action(4, 1),
                Action(4, 2),
                Action(4, 3),
                Action(4, 4),
                Action(4, 5),
                Action(4, 6),
                Action(4, 7),
                Action(4, 8),
            )
        }
    }

    context("starts from row 1, column 1 filled") {
        test("Hand is 1, 3") {
            val state = GameState(
                board = "XXXX..X..".toBoard(),
                hand = listOf(0, 1, 0, 1, 0),
                deck = listOf(0, 2, 3, 2, 1),
            )
            state.iterateAction() shouldContainExactlyInAnyOrder listOf(
                Action(3, 5),
                Action(3, 7),
                Action(3, 8),
            )
        }
        test("Hand is 2, J") {
            val state = GameState(
                board = "XXXX..X..".toBoard(),
                hand = listOf(0, 0, 1, 0, 1),
                deck = listOf(0, 3, 2, 3, 0),
            )
            state.iterateAction() shouldContainExactlyInAnyOrder listOf(
                Action(2, 4),
                Action(2, 5),
                Action(2, 7),

                Action(4, 4),
                Action(4, 5),
                Action(4, 7),
                Action(4, 8),
            )
        }
    }
})

private fun String.toBoard() = mapNotNull { value ->
    when (value) {
        'O' -> Square.MINE
        'X' -> Square.OPPONENT
        '.' -> Square.EMPTY
        else -> null
    }
}