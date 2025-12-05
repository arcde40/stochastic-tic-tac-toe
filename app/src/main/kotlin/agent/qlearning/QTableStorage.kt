package agent.qlearning

import game.Action
import game.GameState
import game.Square
import java.io.File

object QTableStorage {
    fun save(qTable: Map<GameState, MutableMap<Action, Double>>, fileName: String = "q_table.txt") {
        val file = File(fileName)
        println("💾 Saving Q-Table to ${file.absolutePath}...")

        file.bufferedWriter().use { writer ->
            qTable.forEach { (state, actions) ->
                // 1. Key (State) 직렬화
                val stateStr = serializeState(state)

                // 2. Value (Actions) 직렬화
                val actionStr = actions.entries.joinToString(",") { (action, score) ->
                    "${action.card}:${action.positionIdx}:$score"
                }

                // 3. 파일 쓰기
                writer.write("$stateStr=$actionStr")
                writer.newLine()
            }
        }
        println("✅ Saved ${qTable.size} states.")
    }

    // 불러오기
    fun load(fileName: String = "q_table.txt"): MutableMap<GameState, MutableMap<Action, Double>> {
        val file = File(fileName)
        val qTable = mutableMapOf<GameState, MutableMap<Action, Double>>()

        if (!file.exists()) {
            println("⚠️ File not found: $fileName. Starting with empty Q-Table.")
            return qTable
        }

        println("📂 Loading Q-Table from ${file.name}...")

        file.forEachLine { line ->
            if (line.isNotBlank()) {
                val parts = line.split("=")
                if (parts.size == 2) {
                    val stateKey = deserializeState(parts[0])
                    val actionMap = mutableMapOf<Action, Double>()

                    parts[1].split(",").forEach { entry ->
                        val (card, pos, score) = entry.split(":")
                        actionMap[Action(card.toInt(), pos.toInt())] = score.toDouble()
                    }
                    qTable[stateKey] = actionMap
                }
            }
        }
        println("✅ Loaded ${qTable.size} states.")
        return qTable
    }

    private fun serializeState(state: GameState): String {
        // Board: Square Enum -> 0, 1, 2
        val b = state.board.joinToString("") {
            when (it) {
                Square.EMPTY -> "0"
                Square.MINE -> "1"
                Square.OPPONENT -> "2"
            }
        }
        // Hand: List<Int> -> "01010" (구분자 없이 붙임, 한 자리수라 가정)
        val h = state.hand.joinToString("")
        // Deck: List<Int> -> "02210"
        val d = state.deck.joinToString("")

        return "$b|$h|$d"
    }

    // "00120...|01010|02210" -> GameState
    private fun deserializeState(str: String): GameState {
        val (bStr, hStr, dStr) = str.split("|")

        val board = bStr.map {
            when (it) {
                '1' -> Square.MINE
                '2' -> Square.OPPONENT
                else -> Square.EMPTY
            }
        }

        // 문자열 하나씩 끊어서 숫자로 변환
        val hand = hStr.map { it.toString().toInt() }
        val deck = dStr.map { it.toString().toInt() }

        return GameState(board, hand, deck)
    }
}