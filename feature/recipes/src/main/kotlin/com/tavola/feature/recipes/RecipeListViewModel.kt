package com.tavola.feature.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tavola.domain.model.Recipe
import com.tavola.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The recipe book tab: every recipe, filtered by the search field, with delete and undo. */
@HiltViewModel
class RecipeListViewModel @Inject constructor(private val recipeRepository: RecipeRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val retryCount = MutableStateFlow(0)
    private val removedRecipe = MutableStateFlow<Recipe?>(null)

    /** Every recipe; `null` if the repository failed to load them. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val recipes = retryCount.flatMapLatest {
        recipeRepository.observeRecipes()
            .map<List<Recipe>, List<Recipe>?> { loaded -> loaded }
            .catch { emit(null) }
    }

    val uiState: StateFlow<RecipeListUiState> = combine(recipes, query, removedRecipe) { all, text, removed ->
        if (all == null) {
            RecipeListUiState.Error
        } else {
            RecipeListUiState.Success(
                query = text,
                recipes = all.filter { recipe -> recipe.matches(text) }.sortedBy { recipe -> recipe.title.normalizedForSearch() },
                totalRecipes = all.size,
                removedRecipe = removed,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), RecipeListUiState.Loading)

    fun onQueryChange(text: String) = query.update { text }

    fun onRetryClick() = retryCount.update { count -> count + 1 }

    /**
     * Deletes the recipe and, once it is gone, keeps it in [removedRecipe] so it can be undone. If the delete fails nothing is
     * announced: the recipe is still in the list and there is nothing to undo.
     */
    fun onRecipeRemove(recipeId: String) {
        val recipe = (uiState.value as? RecipeListUiState.Success)?.recipes?.firstOrNull { shown -> shown.id == recipeId } ?: return
        viewModelScope.launch {
            try {
                recipeRepository.deleteRecipe(recipeId)
                removedRecipe.value = recipe
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Not deleted, so the recipe stays where it is.
            }
        }
    }

    /** Saving the recipe again brings it back with the same id. If that fails the notice comes back so Undo can be tried again. */
    fun onRecipeRemoveUndo() {
        val recipe = removedRecipe.value ?: return
        removedRecipe.value = null
        viewModelScope.launch {
            try {
                recipeRepository.saveRecipe(recipe)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                removedRecipe.value = recipe
            }
        }
    }

    /** The "recipe deleted" notice ended without undo: there is nothing left to restore. */
    fun onRecipeRemovedShown() {
        removedRecipe.value = null
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
