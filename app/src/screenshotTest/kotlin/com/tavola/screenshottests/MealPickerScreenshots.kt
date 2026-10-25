package com.tavola.screenshottests

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.tavola.core.designsystem.preview.TavolaFontScalePreviews
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.Recipe
import com.tavola.feature.recipes.MealPickerActions
import com.tavola.feature.recipes.MealPickerContent
import com.tavola.feature.recipes.MealPickerUiState
import kotlinx.datetime.LocalDate

private val date = LocalDate(2026, 10, 21)

private val recipes = listOf(
    Recipe("gazpacho", "Gazpacho", 15, listOf("Español"), listOf(Ingredient("Tomates maduros"))),
    Recipe("moussaka", "Moussaka", 70, listOf("Griego"), listOf(Ingredient("Berenjenas"))),
    Recipe("paella", "Paella valenciana", 50, listOf("Español"), listOf(Ingredient("Arroz bomba"))),
    Recipe("risotto", "Risotto de setas", 40, listOf("Italiano"), listOf(Ingredient("Arroz arborio"))),
    Recipe("tiramisu", "Tiramisú", 30, listOf("Italiano"), listOf(Ingredient("Mascarpone"))),
)

/** El contenido del selector sobre una superficie con la forma de la hoja (la hoja real es una ventana y no se captura). */
@Composable
private fun SheetSurface(content: @Composable () -> Unit) {
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

@Composable
private fun Picker(current: PlannedMeal?, state: MealPickerUiState) = SheetSurface {
    MealPickerContent(date, MealType.Lunch, current, state, MealPickerActions())
}

@PreviewTest
@TavolaPreviews
@Composable
fun MealPickerRecipesScreenshot() = Picker(
    current = PlannedMeal.FromRecipe(recipes[2].toRef()),
    state = MealPickerUiState.Success(query = "", recipes = recipes, totalRecipes = recipes.size),
)

@PreviewTest
@TavolaPreviews
@Composable
fun MealPickerNoResultsScreenshot() = Picker(
    current = null,
    state = MealPickerUiState.Success(query = "sushi", recipes = emptyList(), totalRecipes = recipes.size),
)

@PreviewTest
@TavolaPreviews
@Composable
fun MealPickerQuickMealScreenshot() = Picker(
    current = PlannedMeal.Quick("Sobras de ayer", note = "Con ensalada"),
    state = MealPickerUiState.Success(query = "", recipes = recipes, totalRecipes = recipes.size),
)

@PreviewTest
@TavolaPreviews
@Composable
fun MealPickerErrorScreenshot() = Picker(current = null, state = MealPickerUiState.Error)

/** Con texto grande se ve si la cabecera, el conmutador y las filas aguantan sin recortarse. */
@PreviewTest
@TavolaFontScalePreviews
@Composable
fun MealPickerLargeFontScreenshot() = Picker(
    current = PlannedMeal.FromRecipe(recipes[2].toRef()),
    state = MealPickerUiState.Success(query = "", recipes = recipes, totalRecipes = recipes.size),
)
