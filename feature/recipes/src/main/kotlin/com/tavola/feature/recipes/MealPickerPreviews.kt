package com.tavola.feature.recipes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.Recipe
import kotlinx.datetime.LocalDate

private val previewDate = LocalDate(2026, 10, 21)

private val previewRecipes = listOf(
    Recipe("gazpacho", "Gazpacho", 15, listOf("Español"), listOf(Ingredient("Tomates maduros"))),
    Recipe("moussaka", "Moussaka", 70, listOf("Griego"), listOf(Ingredient("Berenjenas"))),
    Recipe("paella", "Paella valenciana", 50, listOf("Español"), listOf(Ingredient("Arroz bomba"))),
    Recipe("risotto", "Risotto de setas", 40, listOf("Italiano"), listOf(Ingredient("Arroz arborio"))),
    Recipe("tiramisu", "Tiramisú", 30, listOf("Italiano"), listOf(Ingredient("Mascarpone"))),
)

@Composable
private fun SheetPreview(content: @Composable () -> Unit) {
    TavolaTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(680.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.extraLarge.copy(bottomStart = CornerSize(0f), bottomEnd = CornerSize(0f)),
        ) {
            content()
        }
    }
}

@TavolaPreviews
@Composable
private fun MealPickerRecipesPreview() = SheetPreview {
    MealPickerContent(
        date = previewDate,
        type = MealType.Lunch,
        current = PlannedMeal.FromRecipe(previewRecipes[2].toRef()),
        state = MealPickerUiState.Success(query = "", recipes = previewRecipes, totalRecipes = previewRecipes.size),
        actions = MealPickerActions(),
    )
}

@TavolaPreviews
@Composable
private fun MealPickerEmptyResultsPreview() = SheetPreview {
    MealPickerContent(
        date = previewDate,
        type = MealType.Dinner,
        current = null,
        state = MealPickerUiState.Success(query = "sushi", recipes = emptyList(), totalRecipes = previewRecipes.size),
        actions = MealPickerActions(),
    )
}

@TavolaPreviews
@Composable
private fun MealPickerQuickPreview() = SheetPreview {
    MealPickerContent(
        date = previewDate,
        type = MealType.Dinner,
        current = PlannedMeal.Quick("Sobras de ayer", note = "Con ensalada"),
        state = MealPickerUiState.Success(query = "", recipes = previewRecipes, totalRecipes = previewRecipes.size),
        actions = MealPickerActions(),
    )
}

@TavolaPreviews
@Composable
private fun MealPickerLoadingPreview() = SheetPreview {
    MealPickerContent(previewDate, MealType.Lunch, current = null, state = MealPickerUiState.Loading, actions = MealPickerActions())
}

@TavolaPreviews
@Composable
private fun MealPickerErrorPreview() = SheetPreview {
    MealPickerContent(previewDate, MealType.Lunch, current = null, state = MealPickerUiState.Error, actions = MealPickerActions())
}
