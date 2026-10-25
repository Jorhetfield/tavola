package com.tavola.feature.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tavola.core.designsystem.calendar.DayCardModel
import com.tavola.core.designsystem.calendar.DayState
import com.tavola.core.designsystem.calendar.MealSlotModel
import com.tavola.core.designsystem.calendar.MealType
import com.tavola.core.designsystem.calendar.TaskChipModel
import com.tavola.core.designsystem.calendar.TavolaDayCard
import com.tavola.core.designsystem.loading.TavolaLoadingIndicator
import com.tavola.feature.showcase.R

/** Tarjetas de día en sus cuatro aspectos: pasado, hoy, próximo y sin planificar. Interactivas. */
@Composable
internal fun CalendarSection(modifier: Modifier = Modifier) {
    var days by remember { mutableStateOf(sampleDays) }

    ShowcaseSection(titleRes = R.string.showcase_calendar_title, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(ShowcaseDimens.ItemGap)) {
            days.forEach { day ->
                TavolaDayCard(
                    model = day,
                    onMealClick = { type -> days = days.fillMeal(day.id, type) },
                    onTaskCheckedChange = { taskId, checked -> days = days.checkTask(day.id, taskId, checked) },
                    onAddTaskClick = {},
                )
            }
        }
        TavolaLoadingIndicator(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.showcase_loading_label),
        )
    }
}

private fun List<DayCardModel>.checkTask(dayId: String, taskId: String, checked: Boolean) = map { day ->
    if (day.id != dayId) {
        day
    } else {
        day.copy(tasks = day.tasks.map { task -> if (task.id == taskId) task.copy(done = checked) else task })
    }
}

/** Pulsar un hueco vacío lo rellena con una receta de ejemplo para ver el estado lleno. */
private fun List<DayCardModel>.fillMeal(dayId: String, type: MealType) = map { day ->
    if (day.id != dayId) return@map day
    val filled = MealSlotModel(type, title = "Tiramisú", subtitle = "30 min · Italiano")
    when (type) {
        MealType.Lunch -> if (day.lunch.isEmpty) day.copy(lunch = filled) else day
        MealType.Dinner -> if (day.dinner.isEmpty) day.copy(dinner = filled) else day
    }
}

private val sampleDays = listOf(
    DayCardModel(
        id = "mon",
        weekday = "Lunes",
        dayNumber = "20",
        state = DayState.Past,
        lunch = MealSlotModel(MealType.Lunch, "Gazpacho", "15 min · Español"),
        dinner = MealSlotModel(MealType.Dinner, "Tortilla de patatas", "30 min · Español"),
        tasks = listOf(TaskChipModel("t0", "Comprar huevos", done = true)),
    ),
    DayCardModel(
        id = "tue",
        weekday = "Martes",
        dayNumber = "21",
        state = DayState.Today,
        lunch = MealSlotModel(MealType.Lunch, "Paella valenciana", "50 min · Español"),
        dinner = MealSlotModel(MealType.Dinner),
        tasks = listOf(
            TaskChipModel("t1", "Sacar pollo a descongelar"),
            TaskChipModel("t2", "Comprar pimientos", done = true),
        ),
    ),
    DayCardModel(
        id = "wed",
        weekday = "Miércoles",
        dayNumber = "22",
        state = DayState.Upcoming,
        lunch = MealSlotModel(MealType.Lunch, "Moussaka", "70 min · Griego"),
        dinner = MealSlotModel(MealType.Dinner, "Risotto de setas", "40 min · Italiano"),
        tasks = listOf(TaskChipModel("t3", "Preparar la masa")),
    ),
    DayCardModel(id = "thu", weekday = "Jueves", dayNumber = "23", state = DayState.Upcoming),
)
