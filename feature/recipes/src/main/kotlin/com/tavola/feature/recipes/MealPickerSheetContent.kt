package com.tavola.feature.recipes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import kotlinx.datetime.LocalDate

/**
 * Selector de comida listo para colocar dentro de una hoja inferior. Al elegir, guardar o quitar escribe en el calendario
 * y llama a [onClose] (que debe cerrar la hoja con su animación).
 */
@Composable
fun MealPickerSheetContent(
    date: LocalDate,
    type: MealType,
    current: PlannedMeal?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealPickerViewModel = hiltViewModel(key = "mealPicker-$date-$type"),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, date, type, onClose) {
        MealPickerActions(
            onQueryChange = viewModel::onQueryChange,
            onRecipeClick = { recipe -> viewModel.onRecipeSelected(date, type, recipe, onClose) },
            onQuickConfirm = { title, note -> viewModel.onQuickMealConfirmed(date, type, title, note, onClose) },
            onClearClick = { viewModel.onClearMeal(date, type, onClose) },
            onRetryClick = viewModel::onRetryClick,
        )
    }
    MealPickerContent(date, type, current, state, actions, modifier)
}
