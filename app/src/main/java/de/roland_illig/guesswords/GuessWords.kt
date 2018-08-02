package de.roland_illig.guesswords

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.UUID

class Card(
        val uuid: UUID,
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

class GameState(secondsPerRound: Int = 120) : Serializable {

    var turn = Player.A; private set
    private val score = intArrayOf(0, 0)
    val totalMillis get() = secondsPerRound * 1000
    private val remainingCards = mutableListOf<Card>()
    var secondsPerRound = secondsPerRound
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

fun loadGameState(ctx: Context): GameState {
    try {
        ctx.openFileInput("state").use {
            ObjectInputStream(it).use {
                return it.readObject() as GameState
            }
        }
    } catch (e: Exception) {
        return GameState()
    }
}

fun GameState.save(ctx: Context) {
    ctx.openFileOutput("state", Context.MODE_PRIVATE).use {
        ObjectOutputStream(it).use {
            it.writeObject(this)
        }
    }
}

class Db(ctx: Context) : SQLiteOpenHelper(ctx, "cards.sqlite3", null, 1), AutoCloseable {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE question (
                uuid       TEXT PRIMARY KEY ON CONFLICT REPLACE,
                language   TEXT NOT NULL,
                term       TEXT NOT NULL,
                forbidden1 TEXT NOT NULL,
                forbidden2 TEXT NOT NULL,
                forbidden3 TEXT NOT NULL,
                forbidden4 TEXT NOT NULL,
                forbidden5 TEXT NOT NULL
            )""")

        for (card in predefinedCards()) {
            db.insert("question", null, ContentValues().apply {
                put("uuid", card.uuid.toString())
                put("language", card.language)
                put("term", card.term)
                put("forbidden1", card.forbidden1)
                put("forbidden2", card.forbidden2)
                put("forbidden3", card.forbidden3)
                put("forbidden4", card.forbidden4)
                put("forbidden5", card.forbidden5)
            })
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        TODO("not implemented")
    }

    fun add(card: Card) = add(writableDatabase, card)

    fun add(db: SQLiteDatabase, card: Card) {
        val cv = ContentValues().apply {
            put("uuid", card.uuid.toString())
            put("language", card.language)
            put("term", card.term)
            put("forbidden1", card.forbidden1)
            put("forbidden2", card.forbidden2)
            put("forbidden3", card.forbidden3)
            put("forbidden4", card.forbidden4)
            put("forbidden5", card.forbidden5)
        }
        db.insert("question", null, cv)
    }

    fun load(language: String): List<Card> {
        val cursor = readableDatabase.query(
                "question",
                arrayOf(
                        "uuid",
                        "language",
                        "term",
                        "forbidden1",
                        "forbidden2",
                        "forbidden3",
                        "forbidden4",
                        "forbidden5"
                ),
                if (language != "") "language = ? AND term IS NOT NULL" else null,
                if (language != "") arrayOf(language) else arrayOf(),
                null,
                null,
                null)

        val cards = mutableListOf<Card>()
        cursor.use {
            while (cursor.moveToNext()) {
                val card = Card(
                        UUID.fromString(cursor.getString(0)),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7))
                cards += card
            }
        }
        return cards
    }
}

fun predefinedCards(): List<Card> {

    fun de(uuid: String, term: String, forbidden1: String, forbidden2: String, forbidden3: String, forbidden4: String, forbidden5: String) =
            Card(UUID.fromString(uuid), "de", term, forbidden1, forbidden2, forbidden3, forbidden4, forbidden5)

    return listOf(
            de("7425a3b8-1052-4712-c9cd-ae9ab7980001", "Krone", "Baum", "König", "Kopf", "Zahn", "Dänemark"),
            de("7425a3b8-1052-4712-c9cd-ae9ab7980002", "Hund", "Katze", "Haustier", "Bello", "Goofy", "bellen"),
            de("7425a3b8-1052-4712-c9cd-ae9ab7980003", "Strand", "Sandburg", "Meer", "Ostsee", "Lagerfeuer", "Düne"),
            de("7425a3b8-1052-4712-c9cd-ae9ab7980004", "Hausboot", "Wasser", "Fluss", "wohnen", "Gebäude", "schwimmen"))
}
