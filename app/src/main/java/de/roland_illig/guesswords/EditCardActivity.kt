package de.roland_illig.guesswords

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import java.util.Locale
import java.util.UUID

class EditCardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_card)
    }

    fun save(view: View) {
        fun text(resource: Int): String = findViewById<EditText>(resource).text.toString()

        val uuid = UUID.randomUUID()
        val language = Locale.getDefault().language
        val term = text(R.id.term)
        val forbidden1 = text(R.id.forbidden1)
        val forbidden2 = text(R.id.forbidden2)
        val forbidden3 = text(R.id.forbidden3)
        val forbidden4 = text(R.id.forbidden4)
        val forbidden5 = text(R.id.forbidden5)
        val card = Card(uuid, language, term, forbidden1, forbidden2, forbidden3, forbidden4, forbidden5)

        Db(this).use {
            it.add(card)
        }

        finish()
    }
}
