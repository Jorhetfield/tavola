package com.tavola.feature.recipes

import androidx.compose.runtime.Composable
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.IngredientUnit
import com.tavola.domain.model.Recipe

private val previewRecipes = listOf(
    Recipe("gazpacho", "Gazpacho", 15, listOf("Español"), listOf(Ingredient("Tomates maduros", 1.0, IngredientUnit.Kilogram))),
    Recipe("moussaka", "Moussaka", 70, listOf("Griego"), listOf(Ingredient("Berenjenas", 3.0, IngredientUnit.Piece))),
    Recipe("paella", "Paella valenciana", 50, listOf("Español"), listOf(Ingredient("Arroz bomba", 400.0, IngredientUnit.Gram))),
    Recipe("tiramisu", "Tiramisú", 30, listOf("Italiano"), listOf(Ingredient("Mascarpone", 500.0, IngredientUnit.Gram))),
)

private val previewDetail = Recipe(
    id = "paella",
    title = "Paella valenciana",
    prepTimeMinutes = 50,
    tags = listOf("Español", "Arroz"),
    ingredients = listOf(
        Ingredient("Arroz bomba", 400.0, IngredientUnit.Gram),
        Ingredient("Caldo de pollo", 1.2, IngredientUnit.Liter),
        Ingredient("Pollo troceado", 1.0, IngredientUnit.Kilogram),
        Ingredient("Azafrán", 1.0, IngredientUnit.Pinch),
        Ingredient("Aceite de oliva", 4.0, IngredientUnit.Tablespoon),
        Ingredient("Sal", unit = IngredientUnit.ToTaste),
    ),
    steps = listOf(
        "Sofríe el pollo en la paella con el aceite hasta que dore.",
        "Añade el arroz y el azafrán y rehoga un minuto.",
        "Vierte el caldo caliente y cuece 18 minutos sin remover.",
    ),
)

@TavolaPreviews
@Composable
private fun RecipeListPreview() {
    TavolaTheme {
        RecipeListContent(
            state = RecipeListUiState.Success(query = "", recipes = previewRecipes, totalRecipes = previewRecipes.size),
            actions = RecipeListActions(),
        )
    }
}

@TavolaPreviews
@Composable
private fun RecipeListEmptyPreview() {
    TavolaTheme {
        RecipeListContent(
            state = RecipeListUiState.Success(query = "", recipes = emptyList(), totalRecipes = 0),
            actions = RecipeListActions(),
        )
    }
}

@TavolaPreviews
@Composable
private fun RecipeDetailPreview() {
    TavolaTheme {
        RecipeDetailContent(state = RecipeDetailUiState.Success(previewDetail), onBack = {}, onRetryClick = {})
    }
}

@TavolaPreviews
@Composable
private fun RecipeDetailNotFoundPreview() {
    TavolaTheme {
        RecipeDetailContent(state = RecipeDetailUiState.NotFound, onBack = {}, onRetryClick = {})
    }
}
