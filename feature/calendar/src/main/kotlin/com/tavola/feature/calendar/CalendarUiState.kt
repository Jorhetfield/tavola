package com.tavola.feature.calendar

import androidx.compose.runtime.Immutable
import com.tavola.core.designsystem.calendar.DayState
import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import kotlinx.datetime.LocalDate

/** Un día de la semana más su estado respecto a hoy (pasado, hoy o próximo). */
@Immutable
data class DayItem(val plan: DayPlan, val state: DayState) {
    val date: LocalDate get() = plan.date
}

/** Hueco elegido para rellenar o cambiar: qué día, si es comida o cena y qué tiene ahora ([current], `null` si está vacío). */
@Immutable
data class MealPickerTarget(val date: LocalDate, val type: MealType, val current: PlannedMeal?)

/** Tarea eliminada de [date] que estaba en la posición [index]; guarda lo necesario para deshacerlo. */
@Immutable
data class RemovedTask(val date: LocalDate, val task: DayTask, val index: Int)

sealed interface CalendarUiState {
    data object Loading : CalendarUiState

    data object Error : CalendarUiState

    /**
     * @property weekStart lunes de la semana mostrada.
     * @property isCurrentWeek true si la semana mostrada contiene el día de hoy.
     * @property addTaskDate día para el que está abierto el diálogo de nueva tarea, o `null` si está cerrado.
     * @property mealPicker hueco para el que está abierto el selector de comida, o `null` si está cerrado.
     * @property removedTask última tarea eliminada, mientras se puede deshacer; `null` si no hay nada que deshacer.
     */
    @Immutable
    data class Success(
        val weekStart: LocalDate,
        val days: List<DayItem>,
        val isCurrentWeek: Boolean,
        val addTaskDate: LocalDate? = null,
        val mealPicker: MealPickerTarget? = null,
        val removedTask: RemovedTask? = null,
    ) : CalendarUiState
}
