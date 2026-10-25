package com.tavola.core.designsystem.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

private val TodayElevation = 8.dp

/**
 * Tarjeta de un día del calendario: cabecera con número grande en serif, comida y cena lado a lado y
 * tareas rápidas debajo. El estado del día cambia su aspecto: hoy (azafrán, destacada), próximo (azul
 * mediterráneo) y pasado (apagada).
 */
@Composable
fun TavolaDayCard(
    model: DayCardModel,
    onMealClick: (MealType) -> Unit,
    onTaskCheckedChange: (taskId: String, checked: Boolean) -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = TavolaTheme.spacing
    val palette = dayCardPalette(model.state)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.container,
        contentColor = palette.content,
        shadowElevation = if (model.state == DayState.Today) TodayElevation else 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            DayCardHeader(model, palette, onAddTaskClick)
            DayCardMeals(model, palette, onMealClick)
            if (model.tasks.isNotEmpty()) {
                DayCardTasks(model.tasks, onTaskCheckedChange)
            }
        }
    }
}

private data class DayCardPalette(val container: Color, val content: Color, val slotContainer: Color, val slotContent: Color)

@Composable
private fun dayCardPalette(state: DayState): DayCardPalette {
    val colors = TavolaTheme.colors
    val target = when (state) {
        DayState.Today -> DayCardPalette(colors.dayCardActive, colors.onDayCardActive, colors.mealSlot, colors.onMealSlot)

        DayState.Upcoming -> DayCardPalette(colors.dayCard, colors.onDayCard, colors.mealSlot, colors.onMealSlot)

        DayState.Past -> DayCardPalette(
            container = colors.dayCardMuted,
            content = colors.onDayCardMuted,
            slotContainer = colors.onDayCardMuted.copy(alpha = 0.12f),
            slotContent = colors.onDayCardMuted,
        )
    }
    return DayCardPalette(
        container = animateColorAsState(target.container, label = "dayCardContainer").value,
        content = animateColorAsState(target.content, label = "dayCardContent").value,
        slotContainer = animateColorAsState(target.slotContainer, label = "dayCardSlot").value,
        slotContent = animateColorAsState(target.slotContent, label = "dayCardSlotContent").value,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DayCardHeader(model: DayCardModel, palette: DayCardPalette, onAddTaskClick: () -> Unit) {
    val spacing = TavolaTheme.spacing
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Text(
            text = model.dayNumber,
            style = MaterialTheme.typography.displaySmall,
            color = palette.content,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.weekday.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = palette.content.copy(alpha = 0.85f),
            )
            DayCaption(model, palette)
        }
        FilledTonalIconButton(onClick = onAddTaskClick, shapes = IconButtonDefaults.shapes()) {
            Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.calendar_add_task))
        }
    }
}

@Composable
private fun DayCaption(model: DayCardModel, palette: DayCardPalette) {
    when {
        model.state == DayState.Today -> TodayBadge(palette)

        model.isUnplanned && model.state != DayState.Past -> Text(
            text = stringResource(R.string.calendar_day_unplanned),
            style = MaterialTheme.typography.bodySmall,
            color = palette.content.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun TodayBadge(palette: DayCardPalette) {
    Text(
        text = stringResource(R.string.calendar_day_today),
        style = MaterialTheme.typography.labelSmall,
        color = palette.container,
        modifier = Modifier
            .padding(top = TavolaTheme.spacing.xs)
            .clip(CircleShape)
            .background(palette.content)
            .padding(horizontal = TavolaTheme.spacing.sm, vertical = TavolaTheme.spacing.xxs),
    )
}

@Composable
private fun DayCardMeals(model: DayCardModel, palette: DayCardPalette, onMealClick: (MealType) -> Unit) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md),
    ) {
        listOf(model.lunch, model.dinner).forEach { slot ->
            TavolaMealSlot(
                model = slot,
                onClick = { onMealClick(slot.type) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                containerColor = palette.slotContainer,
                contentColor = palette.slotContent,
            )
        }
    }
}

@Composable
private fun DayCardTasks(tasks: List<TaskChipModel>, onTaskCheckedChange: (String, Boolean) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
    ) {
        tasks.forEach { task ->
            TavolaTaskChip(model = task, onCheckedChange = { checked -> onTaskCheckedChange(task.id, checked) })
        }
    }
}

private val PreviewDays = listOf(
    DayCardModel(
        id = "mon",
        weekday = "Lunes",
        dayNumber = "20",
        state = DayState.Past,
        lunch = MealSlotModel(MealType.Lunch, "Gazpacho", "15 min"),
        dinner = MealSlotModel(MealType.Dinner, "Tortilla de patatas", "30 min"),
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
    ),
    DayCardModel(id = "thu", weekday = "Jueves", dayNumber = "23", state = DayState.Upcoming),
)

@TavolaPreviews
@Composable
private fun TavolaDayCardPreview() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.lg),
        ) {
            PreviewDays.forEach { day ->
                TavolaDayCard(day, onMealClick = {}, onTaskCheckedChange = { _, _ -> }, onAddTaskClick = {})
            }
        }
    }
}
