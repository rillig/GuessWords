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
                return GameState(120, listOf())
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
                uuid TEXT,
                language TEXT,
                term TEXT,
                forbidden1 TEXT,
                forbidden2 TEXT,
                forbidden3 TEXT,
                forbidden4 TEXT,
                forbidden5 TEXT
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

    fun add(card: UnsavedCard): Card {
        return add(writableDatabase, card)
    }

    fun add(db: SQLiteDatabase, card: UnsavedCard): Card {
        val uuid = UUID.randomUUID()
        val id = db.insert("question", null, ContentValues().apply {
            put("uuid", uuid.toString())
            put("language", card.language)
            put("term", card.term)
            put("forbidden1", card.forbidden1)
            put("forbidden2", card.forbidden2)
            put("forbidden3", card.forbidden3)
            put("forbidden4", card.forbidden4)
            put("forbidden5", card.forbidden5)
        })
        return Card(
                id,
                uuid,
                card.language,
                card.term,
                card.forbidden1,
                card.forbidden2,
                card.forbidden3,
                card.forbidden4,
                card.forbidden5
        )
    }

    fun load(language: String): List<Card> {
        val cursor = readableDatabase.query(
                "question",
                arrayOf(
                        "rowid",
                        "uuid",
                        "language",
                        "term",
                        "forbidden1",
                        "forbidden2",
                        "forbidden3",
                        "forbidden4",
                        "forbidden5"
                ),
                "language = ?",
                arrayOf(language),
                null,
                null,
                null)

        val cards = mutableListOf<Card>()
        cursor.use {
            while (cursor.moveToNext()) {
                val card = Card(
                        cursor.getLong(0),
                        UUID.fromString(cursor.getString(1)),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8))
                cards += card
            }
        }
        return cards
    }
}
