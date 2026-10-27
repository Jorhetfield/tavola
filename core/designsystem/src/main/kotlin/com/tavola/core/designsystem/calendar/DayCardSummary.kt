package com.tavola.core.designsystem.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.theme.TavolaTheme

private const val EMPTY_NAME_ALPHA = 0.6f

/**
 * What a collapsed day shows under its header: the lunch and the dinner, each with the name of its recipe or meal (a dash
 * if nothing is planned), and how many tasks are still to do.
 */
@Composable
internal fun DayCardSummary(model: DayCardModel, palette: DayCardPalette, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm)) {
        SummaryMeal(model.lunch, palette)
        SummaryMeal(model.dinner, palette)
        if (model.pendingTasks > 0) {
            PendingTasksBadge(model.pendingTasks, palette)
        }
    }
}

@Composable
private fun SummaryMeal(slot: MealSlotModel, palette: DayCardPalette) {
    val mealName = stringResource(if (slot.type == MealType.Lunch) R.string.meal_slot_lunch else R.string.meal_slot_dinner)
    Row(
        // One stop for screen readers: "Lunch, Gazpacho".
        modifier = Modifier.semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md),
    ) {
        MealBadge(slot.type, contentDescription = mealName)
        Text(
            text = slot.title ?: stringResource(R.string.day_card_summary_empty),
            style = MaterialTheme.typography.titleSmall,
            color = if (slot.isEmpty) palette.content.copy(alpha = EMPTY_NAME_ALPHA) else palette.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PendingTasksBadge(count: Int, palette: DayCardPalette) {
    Text(
        text = pluralStringResource(R.plurals.day_card_pending_tasks, count, count),
        style = MaterialTheme.typography.labelSmall,
        color = palette.content,
        modifier = Modifier
            .clip(CircleShape)
            .background(palette.slotContainer)
            .padding(horizontal = TavolaTheme.spacing.sm, vertical = TavolaTheme.spacing.xxs),
    )
}
