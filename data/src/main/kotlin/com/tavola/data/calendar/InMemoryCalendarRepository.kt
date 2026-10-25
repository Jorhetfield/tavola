package com.tavola.data.calendar

import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.RecipeRef
import com.tavola.domain.model.WeekPlan
import com.tavola.domain.repository.CalendarRepository
import com.tavola.domain.time.TodayProvider
import com.tavola.domain.time.weekDays
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

/**
 * Calendario con datos de ejemplo generados al vuelo ([FakeDayGenerator]) para cualquier semana. Los cambios
 * (tareas, comidas) se guardan encima en memoria y se pierden al cerrar la app. Temporal hasta la Fase 3.
 */
@Singleton
internal class InMemoryCalendarRepository @Inject constructor(private val todayProvider: TodayProvider) : CalendarRepository {
    private val changes = MutableStateFlow<Map<LocalDate, DayPlan>>(emptyMap())
    private var nextTaskId = 0

    override fun observeWeek(weekStart: LocalDate): Flow<WeekPlan> = changes.map { changed ->
        WeekPlan(
            groupId = GROUP_ID,
            weekStart = weekStart,
            days = weekStart.weekDays().map { date -> changed[date] ?: generate(date) },
        )
    }

    override suspend fun setTaskDone(date: LocalDate, taskId: String, done: Boolean) {
        updateDay(date) { day ->
            day.copy(tasks = day.tasks.map { task -> if (task.id == taskId) task.copy(done = done) else task })
        }
    }

    override suspend fun addTask(date: LocalDate, text: String) {
        updateDay(date) { day -> day.copy(tasks = day.tasks + DayTask(id = "added-${nextTaskId++}", text = text)) }
    }

    override suspend fun setMeal(date: LocalDate, type: MealType, recipe: RecipeRef?) {
        updateDay(date) { day ->
            when (type) {
                MealType.Lunch -> day.copy(lunch = recipe)
                MealType.Dinner -> day.copy(dinner = recipe)
            }
        }
    }

    private fun generate(date: LocalDate): DayPlan = FakeDayGenerator.day(date, todayProvider.today())

    private fun updateDay(date: LocalDate, transform: (DayPlan) -> DayPlan) {
        changes.update { changed -> changed + (date to transform(changed[date] ?: generate(date))) }
    }

    private companion object {
        const val GROUP_ID = "in-memory-group"
    }
}
