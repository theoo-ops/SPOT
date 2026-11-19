package com.example.spot.data

import com.example.spot.data.model.FoodSpot
import java.text.Normalizer
import java.util.Locale

/** The one place search text is normalised and matched. */
object SearchMatching {
    private val CombiningMarks = "\\p{Mn}+".toRegex()
    private val Whitespace = "\\s+".toRegex()

    /** Lower-case, accents removed ("Café" -> "cafe"), spaces trimmed and collapsed. */
    fun normalize(text: String): String =
        Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(CombiningMarks, "")
            .trim()
            .replace(Whitespace, " ")

    /** Every word of the query must appear in the spot's name or category, in any order. */
    fun matches(spot: FoodSpot, query: String): Boolean {
        val words = normalize(query).split(' ').filter { it.isNotEmpty() }
        if (words.isEmpty()) return true // empty query: everything matches
        val haystack = normalize("${spot.name} ${spot.category}")
        return words.all { it in haystack }
    }
}
