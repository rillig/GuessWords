package de.roland_illig.guesswords

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.*


class MainActivity : AppCompatActivity() {

    private val roundLengths = listOf(
        Pair(30, R.string.round_length_30s),
        Pair(60, R.string.round_length_60s),
        Pair(90, R.string.round_length_90s),
        Pair(120, R.string.round_length_120s),
        Pair(180, R.string.round_length_180s),
        Pair(300, R.string.round_length_300s),
        Pair(600, R.string.round_length_600s)
    )

    private lateinit var state: GameState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        state = loadGameState(this)
        initRoundLength()
    }

    override fun onPause() {
        state.save(this)
        super.onPause()
    }

    private fun initRoundLength() {
        val roundLengthLabel = findViewById<TextView>(R.id.round_length_label)
        val roundLength = findViewById<SeekBar>(R.id.round_length)

        fun updateLabel(index: Int) {
            val param = resources.getString(roundLengths[index].second)
            val label = resources.getString(R.string.round_length_label, param)
            roundLengthLabel.text = label
        }

        roundLengths.forEachIndexed { index, (seconds, _) ->
            if (state.secondsPerRound == seconds) {
                roundLength.progress = index
            }
        }
        updateLabel(roundLength.progress)

        val onChange = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                state.secondsPerRound = roundLengths[progress].first
                updateLabel(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }

        roundLength.setOnSeekBarChangeListener(onChange)
    }

    fun startFeedback(view: View) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.type = "*/*"
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("roland.illig@gmx.de"))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_mail_subject))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun getTextFromClipboard(): String {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.description.getMimeType(0).startsWith("text/")) {
            return clip.getItemAt(0).coerceToText(this).toString()
        }
        return ""
    }

    fun runImport(view: View) {
        val text = getTextFromClipboard()
        inBackground({ runImportSimulate(text) }, this::runImportConfirm)
    }

    private fun runImportSimulate(text: String): Pair<List<Card>, MergeStats>? {
        val cards = parseCards(text)
        if (cards.isEmpty()) return null

        val stats = withRepo(this) { it.merge(cards, false) }
        return Pair(cards, stats)
    }

    private fun runImportConfirm(result: Pair<List<Card>, MergeStats>?) {
        if (result != null) {
            val (cards, stats) = result
            val message = String.format(
                getString(R.string.import_stats),
                stats.added, stats.changed, stats.unchanged, stats.removed
            )
            val doImport: (DialogInterface, Int) -> Unit = { _, _ ->
                inBackground<Unit>({ withRepo(this) { it.merge(cards, true) } })
            }

            AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.import_button), doImport)
                .setCancelable(true)
                .show()
        } else {
            val msg = getString(R.string.import_no_cards_found)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    fun runExport(view: View) {
        val cards = withRepo(this) { it.loadAllIncludingDeleted() }
        val sb = StringBuilder("\uFEFF")
        fun writeLine(vararg elements: Any) {
            sb.append(elements.joinToString(separator = "\t", postfix = "\r\n"))
        }
        writeLine(
            getString(R.string.csv_uuid),
            getString(R.string.csv_language),
            getString(R.string.csv_term),
            getString(R.string.csv_forbidden),
            getString(R.string.csv_forbidden),
            getString(R.string.csv_forbidden),
            getString(R.string.csv_forbidden),
            getString(R.string.csv_forbidden)
        )
        cards.forEach {
            it.apply {
                writeLine(
                    uuid,
                    language,
                    term,
                    forbidden1,
                    forbidden2,
                    forbidden3,
                    forbidden4,
                    forbidden5
                )
            }
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText(getString(R.string.app_name), sb)

        val msg = resources.getQuantityString(
            R.plurals.exported_cards_notification,
            cards.size,
            cards.size
        )
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    fun mailExport(view: View) {
        val cards = withRepo(this){ it.loadAllIncludingDeleted() }
        val sb = StringBuilder("\uFEFF")
        fun writeLine(vararg elements: Any) {
            sb.append(elements.joinToString(separator = "\t", postfix = "\r\n"))
        }
        writeLine(
            getString(R.string.csv_uuid),
            getString(R.string.csv_language),
            getString(R.string.csv_term),
            getString(R.string.csv_forbidden),
            getString(R.string.csv_forbidden),
            getString(R.string.csv_forbidden),
            getString(R.string.csv_forbidden),
            getString(R.string.csv_forbidden)
        )
        cards.forEach {
            it.apply {
                writeLine(
                    uuid,
                    language,
                    term,
                    forbidden1,
                    forbidden2,
                    forbidden3,
                    forbidden4,
                    forbidden5
                )
            }
        }

        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
        val fileName = String.format(getString(R.string.export_mail_file_name), timestamp)
        val tmpfile = File(cacheDir, fileName)
        FileOutputStream(tmpfile).use {
            it.writer(StandardCharsets.UTF_16LE).write(sb.toString())
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/csv; charset=UTF-16LE"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_mail_subject))
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.export_mail_body))
        intent.putExtra(
            Intent.EXTRA_STREAM,
            Uri.parse("content://de.roland_illig.guesswords/${fileName}")
        )
        startActivity(intent)
    }

    fun startNewCard(view: View) = startActivity(Intent(this, EditCardActivity::class.java))

    fun startManageCards(view: View) = startActivity(Intent(this, EditCardsActivity::class.java))

    fun startNewGame(view: View) {
        state = GameState(state.secondsPerRound)
        startGame(view)
    }

    fun startGame(view: View) {
        if (state.currentCard() == null) {
            val cards = withRepo(this) { it.loadLanguage(Locale.getDefault().language) }
            state.addCards(cards.shuffled())
        }

        startActivity(Intent(this, TeamsActivity::class.java))
    }
}
