package com.tavola.feature.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.tavola.core.designsystem.field.TavolaTextField
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.IngredientUnit

/** Title, one card per ingredient and the add button. */
internal fun LazyListScope.ingredientsSection(ingredients: List<IngredientDraft>, actions: RecipeEditorActions) {
    item(key = INGREDIENTS_TITLE_KEY) { EditorSectionTitle(R.string.recipe_detail_ingredients, editorItemMotion()) }
    items(ingredients, key = { draft -> "ingredient:${draft.key}" }) { draft ->
        IngredientCard(draft, actions, editorItemMotion())
    }
    item(key = INGREDIENTS_ADD_KEY) { AddRowButton(R.string.recipe_editor_ingredient_add, actions.onIngredientAdd, editorItemMotion()) }
}

@Composable
private fun IngredientCard(draft: IngredientDraft, actions: RecipeEditorActions, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(modifier = Modifier.padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                TavolaTextField(
                    value = draft.name,
                    onValueChange = { text -> actions.onIngredientNameChange(draft.key, text) },
                    label = stringResource(R.string.recipe_editor_ingredient_name),
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { actions.onIngredientRemove(draft.key) }) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.recipe_editor_ingredient_remove))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                if (draft.unit == IngredientUnit.ToTaste) {
                    Box(Modifier.weight(1f))
                } else {
                    TavolaTextField(
                        value = draft.quantity,
                        onValueChange = { text -> actions.onIngredientQuantityChange(draft.key, text) },
                        label = stringResource(R.string.recipe_editor_ingredient_quantity),
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal,
                    )
                }
                UnitPicker(draft.unit) { unit -> actions.onIngredientUnitChange(draft.key, unit) }
            }
        }
    }
}

/** Button showing the current unit; tapping it opens the list of units. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UnitPicker(unit: IngredientUnit, onUnitChange: (IngredientUnit) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilledTonalButton(onClick = { expanded = true }, shapes = ButtonDefaults.shapes()) {
            Text(unit.label())
            Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            IngredientUnit.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label()) },
                    onClick = {
                        onUnitChange(option)
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Short name of the unit as it appears in the picker ("g", "tbsp", "to taste"...). */
@Composable
private fun IngredientUnit.label(): String = stringResource(
    when (this) {
        IngredientUnit.Gram -> R.string.recipe_editor_unit_gram
        IngredientUnit.Kilogram -> R.string.recipe_editor_unit_kilogram
        IngredientUnit.Milliliter -> R.string.recipe_editor_unit_milliliter
        IngredientUnit.Liter -> R.string.recipe_editor_unit_liter
        IngredientUnit.Teaspoon -> R.string.recipe_editor_unit_teaspoon
        IngredientUnit.Tablespoon -> R.string.recipe_editor_unit_tablespoon
        IngredientUnit.Piece -> R.string.recipe_editor_unit_piece
        IngredientUnit.Pinch -> R.string.recipe_editor_unit_pinch
        IngredientUnit.ToTaste -> R.string.ingredient_unit_to_taste
    },
)

private const val INGREDIENTS_TITLE_KEY = "ingredients-title"
private const val INGREDIENTS_ADD_KEY = "ingredients-add"
