package com.tavola.screenshottests

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.tavola.core.designsystem.preview.TavolaAdaptivePreviews
import com.tavola.core.designsystem.preview.TavolaFontScalePreviews
import com.tavola.core.designsystem.preview.TavolaScreenPreviews
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.IngredientUnit
import com.tavola.domain.model.Recipe
import com.tavola.feature.recipes.RecipeDetailContent
import com.tavola.feature.recipes.RecipeDetailUiState
import com.tavola.feature.recipes.RecipeListActions
import com.tavola.feature.recipes.RecipeListContent
import com.tavola.feature.recipes.RecipeListUiState

private val recipes = listOf(
    Recipe("gazpacho", "Gazpacho", 15, listOf("Español"), listOf(Ingredient("Tomates maduros", 1.0, IngredientUnit.Kilogram))),
    Recipe("moussaka", "Moussaka", 70, listOf("Griego"), listOf(Ingredient("Berenjenas", 3.0, IngredientUnit.Piece))),
    Recipe("paella", "Paella valenciana", 50, listOf("Español"), listOf(Ingredient("Arroz bomba", 400.0, IngredientUnit.Gram))),
    Recipe("pizza", "Pizza margarita", 45, listOf("Italiano"), listOf(Ingredient("Harina", 500.0, IngredientUnit.Gram))),
    Recipe("risotto", "Risotto de setas", 40, listOf("Italiano"), listOf(Ingredient("Arroz arborio", 300.0, IngredientUnit.Gram))),
    Recipe("tiramisu", "Tiramisú", 30, listOf("Italiano"), listOf(Ingredient("Mascarpone", 500.0, IngredientUnit.Gram))),
)

private val paella = Recipe(
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
        Ingredient("Pimentón dulce", 1.0, IngredientUnit.Teaspoon),
        Ingredient("Judías verdes", 200.0, IngredientUnit.Gram),
        Ingredient("Sal", unit = IngredientUnit.ToTaste),
    ),
    steps = listOf(
        "Sofríe el pollo en la paella con el aceite hasta que dore.",
        "Añade las judías, el pimentón y el azafrán y rehoga un minuto.",
        "Vierte el caldo caliente, reparte el arroz y cuece 18 minutos sin remover.",
        "Deja reposar cinco minutos tapada con un paño antes de servir.",
    ),
)

private val bareRecipe = Recipe(id = "bare", title = "Receta a medias")

@Composable
private fun RecipeList(state: RecipeListUiState) {
    TavolaTheme { RecipeListContent(state, RecipeListActions()) }
}

@Composable
private fun RecipeDetail(state: RecipeDetailUiState) {
    TavolaTheme { RecipeDetailContent(state, onBack = {}, onRetryClick = {}) }
}

/** The recipe book in phone (light and dark), foldable and tablet. */
@PreviewTest
@TavolaAdaptivePreviews
@Composable
fun RecipeListScreenshot() = RecipeList(RecipeListUiState.Success(query = "", recipes = recipes, totalRecipes = recipes.size))

@PreviewTest
@TavolaScreenPreviews
@Composable
fun RecipeListEmptyScreenshot() = RecipeList(RecipeListUiState.Success(query = "", recipes = emptyList(), totalRecipes = 0))

@PreviewTest
@TavolaScreenPreviews
@Composable
fun RecipeListNoResultsScreenshot() =
    RecipeList(RecipeListUiState.Success(query = "sushi", recipes = emptyList(), totalRecipes = recipes.size))

@PreviewTest
@TavolaScreenPreviews
@Composable
fun RecipeListErrorScreenshot() = RecipeList(RecipeListUiState.Error)

/** Large text: rows and titles must wrap instead of being cut. */
@PreviewTest
@TavolaFontScalePreviews
@Composable
fun RecipeListLargeFontScreenshot() =
    RecipeList(RecipeListUiState.Success(query = "", recipes = recipes.take(3), totalRecipes = recipes.size))

@PreviewTest
@TavolaAdaptivePreviews
@Composable
fun RecipeDetailScreenshot() = RecipeDetail(RecipeDetailUiState.Success(paella))

/** A recipe with nothing but a title: the empty sections explain themselves. */
@PreviewTest
@TavolaScreenPreviews
@Composable
fun RecipeDetailBareScreenshot() = RecipeDetail(RecipeDetailUiState.Success(bareRecipe))

@PreviewTest
@TavolaScreenPreviews
@Composable
fun RecipeDetailNotFoundScreenshot() = RecipeDetail(RecipeDetailUiState.NotFound)

@PreviewTest
@TavolaFontScalePreviews
@Composable
fun RecipeDetailLargeFontScreenshot() = RecipeDetail(RecipeDetailUiState.Success(paella))
