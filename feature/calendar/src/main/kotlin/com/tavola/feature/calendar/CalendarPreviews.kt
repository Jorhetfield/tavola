package com.tavola.feature.calendar

import androidx.compose.runtime.Composable
import com.tavola.core.designsystem.calendar.DayState
import com.tavola.core.designsystem.preview.TavolaScreenPreviews
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.RecipeRef
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private val PreviewToday = LocalDate(2026, 10, 21)
private val PreviewWeekStart = LocalDate(2026, 10, 19)

private val gazpacho = PlannedMeal.FromRecipe(RecipeRef("gazpacho", "Gazpacho", prepTimeMinutes = 15, tags = listOf("Español")))
private val paella = PlannedMeal.FromRecipe(RecipeRef("paella", "Paella valenciana", prepTimeMinutes = 50, tags = listOf("Español")))
private val moussaka = PlannedMeal.FromRecipe(RecipeRef("moussaka", "Moussaka", prepTimeMinutes = 70, tags = listOf("Griego")))
private val risotto = PlannedMeal.FromRecipe(RecipeRef("risotto", "Risotto de setas", prepTimeMinutes = 40, tags = listOf("Italiano")))

private fun previewState(weekStart: LocalDate): CalendarUiState.Success {
    val plans = listOf(
        DayPlan(weekStart, gazpacho, risotto, listOf(DayTask("a", "Comprar pan", done = true))),
        DayPlan(weekStart.plus(1, DateTimeUnit.DAY), paella, tasks = listOf(DayTask("b", "Sacar pollo a descongelar"))),
        DayPlan(weekStart.plus(2, DateTimeUnit.DAY), moussaka, risotto),
        DayPlan(weekStart.plus(3, DateTimeUnit.DAY)),
        DayPlan(weekStart.plus(4, DateTimeUnit.DAY), paella, gazpacho),
        DayPlan(weekStart.plus(5, DateTimeUnit.DAY), risotto, moussaka),
        DayPlan(weekStart.plus(6, DateTimeUnit.DAY), moussaka, PlannedMeal.Quick("Comemos fuera", note = "Con amigos")),
    )
    return CalendarUiState.Success(
        weekStart = weekStart,
        days = plans.map { plan ->
            val state = when {
                plan.date < PreviewToday -> DayState.Past
                plan.date == PreviewToday -> DayState.Today
                else -> DayState.Upcoming
            }
            DayItem(plan, state)
        },
        isCurrentWeek = weekStart == PreviewWeekStart,
    )
}

@TavolaScreenPreviews
@Composable
private fun CalendarContentPreview() {
    TavolaTheme { CalendarContent(previewState(PreviewWeekStart), CalendarActions()) }
}

@TavolaScreenPreviews
@Composable
private fun CalendarContentOtherWeekPreview() {
    TavolaTheme { CalendarContent(previewState(PreviewWeekStart.plus(7, DateTimeUnit.DAY)), CalendarActions()) }
}

@TavolaScreenPreviews
@Composable
private fun CalendarContentLoadingPreview() {
    TavolaTheme { CalendarContent(CalendarUiState.Loading, CalendarActions()) }
}

@TavolaScreenPreviews
@Composable
private fun CalendarContentErrorPreview() {
    TavolaTheme { CalendarContent(CalendarUiState.Error, CalendarActions()) }
}
