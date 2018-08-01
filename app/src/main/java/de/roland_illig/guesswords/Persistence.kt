package de.roland_illig.guesswords

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.UUID

class Persistence {
    companion object {
        fun load(ctx: Context): GameState {
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
