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
    val totalMillis get() = secondsPerRound * 1000
    private val remainingCards = mutableListOf<Card>()
    var secondsPerRound = 120
        set(value) {
            field = value; remainingMillis = totalMillis
        }
    var remainingMillis = totalMillis; private set

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
        remainingMillis = Math.max(0, remainingMillis - millis)
    }

    fun nextTeam() {
        turn = turn.other()
        remainingMillis = totalMillis
    }

    fun currentCard() = remainingCards.lastOrNull()
}
