package com.tavola.feature.recipes

import android.content.res.Resources
import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.IngredientUnit
import java.text.NumberFormat
import java.util.Locale

private const val MAX_FRACTION_DIGITS = 2

/** "400", "0,5" or "1,25": whole numbers have no decimals and the separator follows the [locale]. */
internal fun formatQuantity(quantity: Double, locale: Locale): String = NumberFormat.getNumberInstance(locale).apply {
    maximumFractionDigits = MAX_FRACTION_DIGITS
    minimumFractionDigits = 0
    isGroupingUsed = false
}.format(quantity)

/**
 * The amount of an ingredient as shown next to its name ("400 g", "2 tbsp", "1 pinch", "to taste"), or `null` when
 * there is nothing to show (an ingredient with a unit but no quantity).
 */
internal fun ingredientAmount(ingredient: Ingredient, locale: Locale, resources: Resources): String? {
    if (ingredient.unit == IngredientUnit.ToTaste) return resources.getString(R.string.ingredient_unit_to_taste)

    val quantity = ingredient.quantity ?: return null
    val formatted = formatQuantity(quantity, locale)
    return if (ingredient.unit == IngredientUnit.Pinch) {
        resources.getQuantityString(R.plurals.ingredient_unit_pinch, if (quantity == 1.0) 1 else 2, formatted)
    } else {
        resources.getString(ingredient.unit.formatRes(), formatted)
    }
}

/** Format string of every unit that is written as "<quantity> <unit>"; the pinch and "to taste" have their own wording. */
private fun IngredientUnit.formatRes(): Int = when (this) {
    IngredientUnit.Gram -> R.string.ingredient_unit_gram
    IngredientUnit.Kilogram -> R.string.ingredient_unit_kilogram
    IngredientUnit.Milliliter -> R.string.ingredient_unit_milliliter
    IngredientUnit.Liter -> R.string.ingredient_unit_liter
    IngredientUnit.Teaspoon -> R.string.ingredient_unit_teaspoon
    IngredientUnit.Tablespoon -> R.string.ingredient_unit_tablespoon
    IngredientUnit.Piece, IngredientUnit.Pinch, IngredientUnit.ToTaste -> R.string.ingredient_unit_piece
}
