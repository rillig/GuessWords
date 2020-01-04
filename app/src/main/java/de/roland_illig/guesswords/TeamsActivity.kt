package de.roland_illig.guesswords

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView

class TeamsActivity : AppCompatActivity() {

    private val cardRequestCode = 1
    private var buttonEnabled = true

    private fun scoreA() = findViewById<TextView>(R.id.score_a)
    private fun scoreB() = findViewById<TextView>(R.id.score_b)
    private fun gameState() = findViewById<TextView>(R.id.game_state)
    private fun go() = findViewById<Button>(R.id.go_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teams)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cardRequestCode && resultCode == Activity.RESULT_OK && data != null) {
            buttonEnabled = data.getBooleanExtra("enableButton", true)
            if (!buttonEnabled) {
                Handler().postDelayed({ buttonEnabled = true }, 1200)
            }
        }
    }

    private fun qty(res: Int, n: Int) = resources.getQuantityString(res, n, n)

    override fun onResume() {
        super.onResume()

        val state = loadGameState(this)

        scoreA().text = qty(R.plurals.team_score_label, state.scoreA)
        scoreB().text = qty(R.plurals.team_score_label, state.scoreB)

        val over = state.currentCard() == null
        val gameState =
            if (over) getString(R.string.all_cards_used_up)
            else getString(R.string.team_to_go_label).format(state.turn)
        gameState().text = gameState

        val msgId =
            if (state.remainingMillis == state.totalMillis) R.string.go_button
            else R.string.continue_button
        go().isEnabled = !over
        go().text = getString(msgId)
    }

    fun startCard(view: View) {
        buttonEnabled || return
        startActivityForResult(Intent(this, CardActivity::class.java), cardRequestCode)
    }
}
