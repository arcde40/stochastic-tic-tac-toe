package game

enum class GameStatus {
    WIN,
    LOSE,
    DRAW,
    NOT_ENDED;

    val isEnded: Boolean
        get() = this != NOT_ENDED
}