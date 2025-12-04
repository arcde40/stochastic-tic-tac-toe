package agent.expectimax

object DebugObject {
    var visitedNode = 0L
    var cacheHits = 0L

    fun reset() {
        visitedNode = 0L
        cacheHits = 0
    }
}