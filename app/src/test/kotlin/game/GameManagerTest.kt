package game

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class GameManagerTest : FunSpec({

    context("Agent Wins") {
        withData(
            nameFn = { "Pattern: $it" },
            "OOO......",
            "...OOO...",
            "......OOO",
            "O..O..O..",
            ".O..O..O.",
            "..O..O..O",
            "O...O...O",
            "..O.O.O.."
        ) {
            val board = it.toBoard()
            GameManager.hasWonByBoard(board) shouldBe GameStatus.WIN
        }
    }

    context("Agent Loses") {
        withData(
            nameFn = { "Pattern: $it" },
            "XXX......",
            "...XXX...",
            "......XXX",
            "X..X..X..",
            ".X..X..X.",
            "..X..X..X",
            "X...X...X",
            "..X.X.X.."
        ) {
            val board = it.toBoard()
            GameManager.hasWonByBoard(board) shouldBe GameStatus.LOSE
        }
    }

    context("Game not ended") {
        withData(
            nameFn = { "Pattern $it" },
            "..X..O..X",
            "XXOOOX..."
        ) {
            val board = it.toBoard()
            GameManager.hasWonByBoard(board) shouldBe GameStatus.DRAW
        }
    }

    context("Edge Cases & Scenario") {
        withData(
            nameFn = { "Pattern $it" },
            "XXOOOXXOX",
            "XOXXOOOXX",
            "OXXXOOXOX",
            "XOXOXXOXO",
            "OXOOXXXOX"
        ) {
            GameManager.hasWonByBoard(
                it.toBoard()
            ) shouldBe GameStatus.DRAW
        }
        test("Empty Board") {
            GameManager.hasWonByBoard(
                ".........".toBoard()
            ) shouldBe GameStatus.DRAW
        }

        test("Draw") {
            GameManager.hasWonByBoard(
                "XOXOXOOXO".toBoard()
            ) shouldBe GameStatus.DRAW
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