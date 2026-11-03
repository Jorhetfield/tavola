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
    val onTaskEditClick: (date: LocalDate, taskId: String) -> Unit = { _, _ -> },
    val onAddTaskDismiss: () -> Unit = {},
    val onAddTaskConfirm: (text: String) -> Unit = {},
)

/**
 * The actions of the screen wired to this ViewModel. Every action that the screen can ask for has to be here: the defaults of
 * [CalendarActions] do nothing, so one left out would compile and simply not work (`CalendarActionsTest` checks it).
 */
internal fun CalendarViewModel.toActions(): CalendarActions = CalendarActions(
    onPreviousWeekClick = ::onPreviousWeekClick,
    onNextWeekClick = ::onNextWeekClick,
    onTodayClick = ::onTodayClick,
    onDayToggle = ::onDayToggle,
    onRetryClick = ::onRetryClick,
    onMealClick = ::onMealClick,
    onMealPickerDismiss = ::onMealPickerDismiss,
    onTaskCheckedChange = ::onTaskCheckedChange,
    onTaskRemove = ::onTaskRemove,
    onTaskRemoveUndo = ::onTaskRemoveUndo,
    onTaskRemovedShown = ::onTaskRemovedShown,
    onAddTaskClick = ::onAddTaskClick,
    onTaskEditClick = ::onTaskEditClick,
    onAddTaskDismiss = ::onAddTaskDismiss,
    onAddTaskConfirm = ::onAddTaskConfirm,
)
