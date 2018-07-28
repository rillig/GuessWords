package de.roland_illig.guesswords

import java.util.UUID

fun predefinedCards(): List<Card> {
    var id = 0L

    fun de(term: String, forbidden1: String, forbidden2: String, forbidden3: String, forbidden4: String, forbidden5: String): Card {
        id++
        // See https://youtrack.jetbrains.com/issue/KT-4749
        val uuid = UUID(0x7425a3b810524712L, (0x49cdae9ab7980000L xor Long.MIN_VALUE) + id)
        return Card(id, uuid, "de", term, forbidden1, forbidden2, forbidden3, forbidden4, forbidden5)
    }

    return listOf(
            de("Krone", "Baum", "König", "Kopf", "Zahn", "Dänemark"),
            de("Hund", "Katze", "Haustier", "Bello", "Goofy", "bellen"),
            de("Strand", "Sandburg", "Meer", "Ostsee", "Lagerfeuer", "Düne"),
            de("Hausboot", "Wasser", "Fluss", "wohnen", "Gebäude", "schwimmen")
    )
}
