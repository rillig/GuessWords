package de.roland_illig.guesswords

import java.util.UUID

fun predefinedCards(): List<Card> {

    fun de(uuid: String, term: String, forbidden1: String, forbidden2: String, forbidden3: String, forbidden4: String, forbidden5: String) =
            Card(UUID.fromString(uuid), "de", term, forbidden1, forbidden2, forbidden3, forbidden4, forbidden5)

    return listOf(
            de("7425a3b8-1052-4712-c9cd-ae9ab7980001", "Krone", "Baum", "König", "Kopf", "Zahn", "Dänemark"),
            de("7425a3b8-1052-4712-c9cd-ae9ab7980002", "Hund", "Katze", "Haustier", "Bello", "Goofy", "bellen"),
            de("7425a3b8-1052-4712-c9cd-ae9ab7980003", "Strand", "Sandburg", "Meer", "Ostsee", "Lagerfeuer", "Düne"),
            de("7425a3b8-1052-4712-c9cd-ae9ab7980004", "Hausboot", "Wasser", "Fluss", "wohnen", "Gebäude", "schwimmen"))
}
