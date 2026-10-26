package com.tavola.feature.recipes

import androidx.compose.runtime.Immutable
import com.tavola.domain.model.Recipe

sealed interface RecipeListUiState {
    data object Loading : RecipeListUiState

    data object Error : RecipeListUiState

    /**
     * @property query text of the search field.
     * @property recipes recipes matching the search, sorted by title.
     * @property totalRecipes how many recipes exist in total (to tell "there are none" from "no results").
     */
    @Immutable
    data class Success(val query: String, val recipes: List<Recipe>, val totalRecipes: Int) : RecipeListUiState
}
