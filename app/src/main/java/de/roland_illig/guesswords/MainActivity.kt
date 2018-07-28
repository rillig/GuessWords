package de.roland_illig.guesswords

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun gotoCard(view: View) {
        val state = Persistence.load(this)
        if (state.currentCard() == null) {
            val cards = Db(this).use { it.load(Locale.getDefault().language) }
            state.addCards(cards.shuffled())
        }
        state.save(this)
        startActivity(Intent(this, CardActivity::class.java))
    }

    fun newCard(view: View) = startActivity(Intent(this, EditCardActivity::class.java))
}
