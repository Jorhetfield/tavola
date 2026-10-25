package com.tavola.core.testing.data

import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.IngredientUnit
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.Recipe
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** Datos de ejemplo mediterráneos para tests, previews y pantallas con datos fake. */
object MediterraneanSampleData {
    /** Un lunes: la semana de ejemplo va del 19 al 25 de octubre de 2026. */
    val weekStart = LocalDate(2026, 10, 19)

    val gazpacho = recipe("gazpacho", "Gazpacho", 15, "Español", Ingredient("Tomates maduros", 1.0, IngredientUnit.Kilogram))
    val tortilla = recipe("tortilla", "Tortilla de patatas", 30, "Español", Ingredient("Huevos", 6.0, IngredientUnit.Piece))
    val paella = recipe("paella", "Paella valenciana", 50, "Español", Ingredient("Arroz bomba", 400.0, IngredientUnit.Gram))
    val moussaka = recipe("moussaka", "Moussaka", 70, "Griego", Ingredient("Berenjenas", 3.0, IngredientUnit.Piece))
    val risotto = recipe("risotto", "Risotto de setas", 40, "Italiano", Ingredient("Setas variadas", 300.0, IngredientUnit.Gram))
    val greekSalad = recipe("greek-salad", "Ensalada griega", 10, "Griego", Ingredient("Queso feta", 150.0, IngredientUnit.Gram))
    val pomodoro = recipe("pomodoro", "Pasta al pomodoro", 25, "Italiano", Ingredient("Espaguetis", 400.0, IngredientUnit.Gram))
    val pizza = recipe("pizza", "Pizza margarita", 45, "Italiano", Ingredient("Mozzarella", 250.0, IngredientUnit.Gram))
    val tiramisu = recipe("tiramisu", "Tiramisú", 30, "Italiano", Ingredient("Mascarpone", 500.0, IngredientUnit.Gram))
    val tzatziki = recipe("tzatziki", "Tzatziki con pita", 15, "Griego", Ingredient("Yogur griego", 400.0, IngredientUnit.Gram))

    val allRecipes = listOf(gazpacho, tortilla, paella, moussaka, risotto, greekSalad, pomodoro, pizza, tiramisu, tzatziki)

    val eatingOut = PlannedMeal.Quick("Comemos fuera")

    /** La receta como lo que ocupa un hueco del calendario. */
    fun Recipe.asMeal(): PlannedMeal = PlannedMeal.FromRecipe(toRef())

    /** Semana completa de [start]; el jueves queda sin planificar a propósito y el domingo cena fuera. */
    fun week(start: LocalDate = weekStart): List<DayPlan> = listOf(
        DayPlan(start.day(0), gazpacho.asMeal(), tortilla.asMeal(), listOf(DayTask("t-huevos", "Comprar huevos", done = true))),
        DayPlan(
            date = start.day(1),
            lunch = paella.asMeal(),
            tasks = listOf(
                DayTask("t-pollo", "Sacar pollo a descongelar"),
                DayTask("t-pimientos", "Comprar pimientos", done = true),
            ),
        ),
        DayPlan(start.day(2), moussaka.asMeal(), risotto.asMeal(), listOf(DayTask("t-masa", "Preparar la masa"))),
        DayPlan(start.day(3)),
        DayPlan(start.day(4), greekSalad.asMeal(), pomodoro.asMeal()),
        DayPlan(start.day(5), pizza.asMeal(), tiramisu.asMeal()),
        DayPlan(start.day(6), paella.asMeal(), eatingOut),
    )

    private fun recipe(id: String, title: String, minutes: Int, tag: String, ingredient: Ingredient) =
        Recipe(id = id, title = title, prepTimeMinutes = minutes, tags = listOf(tag), ingredients = listOf(ingredient))

    private fun LocalDate.day(offset: Int): LocalDate = plus(offset, DateTimeUnit.DAY)
}
