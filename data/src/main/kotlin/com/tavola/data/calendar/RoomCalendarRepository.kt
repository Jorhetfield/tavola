package com.tavola.data.calendar

import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.WeekPlan
import com.tavola.domain.repository.CalendarRepository
import com.tavola.domain.time.weekDays
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

/** Calendar stored in Room. There is a single local group until Firebase brings real ones in Phase 3. */
@Singleton
internal class RoomCalendarRepository @Inject constructor(private val calendarDao: CalendarDao) : CalendarRepository {
    override fun observeWeek(weekStart: LocalDate): Flow<WeekPlan> {
        val days = weekStart.weekDays()
        val from = days.first().toString()
        val to = days.last().toString()
        return combine(calendarDao.observeMeals(from, to), calendarDao.observeTasks(from, to)) { meals, tasks ->
            WeekPlan(
                groupId = GROUP_ID,
                weekStart = weekStart,
                days = days.map { date -> dayPlan(date, meals, tasks) },
            )
        }
    }

    override suspend fun setTaskDone(date: LocalDate, taskId: String, done: Boolean) {
        calendarDao.setTaskDone(taskId, done)
    }

    override suspend fun addTask(date: LocalDate, text: String) {
        calendarDao.appendTask(id = UUID.randomUUID().toString(), date = date.toString(), text = text)
    }

    override suspend fun removeTask(date: LocalDate, taskId: String) {
        calendarDao.deleteTask(taskId)
    }

    override suspend fun restoreTask(date: LocalDate, task: DayTask, index: Int) {
        calendarDao.insertTaskAt(task.toEntity(date, position = index), index)
    }

    override suspend fun setMeal(date: LocalDate, type: MealType, meal: PlannedMeal?) {
        if (meal == null) {
            calendarDao.deleteMeal(date.toString(), type.name)
        } else {
            calendarDao.upsertMeal(meal.toEntity(date, type))
        }
    }

    private fun dayPlan(date: LocalDate, meals: List<PlannedMealEntity>, tasks: List<DayTaskEntity>): DayPlan {
        val isoDate = date.toString()
        val dayMeals = meals.filter { meal -> meal.date == isoDate }
        return DayPlan(
            date = date,
            lunch = dayMeals.firstOrNull { meal -> meal.type == MealType.Lunch.name }?.toDomain(),
            dinner = dayMeals.firstOrNull { meal -> meal.type == MealType.Dinner.name }?.toDomain(),
            tasks = tasks.filter { task -> task.date == isoDate }.map(DayTaskEntity::toDomain),
        )
    }

    private companion object {
        const val GROUP_ID = "local-group"
    }
}
