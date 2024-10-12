package de.roland_illig.guesswords

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class EditCardsActivity : AppCompatActivity() {

    private var cards: List<Card>? = null
    private lateinit var cardsAdapter: CardsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_cards)

        cardsAdapter = CardsAdapter()

        val rv = findViewById<RecyclerView>(R.id.cards_list)
        rv.adapter = cardsAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        cards = withRepo(this) { it.loadAll() }
        cardsAdapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        cards = null
    }

    fun startNewCard(view: View) {
        startActivity(Intent(this, EditCardActivity::class.java))
    }

    inner class CardsAdapter : RecyclerView.Adapter<RowHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            RowHolder(layoutInflater.inflate(R.layout.row_edit_cards_card, parent, false))

        override fun getItemCount() = cards?.size ?: 0

        override fun onBindViewHolder(holder: RowHolder, position: Int) =
            holder.bindModel(cards!![position], position)
    }

    inner class RowHolder(private val row: View) : RecyclerView.ViewHolder(row) {
        private val label = row.findViewById<TextView>(R.id.row_edit_cards_label)

        fun bindModel(card: Card, position: Int) {
            label.text = card.term
            row.setBackgroundColor(rainbowColor(position))
            row.setOnClickListener { click() }
        }

        private fun click() {
            val intent = Intent(this@EditCardsActivity, EditCardActivity::class.java)
            intent.putExtra("uuid", cards!![adapterPosition].uuid)
            startActivity(intent)
        }

        private fun rainbowColor(position: Int): Int {
            val hue = ((position * 23).and(0x7FFF_FFFF) % 360).toFloat()
            val hsv = arrayOf(hue, 1.0f, 1.0f).toFloatArray()
            return Color.HSVToColor(0x40, hsv)
        }
    }

}
