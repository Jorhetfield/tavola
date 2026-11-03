package com.tavola.screenshottests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.tavola.core.designsystem.calendar.MealSlotModel
import com.tavola.core.designsystem.calendar.MealType
import com.tavola.core.designsystem.calendar.TaskChipModel
import com.tavola.core.designsystem.calendar.TavolaAddTaskChip
import com.tavola.core.designsystem.calendar.TavolaDayCard
import com.tavola.core.designsystem.calendar.TavolaMealSlot
import com.tavola.core.designsystem.calendar.TavolaTaskChip
import com.tavola.core.designsystem.calendar.TavolaWeekSelector
import com.tavola.core.designsystem.calendar.WeekSelectorModel
import com.tavola.core.designsystem.preview.TavolaFontScalePreviews
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

@PreviewTest
@TavolaPreviews
@Composable
fun DayCardStatesScreenshot() {
    TavolaTheme {
        DayCards()
    }
}

/** The same cards with large text: this is where clipping shows. */
@PreviewTest
@TavolaFontScalePreviews
@Composable
fun DayCardLargeFontScreenshot() {
    TavolaTheme {
        DayCards()
    }
}

@PreviewTest
@TavolaPreviews
@Composable
fun MealSlotScreenshot() {
    TavolaTheme {
        Row(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md),
        ) {
            TavolaMealSlot(
                model = MealSlotModel(MealType.Lunch, "Paella valenciana", "50 min · Español"),
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            TavolaMealSlot(
                model = MealSlotModel(MealType.Dinner),
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@PreviewTest
@TavolaPreviews
@Composable
fun TaskChipScreenshot() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        ) {
            TavolaTaskChip(TaskChipModel("1", "Sacar pollo a descongelar"), onCheckedChange = {}, onRemoveClick = {})
            TavolaTaskChip(TaskChipModel("2", "Comprar pimientos", done = true), onCheckedChange = {}, onRemoveClick = {})
        }
    }
}

/** The last chip of the task list, next to a task so both can be compared. */
@PreviewTest
@TavolaPreviews
@Composable
fun AddTaskChipScreenshot() {
    TavolaTheme {
        FlowRow(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        ) {
            TavolaTaskChip(TaskChipModel("1", "Comprar pimientos"), onCheckedChange = {}, onRemoveClick = {})
            TavolaAddTaskChip(onClick = {})
        }
    }
}

/** Slots of a day that is over: no touch response, a dash where nothing was planned. */
@PreviewTest
@TavolaPreviews
@Composable
fun ReadOnlyMealSlotScreenshot() {
    TavolaTheme {
        Row(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md),
        ) {
            TavolaMealSlot(
                model = MealSlotModel(MealType.Lunch, "Gazpacho", "15 min", recipeId = "gazpacho"),
                onClick = null,
                modifier = Modifier.weight(1f),
            )
            TavolaMealSlot(model = MealSlotModel(MealType.Dinner), onClick = null, modifier = Modifier.weight(1f))
        }
    }
}

@PreviewTest
@TavolaPreviews
@Composable
fun WeekSelectorScreenshot() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.lg),
        ) {
            TavolaWeekSelector(WeekSelectorModel("19 – 25 oct", isCurrentWeek = true), {}, {}, {})
            TavolaWeekSelector(WeekSelectorModel("26 oct – 1 nov", isCurrentWeek = false), {}, {}, {})
        }
    }
}

@Composable
private fun DayCards() {
    Column(
        modifier = Modifier.padding(TavolaTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.lg),
    ) {
        ScreenshotSamples.days.forEach { day ->
            TavolaDayCard(day, onMealClick = {}, onTaskCheckedChange = { _, _ -> }, onTaskRemoveClick = {}, onAddTaskClick = {})
        }
    }
}

/** Past days collapsed: date, weekday and the two meals; a dash for an empty slot and how many tasks are left. */
@PreviewTest
@TavolaPreviews
@Composable
fun DayCardCollapsedScreenshot() {
    TavolaTheme { CollapsedDayCards() }
}

/** The same collapsed days with large text: the names must be cut with an ellipsis and the header must keep its target. */
@PreviewTest
@TavolaFontScalePreviews
@Composable
fun DayCardCollapsedLargeFontScreenshot() {
    TavolaTheme { CollapsedDayCards() }
}

@Composable
private fun CollapsedDayCards() {
    Column(
        modifier = Modifier.padding(TavolaTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md),
    ) {
        ScreenshotSamples.collapsedDays.forEach { day ->
            TavolaDayCard(day, onMealClick = {}, onTaskCheckedChange = { _, _ -> }, onTaskRemoveClick = {}, onAddTaskClick = {})
        }
        // The same first day once opened: the chevron turned and the meals and the tasks are back.
        TavolaDayCard(
            model = ScreenshotSamples.collapsedDays.first().copy(expanded = true),
            onMealClick = {},
            onTaskCheckedChange = { _, _ -> },
            onTaskRemoveClick = {},
            onAddTaskClick = {},
        )
    }
}
