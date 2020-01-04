package de.roland_illig.guesswords

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import java.util.*

class EditCardActivity : AppCompatActivity() {

    private lateinit var card: Card

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_card)

        loadCard(intent.getSerializableExtra("uuid") as UUID?)
    }

    override fun onResume() {
        super.onResume()
        if (card.term != "")
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun loadCard(uuid: UUID?) {
        val card = if (uuid != null) {
            withRepo(this) { it.get(uuid) }
        } else {
            Card(
                UUID.randomUUID(), Locale.getDefault().language,
                "", "", "", "", "", ""
            )
        }

        fun setText(resource: Int, text: String) {
            findViewById<EditText>(resource).setText(text, TextView.BufferType.EDITABLE)
        }

        setText(R.id.term, card.term)
        setText(R.id.forbidden1, card.forbidden1)
        setText(R.id.forbidden2, card.forbidden2)
        setText(R.id.forbidden3, card.forbidden3)
        setText(R.id.forbidden4, card.forbidden4)
        setText(R.id.forbidden5, card.forbidden5)

        this.card = card
    }

    fun save(view: View) {
        fun text(resource: Int): String = findViewById<EditText>(resource).text.toString()

        val card = Card(
            card.uuid,
            card.language,
            text(R.id.term),
            text(R.id.forbidden1),
            text(R.id.forbidden2),
            text(R.id.forbidden3),
            text(R.id.forbidden4),
            text(R.id.forbidden5)
        )

        withRepo(this) { it.merge(listOf(card), true) }

        finish()
    }
}
