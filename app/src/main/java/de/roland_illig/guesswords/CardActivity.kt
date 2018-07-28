package de.roland_illig.guesswords

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView

class CardActivity : AppCompatActivity() {

    private lateinit var state: GameState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)
    }

    override fun onResume() {
        super.onResume()
        state = Persistence.load(this)
        updateCard()
    }

    private fun updateCard() {
        val card = state.currentCard() ?: return finish()

        findViewById<TextView>(R.id.term).text = card.term
        findViewById<TextView>(R.id.forbidden1).text = card.forbidden1
        findViewById<TextView>(R.id.forbidden2).text = card.forbidden2
        findViewById<TextView>(R.id.forbidden3).text = card.forbidden3
        findViewById<TextView>(R.id.forbidden4).text = card.forbidden4
        findViewById<TextView>(R.id.forbidden5).text = card.forbidden5
    }

    fun correctClicked(view: View) {
        state.correct()
        state.save(this)
        updateCard()
    }

    fun wrongClicked(view: View) {
        state.wrong()
        state.save(this)
        updateCard()
    }

    fun skipClicked(view: View) {
        state.nextCard()
        state.save(this)
        updateCard()
    }
}
