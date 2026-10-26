package com.tavola.feature.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tavola.domain.model.Recipe
import com.tavola.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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

/** The recipe book tab: every recipe, filtered by the search field. */
@HiltViewModel
class RecipeListViewModel @Inject constructor(recipeRepository: RecipeRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val retryCount = MutableStateFlow(0)

    /** Every recipe; `null` if the repository failed to load them. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val recipes = retryCount.flatMapLatest {
        recipeRepository.observeRecipes()
            .map<List<Recipe>, List<Recipe>?> { loaded -> loaded }
            .catch { emit(null) }
    }

    val uiState: StateFlow<RecipeListUiState> = combine(recipes, query) { all, text ->
        if (all == null) {
            RecipeListUiState.Error
        } else {
            RecipeListUiState.Success(
                query = text,
                recipes = all.filter { recipe -> recipe.matches(text) }.sortedBy { recipe -> recipe.title.normalizedForSearch() },
                totalRecipes = all.size,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), RecipeListUiState.Loading)

    fun onQueryChange(text: String) = query.update { text }

    fun onRetryClick() = retryCount.update { count -> count + 1 }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
