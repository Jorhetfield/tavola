package com.tavola.core.testing.fake

import com.tavola.core.testing.data.MediterraneanSampleData
import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.WeekPlan
import com.tavola.domain.repository.CalendarRepository
import com.tavola.domain.time.weekDays
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

/**
 * [CalendarRepository] en memoria para tests. Arranca con la semana mediterránea de ejemplo; pásale
 * [initialDays] para controlar el estado inicial.
 */
class FakeCalendarRepository(initialDays: List<DayPlan> = MediterraneanSampleData.week(), private val groupId: String = FAKE_GROUP_ID) :
    CalendarRepository {
    private val days = MutableStateFlow(initialDays.associateBy { day -> day.date })
    private var nextTaskId = 0

    override fun observeWeek(weekStart: LocalDate): Flow<WeekPlan> = days.map { stored ->
        WeekPlan(
            groupId = groupId,
            weekStart = weekStart,
            days = weekStart.weekDays().map { date -> stored[date] ?: DayPlan(date) },
        )
    }

    override suspend fun setTaskDone(date: LocalDate, taskId: String, done: Boolean) {
        updateDay(date) { day ->
            day.copy(tasks = day.tasks.map { task -> if (task.id == taskId) task.copy(done = done) else task })
        }
    }

    override suspend fun addTask(date: LocalDate, text: String) {
        updateDay(date) { day -> day.copy(tasks = day.tasks + DayTask(id = "fake-task-${nextTaskId++}", text = text)) }
    }

    override suspend fun updateTaskText(date: LocalDate, taskId: String, text: String) {
        updateDay(date) { day ->
            day.copy(tasks = day.tasks.map { task -> if (task.id == taskId) task.copy(text = text) else task })
        }
    }

    override suspend fun removeTask(date: LocalDate, taskId: String) {
        updateDay(date) { day -> day.copy(tasks = day.tasks.filterNot { task -> task.id == taskId }) }
    }

    override suspend fun restoreTask(date: LocalDate, task: DayTask, index: Int) {
        updateDay(date) { day ->
            val restored = day.tasks.toMutableList().apply { add(index.coerceIn(0, size), task) }
            day.copy(tasks = restored)
        }
    }

    override suspend fun setMeal(date: LocalDate, type: MealType, meal: PlannedMeal?) {
        updateDay(date) { day ->
            when (type) {
                MealType.Lunch -> day.copy(lunch = meal)
                MealType.Dinner -> day.copy(dinner = meal)
            }
        }
    }

    private fun updateDay(date: LocalDate, transform: (DayPlan) -> DayPlan) {
        days.update { stored -> stored + (date to transform(stored[date] ?: DayPlan(date))) }
    }

    companion object {
        const val FAKE_GROUP_ID = "fake-group"
    }
}
