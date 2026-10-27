package com.tavola.feature.recipes

import com.tavola.domain.model.IngredientUnit

/** Everything the recipe editor can ask of its ViewModel. Does nothing by default (previews and screenshots). */
data class RecipeEditorActions(
    val onTitleChange: (text: String) -> Unit = {},
    val onPrepTimeChange: (text: String) -> Unit = {},
    val onTagInputChange: (text: String) -> Unit = {},
    val onTagAdd: () -> Unit = {},
    val onTagSuggestionClick: (tag: String) -> Unit = {},
    val onTagRemove: (tag: String) -> Unit = {},
    val onIngredientAdd: () -> Unit = {},
    val onIngredientRemove: (key: Int) -> Unit = {},
    val onIngredientNameChange: (key: Int, text: String) -> Unit = { _, _ -> },
    val onIngredientQuantityChange: (key: Int, text: String) -> Unit = { _, _ -> },
    val onIngredientUnitChange: (key: Int, unit: IngredientUnit) -> Unit = { _, _ -> },
    val onStepAdd: () -> Unit = {},
    val onStepRemove: (key: Int) -> Unit = {},
    val onStepChange: (key: Int, text: String) -> Unit = { _, _ -> },
    val onStepMove: (key: Int, offset: Int) -> Unit = { _, _ -> },
    val onSaveClick: () -> Unit = {},
)
