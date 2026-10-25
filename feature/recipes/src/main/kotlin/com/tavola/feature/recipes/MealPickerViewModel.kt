package com.tavola.feature.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.Recipe
import com.tavola.domain.repository.CalendarRepository
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
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * Selector de comida para un hueco del calendario. Escribe directamente en el calendario a través de
 * [CalendarRepository] (el calendario se actualiza solo porque observa un Flow); así las dos features no se conocen.
 */
@HiltViewModel
class MealPickerViewModel @Inject constructor(recipeRepository: RecipeRepository, private val calendarRepository: CalendarRepository) :
    ViewModel() {
    private val query = MutableStateFlow("")
    private val retryCount = MutableStateFlow(0)

    /** Todas las recetas; `null` si el repositorio falló al cargarlas. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val recipes = retryCount.flatMapLatest {
        recipeRepository.observeRecipes()
            .map<List<Recipe>, List<Recipe>?> { it }
            .catch { emit(null) }
    }

    val uiState: StateFlow<MealPickerUiState> = combine(recipes, query) { all, text ->
        if (all == null) {
            MealPickerUiState.Error
        } else {
            MealPickerUiState.Success(
                query = text,
                recipes = all.filter { it.matches(text) }.sortedBy { it.title.normalizedForSearch() },
                totalRecipes = all.size,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), MealPickerUiState.Loading)

    fun onQueryChange(text: String) = query.update { text }

    fun onRetryClick() = retryCount.update { it + 1 }

    /** Pone [recipe] en el hueco y avisa con [onDone] cuando se ha guardado (para cerrar la hoja). */
    fun onRecipeSelected(date: LocalDate, type: MealType, recipe: Recipe, onDone: () -> Unit) {
        viewModelScope.launch {
            calendarRepository.setMeal(date, type, PlannedMeal.FromRecipe(recipe.toRef()))
            onDone()
        }
    }

    /** Anota una comida sin receta. Un título en blanco se ignora; una nota en blanco se guarda como ausente. */
    fun onQuickMealConfirmed(date: LocalDate, type: MealType, title: String, note: String, onDone: () -> Unit) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        viewModelScope.launch {
            calendarRepository.setMeal(date, type, PlannedMeal.Quick(cleanTitle, note.trim().ifEmpty { null }))
            onDone()
        }
    }

    fun onClearMeal(date: LocalDate, type: MealType, onDone: () -> Unit) {
        viewModelScope.launch {
            calendarRepository.setMeal(date, type, null)
            onDone()
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
