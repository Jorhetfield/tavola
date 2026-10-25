package com.tavola.data.calendar

import com.tavola.data.recipe.MediterraneanCatalog
import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.DayTask
import kotlin.random.Random
import kotlinx.datetime.LocalDate

/**
 * Genera un día de ejemplo a partir de su fecha: la misma fecha da siempre el mismo día, en cualquier semana
 * (pasada o futura). Los días pasados tienen sus tareas hechas. Se sustituye por Firestore en la Fase 3.
 */
internal object FakeDayGenerator {
    private val taskTexts = listOf(
        "Sacar pollo a descongelar",
        "Comprar pimientos",
        "Preparar la masa",
        "Comprar pan",
        "Marinar el pescado",
        "Poner las legumbres en remojo",
    )

    fun day(date: LocalDate, today: LocalDate): DayPlan {
        val random = Random(date.toEpochDays().toInt())
        val lunch = MediterraneanCatalog.recipes.random(random)
        val dinner = MediterraneanCatalog.recipes.random(random)
        val roll = random.nextInt(ROLL_RANGE)
        val taskCount = random.nextInt(MAX_TASKS + 1)
        val tasks = List(taskCount) { index ->
            DayTask(id = "$date-t$index", text = taskTexts.random(random), done = date < today)
        }

        return when {
            roll < UNPLANNED_BELOW -> DayPlan(date)
            roll < NO_DINNER_BELOW -> DayPlan(date, lunch = lunch, tasks = tasks)
            else -> DayPlan(date, lunch = lunch, dinner = dinner, tasks = tasks)
        }
    }

    private const val ROLL_RANGE = 10
    private const val UNPLANNED_BELOW = 1
    private const val NO_DINNER_BELOW = 3
    private const val MAX_TASKS = 2
}
