package com.tavola.domain.repository

import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.WeekPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** The calendar of the group of the signed-in person, shared by its members. The real implementation (Firestore) lives in `:data`. */
interface CalendarRepository {
    /** Emite la semana que empieza en [weekStart] cada vez que cambia. */
    fun observeWeek(weekStart: LocalDate): Flow<WeekPlan>

    suspend fun setTaskDone(date: LocalDate, taskId: String, done: Boolean)

    /** Añade una tarea rápida (pendiente) al día [date], al final de las que ya tiene. */
    suspend fun addTask(date: LocalDate, text: String)

    /** Changes the text of the task [taskId] of the day [date]; its place and whether it is done stay as they were. */
    suspend fun updateTaskText(date: LocalDate, taskId: String, text: String)

    /** Removes the task [taskId] from the day [date]. */
    suspend fun removeTask(date: LocalDate, taskId: String)

    /** Puts [task] back on the day [date] (undo of [removeTask]): it goes where its creation time puts it, as it was. */
    suspend fun restoreTask(date: LocalDate, task: DayTask)

    /** Pone [meal] (receta o comida rápida) en la comida o cena de [date]; con `null` deja el hueco vacío. */
    suspend fun setMeal(date: LocalDate, type: MealType, meal: PlannedMeal?)
}
