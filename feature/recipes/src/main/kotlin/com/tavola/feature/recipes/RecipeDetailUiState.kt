package com.tavola.feature.recipes

import androidx.compose.runtime.Immutable
import com.tavola.domain.model.Recipe

sealed interface RecipeDetailUiState {
    data object Loading : RecipeDetailUiState

    data object Error : RecipeDetailUiState

    /** The recipe does not exist (it was never there or it was deleted while the screen was open). */
    data object NotFound : RecipeDetailUiState

    @Immutable
    data class Success(val recipe: Recipe) : RecipeDetailUiState
}
