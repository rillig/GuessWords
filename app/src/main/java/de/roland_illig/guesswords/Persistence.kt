package de.roland_illig.guesswords

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

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

class Db(ctx: Context) : SQLiteOpenHelper(ctx, "cards.sqlite3", null, 1) {
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
            add(db, card)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        TODO("not implemented")
    }

    fun add(db: SQLiteDatabase, card: Card) {
        db.insert("question", null, ContentValues().apply {
            put("id", card.id)
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
