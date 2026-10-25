package com.tavola.core.testing.data

import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.RecipeRef
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** Datos de ejemplo mediterráneos para tests, previews y pantallas con datos fake. */
object MediterraneanSampleData {
    /** Un lunes: la semana de ejemplo va del 19 al 25 de octubre de 2026. */
    val weekStart = LocalDate(2026, 10, 19)

    val gazpacho = RecipeRef("gazpacho", "Gazpacho", prepTimeMinutes = 15, tags = listOf("Español"))
    val tortilla = RecipeRef("tortilla", "Tortilla de patatas", prepTimeMinutes = 30, tags = listOf("Español"))
    val paella = RecipeRef("paella", "Paella valenciana", prepTimeMinutes = 50, tags = listOf("Español"))
    val moussaka = RecipeRef("moussaka", "Moussaka", prepTimeMinutes = 70, tags = listOf("Griego"))
    val risotto = RecipeRef("risotto", "Risotto de setas", prepTimeMinutes = 40, tags = listOf("Italiano"))
    val greekSalad = RecipeRef("greek-salad", "Ensalada griega", prepTimeMinutes = 10, tags = listOf("Griego"))
    val pomodoro = RecipeRef("pomodoro", "Pasta al pomodoro", prepTimeMinutes = 25, tags = listOf("Italiano"))
    val pizza = RecipeRef("pizza", "Pizza margarita", prepTimeMinutes = 45, tags = listOf("Italiano"))
    val tiramisu = RecipeRef("tiramisu", "Tiramisú", prepTimeMinutes = 30, tags = listOf("Italiano"))
    val tzatziki = RecipeRef("tzatziki", "Tzatziki con pita", prepTimeMinutes = 15, tags = listOf("Griego"))

    val allRecipes = listOf(
        gazpacho,
        tortilla,
        paella,
        moussaka,
        risotto,
        greekSalad,
        pomodoro,
        pizza,
        tiramisu,
        tzatziki,
    )

    /** Semana completa de [start]; el jueves queda sin planificar a propósito. */
    fun week(start: LocalDate = weekStart): List<DayPlan> = listOf(
        DayPlan(start.day(0), gazpacho, tortilla, listOf(DayTask("t-huevos", "Comprar huevos", done = true))),
        DayPlan(
            date = start.day(1),
            lunch = paella,
            tasks = listOf(
                DayTask("t-pollo", "Sacar pollo a descongelar"),
                DayTask("t-pimientos", "Comprar pimientos", done = true),
            ),
        ),
        DayPlan(start.day(2), moussaka, risotto, listOf(DayTask("t-masa", "Preparar la masa"))),
        DayPlan(start.day(3)),
        DayPlan(start.day(4), greekSalad, pomodoro),
        DayPlan(start.day(5), pizza, tiramisu),
        DayPlan(start.day(6), paella, tzatziki),
    )

    private fun LocalDate.day(offset: Int): LocalDate = plus(offset, DateTimeUnit.DAY)
}
