package com.tavola.feature.recipes

import com.tavola.domain.model.Recipe
import java.text.Normalizer

private val combiningMarks = Regex("\\p{Mn}+")

/** Minúsculas y sin tildes, para que "tiramisu" encuentre "Tiramisú". */
internal fun String.normalizedForSearch(): String = Normalizer.normalize(this, Normalizer.Form.NFD)
    .replace(combiningMarks, "")
    .lowercase()

/** Una receta coincide si la búsqueda aparece en su título, sus etiquetas o sus ingredientes. Una búsqueda vacía las admite todas. */
internal fun Recipe.matches(query: String): Boolean {
    val needle = query.normalizedForSearch().trim()
    if (needle.isEmpty()) return true
    return title.normalizedForSearch().contains(needle) ||
        tags.any { tag -> tag.normalizedForSearch().contains(needle) } ||
        ingredients.any { ingredient -> ingredient.name.normalizedForSearch().contains(needle) }
}
