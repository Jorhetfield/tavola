package com.tavola.feature.recipes

import androidx.compose.runtime.Immutable
import com.tavola.domain.model.Recipe

sealed interface MealPickerUiState {
    data object Loading : MealPickerUiState

    data object Error : MealPickerUiState

    /**
     * @property query texto del buscador.
     * @property recipes recetas que cumplen la búsqueda, por orden alfabético.
     * @property totalRecipes cuántas recetas hay en total (para distinguir "no hay ninguna" de "no hay resultados").
     */
    @Immutable
    data class Success(val query: String, val recipes: List<Recipe>, val totalRecipes: Int) : MealPickerUiState
}
