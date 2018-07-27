package de.roland_illig.guesswords

import java.io.Serializable
import java.util.UUID

class Card(
        id: UUID,
        language: String,
        searchTerm: String,
        forbidden: List<String>,
        labels: List<String> = listOf())

enum class Player {
    A, B;

    fun other() = if (this == A) B else A
}

class GameState(
        var millisTotal: Int,
        cards: List<Card>) : Serializable {

    private var turn: Player = Player.A
    private val points = intArrayOf(0, 0)
    private var millisLeft: Int
    private val remainingCards: MutableList<Card>

    init {
        millisLeft = millisTotal
        remainingCards = cards.toMutableList()
    }

    fun correct() {
        points[turn.ordinal]++
        nextCard()
    }

    fun wrong() {
        points[turn.other().ordinal]++
        nextCard()
    }

    fun nextCard() {
        if (!remainingCards.isEmpty())
            remainingCards.removeAt(remainingCards.lastIndex)
    }

    fun timePasses(millis: Int) {
        millisLeft -= Math.max(0, millis)
    }

    fun nextTeam() {
        turn = turn.other()
    }

    fun currentCard() = remainingCards.firstOrNull()

    fun over() = millisLeft == 0

    fun timePercentage() = 100 - 100 * millisLeft / millisTotal
}
