package agent.expectimax

import game.GameState
import game.GameStatus
import game.Square

object Heuristic {

    // 가중치 (Tuning Parameter)
    private const val W_THREAT = 0.3   // 2목 가중치
    private const val W_JOKER = 0.15   // 조커 보유 가중치
    private const val W_CENTER = 0.05  // 중앙 카드(2) 가중치
    private const val W_MOBILITY = 0.01 // 행동 가능 횟수 가중치

    fun evaluate(state: GameState): Double {
        // 1. 이미 승패가 난 경우 (Terminal)
        if (state.isGameEnded()) {
            return when (state.status) {
                GameStatus.WIN -> 1.0
                GameStatus.LOSE -> -1.0
                else -> 0.0
            }
        }

        var score = 0.0

        // 2. 보드 위협 평가 (Lines)
        score += evaluateLines(state.board, Square.MINE) - evaluateLines(state.board, Square.OPPONENT)

        // 3. 핸드 가치 평가 (Cards)
        score += evaluateHand(state.hand)

        // 4. 기동성 평가 (Mobility - 선택지가 많으면 좋다)
        // (상대 패는 모르므로 내 기동성만 계산하거나, 덱 추론을 통해 상대 기동성도 뺄 수 있음)
        val myActions = state.iterateAction().size
        score += myActions * W_MOBILITY

        // * 점수가 -0.9 ~ 0.9를 넘지 않도록 클램핑 (승리/패배와 구분하기 위해)
        return score.coerceIn(-0.9, 0.9)
    }

    // 라인 평가 (2개가 이어져 있고 나머지가 빈칸이면 위협적인 상태)
    private fun evaluateLines(board: List<Square>, target: Square): Double {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Cols
            listOf(0, 4, 8), listOf(2, 4, 6)                   // Diags
        )

        var lineScore = 0.0

        for (indices in lines) {
            val cells = indices.map { board[it] }
            val countTarget = cells.count { it == target }
            val countEmpty = cells.count { it == Square.EMPTY }

            // 2목 완성 (승리 직전)
            if (countTarget == 2 && countEmpty == 1) {
                lineScore += W_THREAT
            }
            // 1목 (잠재력) - 가중치 낮게
            else if (countTarget == 1 && countEmpty == 2) {
                lineScore += (W_THREAT * 0.1)
            }
        }
        return lineScore
    }

    // 핸드 평가 (조커 > 2 > 1,3)
    private fun evaluateHand(hand: List<Int>): Double {
        var handScore = 0.0

        // hand는 [dummy, cnt1, cnt2, cnt3, cntJoker] 형태라고 가정
        // 또는 정렬된 리스트라면 순회하며 체크

        // 여기서는 hand가 Count 리스트가 아니라 [1, 2, 4] 처럼 카드 값의 리스트라고 가정
        // (작성하신 GameState 구조에 맞춰 수정하세요)
        for (card in hand) {
            when (card) {
                4 -> handScore += W_JOKER      // 조커
                2 -> handScore += W_CENTER     // 2번 카드 (중앙 제어)
                1, 3 -> handScore += (W_CENTER * 0.5) // 변두리
            }
        }
        return handScore
    }
}