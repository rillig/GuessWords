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

    fun startTeams(view: View) = startActivity(Intent(this, TeamsActivity::class.java))

    fun startNewCard(view: View) = startActivity(Intent(this, EditCardActivity::class.java))
}
