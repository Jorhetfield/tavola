package com.tavola.feature.recipes

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.tavola.core.testing.data.MediterraneanSampleData
import com.tavola.core.testing.fake.FakeRecipeRepository
import com.tavola.core.testing.rule.MainDispatcherExtension
import com.tavola.domain.model.Recipe
import com.tavola.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class RecipeListViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    @Test
    fun `it lists every recipe sorted by title ignoring case and accents`() = runTest {
        RecipeListViewModel(FakeRecipeRepository()).uiState.test {
            val state = awaitSuccess()

            assertEquals(MediterraneanSampleData.allRecipes.size, state.totalRecipes)
            assertEquals(
                state.recipes.map { recipe -> recipe.title }.sortedBy { title -> title.normalizedForSearch() },
                state.recipes.map { recipe -> recipe.title },
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searching filters by title, tag and ingredient and keeps the total`() = runTest {
        val viewModel = RecipeListViewModel(FakeRecipeRepository())
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onQueryChange("tiramisu")
            val byTitle = awaitSuccess { success -> success.query == "tiramisu" }
            assertEquals(listOf("Tiramisú"), byTitle.recipes.map { recipe -> recipe.title })
            assertEquals(MediterraneanSampleData.allRecipes.size, byTitle.totalRecipes)

            viewModel.onQueryChange("griego")
            val byTag = awaitSuccess { success -> success.query == "griego" }
            assertTrue(byTag.recipes.isNotEmpty() && byTag.recipes.all { recipe -> recipe.tags.contains("Griego") })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `an empty recipe book is a success with no recipes and a zero total`() = runTest {
        RecipeListViewModel(FakeRecipeRepository(initialRecipes = emptyList())).uiState.test {
            val state = awaitSuccess()

            assertTrue(state.recipes.isEmpty())
            assertEquals(0, state.totalRecipes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a recipe saved while the list is open appears in it`() = runTest {
        val repository = FakeRecipeRepository(initialRecipes = emptyList())
        RecipeListViewModel(repository).uiState.test {
            awaitSuccess()

            repository.saveRecipe(Recipe(id = "", title = "Nueva"))

            assertEquals(listOf("Nueva"), awaitSuccess { success -> success.totalRecipes == 1 }.recipes.map { recipe -> recipe.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `if the repository fails it shows the error and retry recovers`() = runTest {
        val flaky = FlakyRecipeRepository()
        val viewModel = RecipeListViewModel(flaky)
        viewModel.uiState.test {
            while (awaitItem() != RecipeListUiState.Error) continue

            flaky.shouldFail = false
            viewModel.onRetryClick()

            assertTrue(awaitSuccess().recipes.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class FlakyRecipeRepository(private val delegate: RecipeRepository = FakeRecipeRepository()) : RecipeRepository by delegate {
        var shouldFail = true

        override fun observeRecipes(): Flow<List<Recipe>> = flow {
            check(!shouldFail) { "offline" }
            emitAll(delegate.observeRecipes())
        }
    }

    /** Skips the intermediate states until the first `Success` that satisfies [predicate]. */
    private suspend fun ReceiveTurbine<RecipeListUiState>.awaitSuccess(
        predicate: (RecipeListUiState.Success) -> Boolean = { true },
    ): RecipeListUiState.Success {
        while (true) {
            val item = awaitItem()
            if (item is RecipeListUiState.Success && predicate(item)) return item
        }
    }
}
