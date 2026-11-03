package com.tavola.feature.calendar

import androidx.compose.runtime.Immutable
import com.tavola.core.designsystem.calendar.DayState
import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import kotlinx.datetime.LocalDate

/**
 * A day of the week plus its state relative to today (past, today or upcoming).
 *
 * @property expanded whether the day is shown in full. Only a past day can be collapsed, and it starts collapsed; today and the
 *   days to come are always expanded.
 */
@Immutable
data class DayItem(val plan: DayPlan, val state: DayState, val expanded: Boolean = true) {
    val date: LocalDate get() = plan.date
}

/** Slot to fill or change: which day, lunch or dinner and what it has now ([current], `null` if it is empty). */
@Immutable
data class MealPickerTarget(val date: LocalDate, val type: MealType, val current: PlannedMeal?)

/** Task [taskId] of [date] whose [text] is being edited. */
@Immutable
data class EditingTask(val date: LocalDate, val taskId: String, val text: String)

/** Task removed from [date]; keeps what is needed to undo it (the task carries its creation time, which is its place). */
@Immutable
data class RemovedTask(val date: LocalDate, val task: DayTask)

sealed interface CalendarUiState {
    data object Loading : CalendarUiState

    data object Error : CalendarUiState

    /**
     * @property weekStart Monday of the week shown.
     * @property isCurrentWeek true if the week shown contains today.
     * @property addTaskDate day the new-task dialog is open for, or `null` if it is closed.
     * @property editTask task whose text the dialog is editing, or `null` if no task is being edited.
     * @property mealPicker slot the meal picker is open for, or `null` if it is closed.
     * @property removedTask last removed task while it can still be undone; `null` if there is nothing to undo.
     */
    @Immutable
    data class Success(
        val weekStart: LocalDate,
        val days: List<DayItem>,
        val isCurrentWeek: Boolean,
        val addTaskDate: LocalDate? = null,
        val editTask: EditingTask? = null,
        val mealPicker: MealPickerTarget? = null,
        val removedTask: RemovedTask? = null,
    ) : CalendarUiState
}
