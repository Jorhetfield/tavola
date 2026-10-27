package com.tavola.feature.calendar

import com.tavola.domain.model.MealType
import kotlinx.datetime.LocalDate

/** Everything the screen can ask of its ViewModel. Does nothing by default (previews and screenshots). */
data class CalendarActions(
    val onPreviousWeekClick: () -> Unit = {},
    val onNextWeekClick: () -> Unit = {},
    val onTodayClick: () -> Unit = {},
    val onDayToggle: (date: LocalDate) -> Unit = {},
    val onRetryClick: () -> Unit = {},
    val onMealClick: (date: LocalDate, type: MealType) -> Unit = { _, _ -> },
    val onMealPickerDismiss: () -> Unit = {},
    val onTaskCheckedChange: (date: LocalDate, taskId: String, checked: Boolean) -> Unit = { _, _, _ -> },
    val onTaskRemove: (date: LocalDate, taskId: String) -> Unit = { _, _ -> },
    val onTaskRemoveUndo: () -> Unit = {},
    val onTaskRemovedShown: () -> Unit = {},
    val onAddTaskClick: (date: LocalDate) -> Unit = {},
    val onAddTaskDismiss: () -> Unit = {},
    val onAddTaskConfirm: (text: String) -> Unit = {},
)
