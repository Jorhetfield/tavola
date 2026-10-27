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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class RecipeDetailViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val gazpacho = MediterraneanSampleData.gazpacho

    @Test
    fun `it shows the recipe with that id`() = runTest {
        RecipeDetailViewModel(gazpacho.id, FakeRecipeRepository()).uiState.test {
            awaitState(RecipeDetailUiState.Success(gazpacho))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `an unknown id is not found`() = runTest {
        RecipeDetailViewModel("does-not-exist", FakeRecipeRepository()).uiState.test {
            awaitState(RecipeDetailUiState.NotFound)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the detail follows edits and turns into not found when the recipe is deleted`() = runTest {
        val repository = FakeRecipeRepository()
        RecipeDetailViewModel(gazpacho.id, repository).uiState.test {
            awaitState(RecipeDetailUiState.Success(gazpacho))

            val edited = gazpacho.copy(title = "Gazpacho andaluz")
            repository.saveRecipe(edited)
            awaitState(RecipeDetailUiState.Success(edited))

            repository.deleteRecipe(gazpacho.id)
            awaitState(RecipeDetailUiState.NotFound)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `if the repository fails it shows the error and retry recovers`() = runTest {
        val flaky = FlakyRecipeRepository()
        val viewModel = RecipeDetailViewModel(gazpacho.id, flaky)
        viewModel.uiState.test {
            awaitState(RecipeDetailUiState.Error)

            flaky.shouldFail = false
            viewModel.onRetryClick()

            awaitState(RecipeDetailUiState.Success(gazpacho))
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** Skips the intermediate states until the one that equals [expected]. */
    private suspend fun ReceiveTurbine<RecipeDetailUiState>.awaitState(expected: RecipeDetailUiState) {
        while (awaitItem() != expected) continue
    }

    private class FlakyRecipeRepository(private val delegate: RecipeRepository = FakeRecipeRepository()) : RecipeRepository by delegate {
        var shouldFail = true

        override fun observeRecipe(id: String): Flow<Recipe?> = flow {
            check(!shouldFail) { "offline" }
            emitAll(delegate.observeRecipe(id))
        }
    }
}
