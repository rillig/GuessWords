package de.roland_illig.guesswords

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class CardActivity : AppCompatActivity() {

    private lateinit var progress: ProgressBar
    private var buttonsEnabled = false
    private lateinit var timer: Timer
    private lateinit var handler: Handler
    private lateinit var state: GameState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)
    }

    override fun onResume() {
        super.onResume()

        state = loadGameState(this)
        progress = findViewById(R.id.progress)
        progress.max = state.totalMillis

        handler = Handler()
        timer = Timer()
        timer.scheduleAtFixedRate(0, 100) { updateProgressBar() }

        updateCard()
    }

    override fun onPause() {
        timer.cancel()
        state.save(this)
        super.onPause()
    }

    private fun updateProgressBar() {
        state.timePasses(100)
        progress.progress = state.totalMillis - state.remainingMillis
        if (state.remainingMillis == 0) {
            state.nextCard()
            state.nextTeam()
            setResult(RESULT_OK, Intent().putExtra("enableButton", false))
            finish()
        }
    }

    private fun updateCard() {
        val card = state.currentCard() ?: return finish()

        findViewById<TextView>(R.id.term).text = card.term
        findViewById<TextView>(R.id.forbidden1).text = card.forbidden1
        findViewById<TextView>(R.id.forbidden2).text = card.forbidden2
        findViewById<TextView>(R.id.forbidden3).text = card.forbidden3
        findViewById<TextView>(R.id.forbidden4).text = card.forbidden4
        findViewById<TextView>(R.id.forbidden5).text = card.forbidden5

        buttonsEnabled = false
        Handler().postDelayed({ buttonsEnabled = true }, 1200)
    }

    fun wrongClicked(view: View) {
        if (!buttonsEnabled) return
        state.wrong()
        updateCard()
    }

    fun skipClicked(view: View) {
        if (!buttonsEnabled) return
        state.nextCard()
        updateCard()
    }

    fun correctClicked(view: View) {
        if (!buttonsEnabled) return
        state.correct()
        updateCard()
    }
}
