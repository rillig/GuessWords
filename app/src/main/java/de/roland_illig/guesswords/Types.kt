package de.roland_illig.guesswords

import java.io.Serializable
import java.util.UUID

class Card(
        val id: Long,
        val uuid: UUID,
        val language: String,
        val term: String,
        val forbidden1: String,
        val forbidden2: String,
        val forbidden3: String,
        val forbidden4: String,
        val forbidden5: String) : Serializable

class UnsavedCard(
        val language: String,
        val term: String,
        val forbidden1: String,
        val forbidden2: String,
        val forbidden3: String,
        val forbidden4: String,
        val forbidden5: String) : Serializable

enum class Player {
    A, B;

    fun other() = if (this == A) B else A
}

class GameState() : Serializable {

    var turn = Player.A; private set
    private val score = intArrayOf(0, 0)
    private var millisLeft = 0
    private val remainingCards = mutableListOf<Card>()
    var secondsPerRound = 120
        set(value) {
            field = value; millisLeft = Math.min(millisLeft, value * 1000)
        }

    val scoreA get() = score[0]
    val scoreB get() = score[1]

    fun addCards(cards: List<Card>) {
        remainingCards += cards
    }

    fun correct() {
        score[turn.ordinal]++
        nextCard()
    }

    fun wrong() {
        score[turn.other().ordinal]++
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

    fun currentCard() = remainingCards.lastOrNull()

    fun over() = millisLeft == 0

    fun timePercentage() = 100 - 100 * millisLeft / (secondsPerRound * 1000)
}
