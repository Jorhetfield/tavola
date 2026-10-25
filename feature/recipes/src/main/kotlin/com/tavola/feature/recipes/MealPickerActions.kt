package com.tavola.feature.recipes

import com.tavola.domain.model.Recipe

/** Todo lo que el selector puede pedirle al ViewModel. Por defecto no hace nada (previews y capturas). */
data class MealPickerActions(
    val onQueryChange: (text: String) -> Unit = {},
    val onRecipeClick: (recipe: Recipe) -> Unit = {},
    val onQuickConfirm: (title: String, note: String) -> Unit = { _, _ -> },
    val onClearClick: () -> Unit = {},
    val onRetryClick: () -> Unit = {},
)
