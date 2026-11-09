package com.tavola.domain.model

import java.text.Normalizer

/**
 * Something a feature that knows about food (a recipe) offers to put on the shopping list: a [name] and a [quantity] that is
 * already written for the person ("400 g"), or `null` when there is nothing to say (an ingredient "to taste").
 */
data class ShoppingCandidate(val name: String, val quantity: String? = null)

/** How the names of shopping items are compared, so "Tomates" and "tomate" are the same thing. */
object ShoppingNames {
    private val nonLetters = Regex("[^a-z]+")

    /** The letters of [text] as lowercase words without accents or anything else between them. */
    internal fun words(text: String): List<String> = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
        .filter { character -> Character.getType(character) != Character.NON_SPACING_MARK.toInt() }
        .split(nonLetters)
        .filter { word -> word.isNotEmpty() }

    /** Whether [first] and [second] name the same thing, ignoring case, accents, spacing and a plural "s" or "es" on the whole name. */
    fun same(first: String, second: String): Boolean {
        val left = words(first).joinToString(" ")
        val right = words(second).joinToString(" ")
        if (left.isEmpty() || right.isEmpty()) return false
        return left == right || PLURAL_ENDINGS.any { ending -> left + ending == right || right + ending == left }
    }

    private val PLURAL_ENDINGS = listOf("s", "es")
}
