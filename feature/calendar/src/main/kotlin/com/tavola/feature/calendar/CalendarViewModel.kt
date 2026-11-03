package com.tavola.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tavola.core.designsystem.calendar.DayState
import com.tavola.domain.model.MealType
import com.tavola.domain.model.WeekPlan
import com.tavola.domain.repository.CalendarRepository
import com.tavola.domain.time.TodayProvider
import com.tavola.domain.time.plusWeeks
import com.tavola.domain.time.startOfWeek
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

@HiltViewModel
class CalendarViewModel @Inject constructor(private val calendarRepository: CalendarRepository, private val todayProvider: TodayProvider) :
    ViewModel() {
    private var visits = 0
    private val selectedWeek = MutableStateFlow(SelectedWeek(todayProvider.today().startOfWeek(), visit = visits))
    private val taskDialog = MutableStateFlow<TaskDialog?>(null)
    private val pickerSlot = MutableStateFlow<Pair<LocalDate, MealType>?>(null)
    private val removedTask = MutableStateFlow<RemovedTask?>(null)
    private val openDays = MutableStateFlow(OpenDays(visit = visits, dates = emptySet()))
    private val retryCount = MutableStateFlow(0)

    /** The chosen week; `null` if the repository failed to load it. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val week = combine(selectedWeek, retryCount) { selected, _ -> selected }
        .flatMapLatest { selected ->
            calendarRepository.observeWeek(selected.start)
                .map<WeekPlan, LoadedWeek?> { plan -> LoadedWeek(plan, selected.visit) }
                .catch { emit(null) }
        }

    val uiState: StateFlow<CalendarUiState> = combine(week, taskDialog, pickerSlot, removedTask, openDays) {
            week,
            dialog,
            slot,
            removed,
            open,
        ->
        if (week == null) CalendarUiState.Error else week.toSuccess(dialog, slot, removed, open)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), CalendarUiState.Loading)

    fun onPreviousWeekClick() = showWeek(selectedWeek.value.start.plusWeeks(-1))

    fun onNextWeekClick() = showWeek(selectedWeek.value.start.plusWeeks(1))

    fun onTodayClick() = showWeek(todayProvider.today().startOfWeek())

    /** Opens or closes a past day. Today and the days to come are always shown in full, so asking for one of them does nothing. */
    fun onDayToggle(date: LocalDate) {
        if (date >= todayProvider.today()) return
        val visit = selectedWeek.value.visit
        openDays.update { open ->
            val dates = if (open.visit == visit) open.dates else emptySet()
            OpenDays(visit, if (date in dates) dates - date else dates + date)
        }
    }

    /**
     * Changes the week shown. Every move is a new visit, and the open days belong to the visit they were opened in: the week
     * that slides out keeps showing its open days until its data is replaced, and the week that comes in (even one seen before)
     * starts with every past day collapsed.
     */
    private fun showWeek(weekStart: LocalDate) {
        if (weekStart == selectedWeek.value.start) return
        selectedWeek.value = SelectedWeek(weekStart, ++visits)
    }

    fun onRetryClick() {
        // The failure cancelled the notice, so the pending undo is stale.
        removedTask.value = null
        retryCount.update { count -> count + 1 }
    }

    fun onTaskCheckedChange(date: LocalDate, taskId: String, checked: Boolean) {
        viewModelScope.launch { calendarRepository.setTaskDone(date, taskId, checked) }
    }

    /** Removes the task and keeps it in [removedTask] so it can be undone. */
    fun onTaskRemove(date: LocalDate, taskId: String) {
        val success = uiState.value as? CalendarUiState.Success ?: return
        val tasks = success.days.firstOrNull { item -> item.date == date }?.plan?.tasks ?: return
        val index = tasks.indexOfFirst { task -> task.id == taskId }
        if (index < 0) return

        removedTask.value = RemovedTask(date, tasks[index], index)
        viewModelScope.launch { calendarRepository.removeTask(date, taskId) }
    }

    fun onTaskRemoveUndo() {
        val removed = removedTask.value ?: return
        removedTask.value = null
        viewModelScope.launch { calendarRepository.restoreTask(removed.date, removed.task, removed.index) }
    }

    /** The "task removed" notice ended without undo: there is nothing left to restore. */
    fun onTaskRemovedShown() {
        removedTask.value = null
    }

    /** Opens the meal picker for this slot (empty or full). The picker screen is the one that chooses and saves. */
    fun onMealClick(date: LocalDate, type: MealType) {
        pickerSlot.value = date to type
    }

    fun onMealPickerDismiss() {
        pickerSlot.value = null
    }

    fun onAddTaskClick(date: LocalDate) {
        taskDialog.value = TaskDialog.Add(date)
    }

    /** Opens the task dialog with the current text of the task. A day that is over is a record, so its tasks cannot be edited. */
    fun onTaskEditClick(date: LocalDate, taskId: String) {
        if (date < todayProvider.today()) return
        val success = uiState.value as? CalendarUiState.Success ?: return
        val task = success.days.firstOrNull { item -> item.date == date }?.plan?.tasks?.firstOrNull { dayTask -> dayTask.id == taskId }
        if (task != null) taskDialog.value = TaskDialog.Edit(EditingTask(date, task.id, task.text))
    }

    /** Closes the task dialog, whether it was adding a task or editing one. */
    fun onTaskDialogDismiss() {
        taskDialog.value = null
    }

    /**
     * Saves what the task dialog asked for: a new task, or the new text of the one being edited. Blank text is ignored, and line
     * breaks (from a paste or a hardware keyboard) become spaces, because a task is one line and its chip shows only the first.
     */
    fun onTaskDialogConfirm(text: String) {
        val dialog = taskDialog.value ?: return
        val task = text.trim().replace(LINE_BREAKS, " ")
        if (task.isEmpty()) return

        taskDialog.value = null
        when (dialog) {
            is TaskDialog.Add -> viewModelScope.launch { calendarRepository.addTask(dialog.date, task) }

            is TaskDialog.Edit -> if (task != dialog.task.text) {
                viewModelScope.launch { calendarRepository.updateTaskText(dialog.task.date, dialog.task.taskId, task) }
            }
        }
    }

    private fun LoadedWeek.toSuccess(
        dialog: TaskDialog?,
        slot: Pair<LocalDate, MealType>?,
        removed: RemovedTask?,
        open: OpenDays,
    ): CalendarUiState.Success {
        val today = todayProvider.today()
        val openDates = if (open.visit == visit) open.dates else emptySet()
        val items = plan.days.map { day ->
            val state = when {
                day.date < today -> DayState.Past
                day.date == today -> DayState.Today
                else -> DayState.Upcoming
            }
            // A past day starts collapsed and is open only while the user has opened it; the others are always in full.
            DayItem(day, state, expanded = state != DayState.Past || day.date in openDates)
        }
        return CalendarUiState.Success(
            weekStart = plan.weekStart,
            days = items,
            isCurrentWeek = plan.weekStart == today.startOfWeek(),
            addTaskDate = (dialog as? TaskDialog.Add)?.date,
            editTask = (dialog as? TaskDialog.Edit)?.task,
            removedTask = removed,
            mealPicker = slot?.let { (date, type) ->
                MealPickerTarget(date, type, items.firstOrNull { item -> item.date == date }?.plan?.meal(type))
            },
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L

        /** A run of line breaks with the spaces around it. */
        val LINE_BREAKS = Regex("\\s*[\\r\\n]+\\s*")
    }
}

/** The task dialog that is open: for a new task on a day, or for the text of an existing one. */
private sealed interface TaskDialog {
    data class Add(val date: LocalDate) : TaskDialog

    data class Edit(val task: EditingTask) : TaskDialog
}

/** The week the user is looking at. [visit] is new every time they move to a week, even one they had seen before. */
private data class SelectedWeek(val start: LocalDate, val visit: Int)

/** A week as loaded, with the visit it was asked for. */
private class LoadedWeek(val plan: WeekPlan, val visit: Int)

/** The past days the user has opened during [visit]; days opened in an earlier visit do not count. */
private data class OpenDays(val visit: Int, val dates: Set<LocalDate>)
