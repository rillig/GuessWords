package de.roland_illig.guesswords

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import java.util.Locale

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
        findViewById<TextView>(R.id.team_to_go).text = getString(R.string.team_to_go_label).format(state.turn)
    }

    fun startCard(view: View) {
        val state = Persistence.load(this)
        if (state.currentCard() == null) {
            val cards = Db(this).use { it.load(Locale.getDefault().language) }
            state.addCards(cards.shuffled())
        }
        state.save(this)
        startActivity(Intent(this, CardActivity::class.java))
    }
}
