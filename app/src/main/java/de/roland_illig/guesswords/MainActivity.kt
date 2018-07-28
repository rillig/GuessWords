package de.roland_illig.guesswords

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun gotoCard(view: View) {
        val state = Persistence.load(this)
        if (state.currentCard() == null) {
            state.addCards(predefinedCards().shuffled())
            state.save(this)
        }
        startActivity(Intent(this, CardActivity::class.java))
    }
}
