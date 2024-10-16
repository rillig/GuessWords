package de.roland_illig.guesswords

import com.memoizr.assertk.assert
import org.junit.Test
import java.util.UUID

class GameStateTest {
    @Test
    fun `percentage is reported correctly`() {
        val cards = listOf(
                Card(UUID.randomUUID(), "de", "Krone", "Baum", "König", "Schmuck", "aufsetzen", "richten"),
                Card(UUID.randomUUID(), "de", "Hunger", "Essen", "Bauch", "Magen", "Durst", "Dritte Welt"),
                Card(UUID.randomUUID(), "de", "Atom", "klein", "winzig", "unteilbar", "Teilchen", "Element")
        )
        val state = GameState(120_000)
        state.addCards(cards)

        // See https://github.com/memoizr/assertk-core
        assert that state.remainingMillis isEqualTo 120_000
    }
}
