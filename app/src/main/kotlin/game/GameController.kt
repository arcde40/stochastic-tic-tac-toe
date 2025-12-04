package game

import agent.Agent
import util.adjustElement

class GameController(
    val agent1: Agent,
    val agent2: Agent,
) {


    fun startGame() {


        val drawSequence = listOf(0, 1, 0, 1)

        val initialState = drawSequence.fold(GlobalGameState(), ::drawCard)

        val finalState = gameLoop(initialState)

        if (finalState.status == GameStatus.WIN) {
            println("${finalState.getCurrentAgent().name} Win!")
        } else if (finalState.status == GameStatus.LOSE) {
            println("${finalState.getOpponentAgent().name} Win!")
        } else println("Draw!")
    }


    private tailrec fun gameLoop(state: GlobalGameState): GlobalGameState {
        if (state.isGameEnded()) return state

        val action = state.getCurrentAgent().decideMove(state.toState())
        if (action == null) {
            println("${state.getCurrentAgent().name} Resigned.")
            return state
        }

        println("${state.getCurrentAgent().name} -> $action")

        val newState = applyAction(state, action)
        return gameLoop(newState)
    }

    private fun applyAction(state: GlobalGameState, action: Action) =
        with(state) {
            val drawnCard = GameManager.rollProbUniform(deck)
            play(action, drawnCard)
        }


    private fun GlobalGameState.getCurrentAgent() =
        if (currentPlayer == 0) agent1 else agent2


    private fun drawCard(
        state: GlobalGameState, playerIdx: Int
    ) =
        GameManager.rollProbUniform(state.deck).let { drawnCard ->
            assert(state.deck[drawnCard] > 0)
            state.giveCardToPlayer(playerIdx, drawnCard)
        }

    private fun GlobalGameState.getOpponentAgent() =
        if (currentPlayer == 0) agent2 else agent1

    private fun GlobalGameState.giveCardToPlayer(playerIdx: Int, drawnCard: Int) =
        copy(
            deck = deck.adjustElement(drawnCard, -1),
            hands = hands.mapIndexed { idx, originalHand ->
                if (idx == playerIdx) {
                    originalHand.adjustElement(drawnCard, 1)
                } else originalHand
            }
        )
    /*

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
    */
}