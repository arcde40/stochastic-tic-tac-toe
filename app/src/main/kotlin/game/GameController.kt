package game

import agent.Agent

class GameController(
    val agent1: Agent,
    val agent2: Agent,
) {
    fun startGame(agent1State: GameState, agent2State: GameState) {
        var turnCount = 1

        val realDeck = mutableListOf(0, 3, 3, 3, 1)
        agent1State.hand.forEachIndexed { index, hand ->
            realDeck[index] -= hand
        }
        agent2State.hand.forEachIndexed { index, hand ->
            realDeck[index] -= hand
        }
        var currentState = agent1State.copy(deck = realDeck.mapIndexed { idx, value -> value + agent2State.hand[idx] })
        var opponentState =
            agent2State.copy(deck = realDeck.mapIndexed { idx, value -> value + agent1State.hand[idx] })


        while (!currentState.isGameEnded()) {
            val currentPlayer = if (turnCount % 2 == 1) agent1 else agent2
            val action = currentPlayer.decideMove(currentState) ?: break
            println("${currentPlayer.name} : $action")

            val drawCard = GameManager.rollProbUniform(currentState.deck)

            currentState = currentState.play(action, Square.MINE)
            if (drawCard > 0) {
                currentState = currentState.draw(drawCard)
                realDeck[drawCard]--
            }
            val swap = currentState.copy()
            currentState =
                opponentState.copy(
                    board = currentState.board.invert(),
                    hand = opponentState.hand,
                    deck = realDeck.mapIndexed { idx, value -> value + currentState.hand[idx] },
                )
            opponentState = swap
            turnCount++
            println("Current State validating...")
            validateGameState(currentState)
            println("Opponent State validating...")
            validateGameState(opponentState)
        }

        println("===============================================")
        if (currentState.status == GameStatus.WIN) {
            if (turnCount % 2 == 1) {
                println("${agent1.name} Win!")
                if (opponentState.iterateAction().isEmpty()) println("${agent2.name} had no available action!")
            } else {
                println("${agent2.name} Win!")
                if (opponentState.iterateAction().isEmpty()) println("${agent1.name} had no available action!")
            }
        } else if (currentState.status == GameStatus.LOSE) {
            if (turnCount % 2 == 0) {
                println("${agent1.name} Win!")
                if (currentState.iterateAction().isEmpty()) println("${agent1.name} had no available action!")
            } else {
                println("${agent2.name} Win!")
                if (currentState.iterateAction().isEmpty()) println("${agent2.name} had no available action!")
            }
        } else println("${currentState.status} ${opponentState.status}")
    }

    private fun List<Square>.invert() = map {
        when (it) {
            Square.MINE -> Square.OPPONENT
            Square.OPPONENT -> Square.MINE
            else -> it
        }
    }

    fun validateGameState(state: GameState) {
        // 1. 보드 위에 놓인 카드 수 (빈칸이 아닌 것)
        val onBoardCount = state.board.count { it != Square.EMPTY }

        // 2. 내 손패의 카드 수 (0번 인덱스 제외)
        val handCount = state.hand.sum() // 또는 hand.drop(1).sum() (구현에 따라)

        // 3. 덱에 남은 카드 수
        val deckCount = state.deck.sum()

        // 4. 총합 (초기 설정에 따라 다름, 예: 10장)
        val total = onBoardCount + handCount + deckCount
        val expectedTotal = 10 // (1,2,3 각 3장 + 조커 1장 가정 시)

        if (total != expectedTotal) {
            throw IllegalStateException(
                """
            🚨 카드 증발/복사 발생!
            Total: $total (Expected: $expectedTotal)
            - Board: $onBoardCount
            - Hand: $handCount
            - Deck: $deckCount
            
            State Dump:
            $state
        """.trimIndent()
            )
        }

        // 추가: 덱에 음수가 있는지 확인
        if (state.deck.any { it < 0 }) {
            throw IllegalStateException("🚨 덱에 음수가 있습니다! ${state.deck}")
        }
    }

}