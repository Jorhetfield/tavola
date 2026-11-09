package com.tavola.feature.recipes

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalResources
import com.tavola.core.designsystem.sheet.TavolaModalBottomSheet
import com.tavola.core.designsystem.sheet.rememberSheetCloser
import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.IngredientUnit
import com.tavola.domain.model.Recipe
import com.tavola.domain.model.ShoppingCandidate

/**
 * Content that adds [candidates] (the ingredients of a recipe) to the shopping list, which `:app` provides from `:feature:shopping` so
 * this module does not know it. It calls its second argument with how many things it added, once they are added.
 */
typealias AddToShoppingSlot = @Composable (candidates: List<ShoppingCandidate>, onClose: (added: Int) -> Unit) -> Unit

/** The ingredient as something to buy: "to taste" ingredients go without a quantity, as they have none to buy. */
internal fun Ingredient.toShoppingCandidate(amount: String?): ShoppingCandidate =
    ShoppingCandidate(name = name.trim(), quantity = if (unit == IngredientUnit.ToTaste) null else amount)

/** The ingredients of [recipe] as things to buy, with their quantities written in the language of the app. */
@Composable
internal fun rememberShoppingCandidates(recipe: Recipe?): List<ShoppingCandidate> {
    val resources = LocalResources.current
    val locale = rememberLocale()
    return remember(recipe, locale) {
        recipe?.ingredients.orEmpty()
            .filter { ingredient -> ingredient.name.isNotBlank() }
            .map { ingredient -> ingredient.toShoppingCandidate(ingredientAmount(ingredient, locale, resources)) }
    }
}

/**
 * The bottom sheet that hosts [content]. It closes with the animation of the theme when the content is done and then calls
 * [onDismiss] with how many things were added (0 if it was swiped away or tapped outside).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddToShoppingHost(candidates: List<ShoppingCandidate>, onDismiss: (added: Int) -> Unit, content: AddToShoppingSlot) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var added by remember { mutableIntStateOf(0) }
    val close = rememberSheetCloser(sheetState, onClosed = { onDismiss(added) })
    TavolaModalBottomSheet(onDismissRequest = { onDismiss(0) }, sheetState = sheetState) {
        content(candidates) { count ->
            added = count
            close()
        }
    }
}
