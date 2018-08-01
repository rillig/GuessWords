package de.roland_illig.guesswords

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import java.util.Locale
import java.util.UUID


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun runImport(view: View) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            val text = clipboard.primaryClip.getItemAt(0).text
            val cards = mutableListOf<Card>()
            text.split(Regex("\r?\n")).forEach { line ->
                val cells = line.split(Regex("[,;\t] *+"))
                if (cells.size >= 8 && (cells[0] == "" || cells[0].length == 36)) {
                    val uuid = if (cells[0] == "") UUID.randomUUID() else UUID.fromString(cells[0])
                    cards += Card(uuid, cells[1], cells[2], cells[3], cells[4], cells[5], cells[6], cells[7])
                }
            }

            if (cards.isEmpty()) {
                Toast.makeText(this, getString(de.roland_illig.guesswords.R.string.import_no_cards_found), Toast.LENGTH_LONG).show()
                return
            }

            AlertDialog.Builder(this)
                    .setMessage(resources.getQuantityString(R.plurals.import_cards_question, cards.size, cards.size))
                    .setPositiveButton(getString(R.string.import_button)) { _, _ -> Db(this).use { db -> cards.forEach(db::add) } }
                    .setCancelable(true)
                    .show()
        }
    }

    fun runExport(view: View) {
        val cards = Db(this).load("")
        val sb = StringBuilder("\uFEFF")
        fun writeLine(vararg elements: Any) {
            sb.append(elements.joinToString(postfix = "\r\n"))
        }
        writeLine(
                getString(R.string.csv_uuid),
                getString(R.string.csv_language),
                getString(R.string.csv_term),
                getString(R.string.csv_forbidden),
                getString(R.string.csv_forbidden),
                getString(R.string.csv_forbidden),
                getString(R.string.csv_forbidden),
                getString(R.string.csv_forbidden))
        cards.forEach {
            it.apply {
                writeLine(uuid, language, term, forbidden1, forbidden2, forbidden3, forbidden4, forbidden5)
            }
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText(getString(R.string.app_name), sb)

        val msg = resources.getQuantityString(R.plurals.exported_cards_notification, cards.size, cards.size)
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    fun startNewCard(view: View) = startActivity(Intent(this, EditCardActivity::class.java))

    fun startNewGame(view: View) {
        GameState().save(this)
        startGame(view)
    }

    fun startGame(view: View) {
        val state = Persistence.load(this)
        if (state.currentCard() == null) {
            val cards = Db(this).use { it.load(Locale.getDefault().language) }
            state.addCards(cards.shuffled())
        }
        state.save(this)

        startActivity(Intent(this, TeamsActivity::class.java))
    }
}
