package com.tavola.feature.recipes

/** Everything the recipe list can ask of its ViewModel. Does nothing by default (previews and screenshots). */
data class RecipeListActions(
    val onQueryChange: (text: String) -> Unit = {},
    val onRecipeClick: (recipeId: String) -> Unit = {},
    val onRecipeRemove: (recipeId: String) -> Unit = {},
    val onRecipeRemoveUndo: () -> Unit = {},
    val onRecipeRemovedShown: () -> Unit = {},
    val onRetryClick: () -> Unit = {},
)
