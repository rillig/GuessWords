package de.roland_illig.guesswords

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView

class TeamsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teams)
    }

    override fun onResume() {
        super.onResume()
        val state = Persistence.load(this)
        findViewById<TextView>(R.id.score_a).text = resources.getQuantityString(R.plurals.team_score_label, state.scoreA, state.scoreA)
        findViewById<TextView>(R.id.score_b).text = resources.getQuantityString(R.plurals.team_score_label, state.scoreB, state.scoreB)
        val over = state.currentCard() == null
        val gameState = if (over) getString(R.string.all_cards_used_up) else getString(R.string.team_to_go_label).format(state.turn)
        findViewById<TextView>(R.id.game_state).text = gameState
        findViewById<Button>(R.id.go_button).apply {
            isEnabled = !over
            text = getString(if (state.remainingMillis == state.totalMillis) R.string.go_button else R.string.continue_button)
        }
    }

    fun startCard(view: View) {
        startActivity(Intent(this, CardActivity::class.java))
    }
}
