package com.tavola.screenshottests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.tavola.core.designsystem.calendar.MealSlotModel
import com.tavola.core.designsystem.calendar.MealType
import com.tavola.core.designsystem.calendar.TaskChipModel
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

/** Mismas tarjetas con texto grande: aquí se ven los recortes. */
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
            TavolaTaskChip(TaskChipModel("1", "Sacar pollo a descongelar"), onCheckedChange = {})
            TavolaTaskChip(TaskChipModel("2", "Comprar pimientos", done = true), onCheckedChange = {})
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
            TavolaDayCard(day, onMealClick = {}, onTaskCheckedChange = { _, _ -> }, onAddTaskClick = {})
        }
    }
}
