package com.tavola.feature.calendar

import androidx.compose.runtime.Immutable
import com.tavola.core.designsystem.calendar.DayState
import com.tavola.domain.model.DayPlan
import kotlinx.datetime.LocalDate

/** Un día de la semana más su estado respecto a hoy (pasado, hoy o próximo). */
@Immutable
data class DayItem(val plan: DayPlan, val state: DayState) {
    val date: LocalDate get() = plan.date
}

sealed interface CalendarUiState {
    data object Loading : CalendarUiState

    data object Error : CalendarUiState

    /**
     * @property weekStart lunes de la semana mostrada.
     * @property isCurrentWeek true si la semana mostrada contiene el día de hoy.
     * @property addTaskDate día para el que está abierto el diálogo de nueva tarea, o `null` si está cerrado.
     */
    @Immutable
    data class Success(val weekStart: LocalDate, val days: List<DayItem>, val isCurrentWeek: Boolean, val addTaskDate: LocalDate? = null) :
        CalendarUiState
}
