package com.tavola.domain.repository

import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.WeekPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Calendario del grupo actual. La implementación real (Firestore) vive en `:data`. */
interface CalendarRepository {
    /** Emite la semana que empieza en [weekStart] cada vez que cambia. */
    fun observeWeek(weekStart: LocalDate): Flow<WeekPlan>

    suspend fun setTaskDone(date: LocalDate, taskId: String, done: Boolean)

    /** Añade una tarea rápida (pendiente) al día [date]. */
    suspend fun addTask(date: LocalDate, text: String)

    /** Removes the task [taskId] from the day [date]. */
    suspend fun removeTask(date: LocalDate, taskId: String)

    /** Puts [task] back on the day [date] at position [index] (undo of [removeTask]). */
    suspend fun restoreTask(date: LocalDate, task: DayTask, index: Int)

    /** Pone [meal] (receta o comida rápida) en la comida o cena de [date]; con `null` deja el hueco vacío. */
    suspend fun setMeal(date: LocalDate, type: MealType, meal: PlannedMeal?)
}
