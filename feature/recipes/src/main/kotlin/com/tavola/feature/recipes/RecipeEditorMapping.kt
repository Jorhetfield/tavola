package com.tavola.feature.recipes

import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.IngredientUnit
import com.tavola.domain.model.Recipe

private const val MAX_PREP_TIME_DIGITS = 4

/** Keeps only the digits of what was typed in the prep time field, so it can always be parsed. */
internal fun sanitizePrepTime(text: String): String = text.filter { character -> character.isDigit() }.take(MAX_PREP_TIME_DIGITS)

/** "1.5" or "1,5" (either separator, since it follows the keyboard); `null` if it is blank or not a positive number. */
internal fun parseQuantity(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { value -> value > 0.0 }

/** The quantity as it is edited: no trailing ".0" for whole numbers. */
internal fun quantityText(quantity: Double): String =
    if (quantity == quantity.toLong().toDouble()) quantity.toLong().toString() else quantity.toString()

/** Fills the form with [recipe]; [nextKey] hands out the row keys. */
internal fun Recipe.toEditing(nextKey: () -> Int): RecipeEditorUiState.Editing = RecipeEditorUiState.Editing(
    isNew = false,
    title = title,
    prepTime = prepTimeMinutes?.toString().orEmpty(),
    tags = tags,
    ingredients = ingredients.map { ingredient ->
        IngredientDraft(
            key = nextKey(),
            name = ingredient.name,
            quantity = ingredient.quantity?.let(::quantityText).orEmpty(),
            unit = ingredient.unit,
        )
    },
    steps = steps.map { step -> StepDraft(key = nextKey(), text = step) },
)

/**
 * The recipe the form describes, stored under [id] (blank to create a new one). Rows left empty are dropped, a time of zero
 * means "not set" and "to taste" ingredients carry no quantity.
 */
internal fun RecipeEditorUiState.Editing.toRecipe(id: String): Recipe = Recipe(
    id = id,
    title = title.trim(),
    prepTimeMinutes = prepTime.toIntOrNull()?.takeIf { minutes -> minutes > 0 },
    tags = tags,
    ingredients = ingredients
        .filter { draft -> draft.name.isNotBlank() }
        .map { draft ->
            Ingredient(
                name = draft.name.trim(),
                quantity = if (draft.unit == IngredientUnit.ToTaste) null else parseQuantity(draft.quantity),
                unit = draft.unit,
            )
        },
    steps = steps.map { step -> step.text.trim() }.filter { step -> step.isNotEmpty() },
)

private const val MAX_QUANTITY_LENGTH = 8

/** Keeps what can be a quantity: digits and a single "." or ",", so what is typed or pasted is never silently dropped on save. */
internal fun sanitizeQuantity(text: String): String {
    var separatorSeen = false
    return text.filter { character ->
        when {
            character.isDigit() -> true

            (character == '.' || character == ',') && !separatorSeen -> {
                separatorSeen = true
                true
            }

            else -> false
        }
    }.take(MAX_QUANTITY_LENGTH)
}
