package com.tavola.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tavola.core.designsystem.calendar.DayState
import com.tavola.domain.model.MealType
import com.tavola.domain.model.WeekPlan
import com.tavola.domain.repository.CalendarRepository
import com.tavola.domain.repository.RecipeRepository
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
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val recipeRepository: RecipeRepository,
    private val todayProvider: TodayProvider,
) : ViewModel() {
    private val selectedWeekStart = MutableStateFlow(todayProvider.today().startOfWeek())
    private val addTaskDate = MutableStateFlow<LocalDate?>(null)
    private val retryCount = MutableStateFlow(0)

    /** La semana elegida; `null` si el repositorio falló al cargarla. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val week = combine(selectedWeekStart, retryCount) { start, _ -> start }
        .flatMapLatest { start ->
            calendarRepository.observeWeek(start)
                .map<WeekPlan, WeekPlan?> { it }
                .catch { emit(null) }
        }

    val uiState: StateFlow<CalendarUiState> = combine(week, addTaskDate) { week, dialogDate ->
        if (week == null) CalendarUiState.Error else week.toSuccess(dialogDate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), CalendarUiState.Loading)

    fun onPreviousWeekClick() = selectedWeekStart.update { it.plusWeeks(-1) }

    fun onNextWeekClick() = selectedWeekStart.update { it.plusWeeks(1) }

    fun onTodayClick() {
        selectedWeekStart.value = todayProvider.today().startOfWeek()
    }

    fun onRetryClick() = retryCount.update { it + 1 }

    fun onTaskCheckedChange(date: LocalDate, taskId: String, checked: Boolean) {
        viewModelScope.launch { calendarRepository.setTaskDone(date, taskId, checked) }
    }

    /** Un hueco vacío se rellena con una receta sugerida (provisional hasta que exista el selector de recetas). */
    fun onMealClick(date: LocalDate, type: MealType) {
        val day = (uiState.value as? CalendarUiState.Success)?.days?.firstOrNull { it.date == date }
        if (day?.plan?.meal(type) != null) return

        viewModelScope.launch {
            val suggestions = recipeRepository.getSuggestions()
            if (suggestions.isEmpty()) return@launch
            val index = Math.floorMod(date.toEpochDays().toInt() + type.ordinal, suggestions.size)
            calendarRepository.setMeal(date, type, suggestions[index])
        }
    }

    fun onAddTaskClick(date: LocalDate) {
        addTaskDate.value = date
    }

    fun onAddTaskDismiss() {
        addTaskDate.value = null
    }

    fun onAddTaskConfirm(text: String) {
        val date = addTaskDate.value ?: return
        val task = text.trim()
        if (task.isEmpty()) return

        addTaskDate.value = null
        viewModelScope.launch { calendarRepository.addTask(date, task) }
    }

    private fun WeekPlan.toSuccess(dialogDate: LocalDate?): CalendarUiState.Success {
        val today = todayProvider.today()
        return CalendarUiState.Success(
            weekStart = weekStart,
            days = days.map { plan ->
                val state = when {
                    plan.date < today -> DayState.Past
                    plan.date == today -> DayState.Today
                    else -> DayState.Upcoming
                }
                DayItem(plan, state)
            },
            isCurrentWeek = weekStart == today.startOfWeek(),
            addTaskDate = dialogDate,
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
