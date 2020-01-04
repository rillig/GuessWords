package de.roland_illig.guesswords

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.AsyncTask
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*

data class Card(
    val uuid: UUID,
    val language: String,
    val term: String,
    val forbidden1: String,
    val forbidden2: String,
    val forbidden3: String,
    val forbidden4: String,
    val forbidden5: String
) : Serializable

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
        if (remainingCards.isNotEmpty())
            remainingCards.removeAt(remainingCards.lastIndex)
    }

    fun timePasses(millis: Int) {
        remainingMillis = 0.coerceAtLeast(remainingMillis - millis)
    }

    fun nextTeam() {
        turn = turn.other()
        remainingMillis = totalMillis
    }

    fun currentCard() = remainingCards.lastOrNull()
}

fun loadGameState(ctx: Context): GameState {
    try {
        ctx.openFileInput("state").use { `is` ->
            ObjectInputStream(`is`).use { ois ->
                return ois.readObject() as GameState
            }
        }
    } catch (e: Exception) {
        return GameState()
    }
}

fun GameState.save(ctx: Context) {
    ctx.openFileOutput("state", Context.MODE_PRIVATE).use { os ->
        ObjectOutputStream(os).use { oos ->
            oos.writeObject(this)
        }
    }
}

interface Repo : AutoCloseable {
    fun loadLanguage(language: String): List<Card>
    fun loadAll(): List<Card>
    fun loadAllIncludingDeleted(): List<Card>
    fun merge(cards: List<Card>, commit: Boolean): MergeStats
    fun get(uuid: UUID): Card
}

data class MergeStats(
    val added: Int,
    val removed: Int,
    val changed: Int,
    val unchanged: Int
)

fun repo(ctx: Context): Repo = SQLiteRepo(ctx)

class SQLiteRepo(ctx: Context) : SQLiteOpenHelper(ctx, "cards.sqlite3", null, 1), Repo {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE question (
                uuid       TEXT PRIMARY KEY ON CONFLICT REPLACE,
                language   TEXT NOT NULL,
                term       TEXT NOT NULL,
                forbidden1 TEXT NOT NULL,
                forbidden2 TEXT NOT NULL,
                forbidden3 TEXT NOT NULL,
                forbidden4 TEXT NOT NULL,
                forbidden5 TEXT NOT NULL
            )
            """
        )

        for (card in predefinedCards()) {
            add(db, card)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) =
        TODO("not implemented")

    private fun add(db: SQLiteDatabase, card: Card) {
        db.insert("question", null, card.toContentValues())
    }

    override fun loadLanguage(language: String) =
        load("language = ? AND term IS NOT NULL AND term <> ''", language)

    override fun loadAll() = load("term IS NOT NULL AND term <> '' ORDER BY term")

    override fun loadAllIncludingDeleted() = load(null)

    private fun load(uuid: UUID): Card? {
        readableDatabase
            .query("question", columns, "uuid = ?", arrayOf(uuid.toString()), null, null, null)
            .use { return if (it.moveToNext()) it.toCard() else null }
    }

    private fun load(selection: String?, vararg selectionArgs: String): List<Card> {
        val cursor = readableDatabase.query(
            "question",
            columns,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val cards = mutableListOf<Card>()
        cursor.use {
            while (cursor.moveToNext()) {
                cards += cursor.toCard()
            }
        }
        return cards
    }

    override fun merge(cards: List<Card>, commit: Boolean): MergeStats {
        var added = 0
        var removed = 0
        var changed = 0
        var unchanged = 0

        for (card in cards) {
            val existing = load(card.uuid)
            if (existing == null) {
                added++
                if (commit) {
                    add(writableDatabase, card)
                }
            } else if (existing == card) {
                unchanged++
            } else if (card.term == "" && existing.term != "") {
                removed++
                if (commit) {
                    update(card)
                }
            } else {
                changed++
                if (commit) {
                    update(card)
                }
            }
        }

        return MergeStats(added, removed, changed, unchanged)
    }

    override fun get(uuid: UUID) = load(uuid)!!

    private fun update(card: Card) {
        writableDatabase.update(
            "question",
            card.toContentValues(),
            "uuid = ?",
            arrayOf(card.uuid.toString())
        )
    }

    private val columns = arrayOf(
        "uuid",
        "language",
        "term",
        "forbidden1",
        "forbidden2",
        "forbidden3",
        "forbidden4",
        "forbidden5"
    )

    private fun Card.toContentValues() = ContentValues().apply {
        put("uuid", uuid.toString())
        put("language", language)
        put("term", term)
        put("forbidden1", forbidden1)
        put("forbidden2", forbidden2)
        put("forbidden3", forbidden3)
        put("forbidden4", forbidden4)
        put("forbidden5", forbidden5)
    }

    private fun Cursor.toCard() = Card(
        UUID.fromString(getString(0)),
        getString(1),
        getString(2),
        getString(3),
        getString(4),
        getString(5),
        getString(6),
        getString(7)
    )
}

fun predefinedCards(): List<Card> {

    fun de(
        uuid: String,
        term: String,
        forbidden1: String,
        forbidden2: String,
        forbidden3: String,
        forbidden4: String,
        forbidden5: String
    ) = Card(
        UUID.fromString(uuid),
        "de",
        term,
        forbidden1,
        forbidden2,
        forbidden3,
        forbidden4,
        forbidden5
    )

    return listOf(
        de(
            "7425a3b8-1052-4712-c9cd-ae9ab7980001",
            "Krone",
            "Baum",
            "König",
            "Kopf",
            "Zahn",
            "Dänemark"
        ),
        de(
            "7425a3b8-1052-4712-c9cd-ae9ab7980002",
            "Hund",
            "Katze",
            "Haustier",
            "Bello",
            "Goofy",
            "bellen"
        ),
        de(
            "7425a3b8-1052-4712-c9cd-ae9ab7980003",
            "Strand",
            "Sandburg",
            "Meer",
            "Ostsee",
            "Lagerfeuer",
            "Düne"
        ),
        de(
            "7425a3b8-1052-4712-c9cd-ae9ab7980004",
            "Hausboot",
            "Wasser",
            "Fluss",
            "wohnen",
            "Gebäude",
            "schwimmen"
        )
    )
}

fun parseCards(text: CharSequence): List<Card> {
    val cards = mutableListOf<Card>()
    text.split(Regex("[\r\n]++")).forEach { line ->
        val cells = line.split(Regex("[,;\t] *+")).map(String::trim)
        if (cells.size >= 8 && (cells[0] == "" || cells[0].length == 36)) {
            val uuid = if (cells[0] == "") UUID.randomUUID() else UUID.fromString(cells[0])
            cards += Card(
                uuid,
                cells[1],
                cells[2],
                cells[3],
                cells[4],
                cells[5],
                cells[6],
                cells[7]
            )
        }
    }
    return cards
}

fun <Result> inBackground(background: () -> Result, post: (Result) -> Unit = {}) {
    class Task : AsyncTask<Unit, Unit, Result>() {
        override fun doInBackground(vararg params: Unit) = background()
        override fun onPostExecute(result: Result) = post(result)
    }
    Task().execute()
}
