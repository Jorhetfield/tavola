package com.tavola.core.designsystem.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.theme.TavolaTheme

private val CompactBadgeSize = 20.dp
private val CompactBadgeIconSize = 12.dp
private val CounterMinSize = 24.dp
private const val EMPTY_NAME_ALPHA = 0.6f

/**
 * The two meals of a collapsed day, one line each (a small sun or moon and the name of the recipe or meal, a dash if nothing
 * is planned), a little apart. Two lines of text are all the height the collapsed row needs.
 */
@Composable
internal fun CompactMeals(model: DayCardModel, palette: DayCardPalette, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.xs)) {
        CompactMeal(model.lunch, palette)
        CompactMeal(model.dinner, palette)
    }
}

@Composable
private fun CompactMeal(slot: MealSlotModel, palette: DayCardPalette) {
    val mealName = stringResource(if (slot.type == MealType.Lunch) R.string.meal_slot_lunch else R.string.meal_slot_dinner)
    Row(
        // One stop for screen readers: "Lunch, Gazpacho".
        modifier = Modifier.semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
    ) {
        MealBadge(slot.type, contentDescription = mealName, size = CompactBadgeSize, iconSize = CompactBadgeIconSize)
        Text(
            text = slot.title ?: stringResource(R.string.day_card_summary_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = if (slot.isEmpty) palette.content.copy(alpha = EMPTY_NAME_ALPHA) else palette.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** How many tasks are left on a collapsed day: a small round number that grows into a pill for two digits. */
@Composable
internal fun PendingTasksCount(count: Int, palette: DayCardPalette, modifier: Modifier = Modifier) {
    val description = pluralStringResource(R.plurals.day_card_pending_tasks, count, count)
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = CounterMinSize, minHeight = CounterMinSize)
            .clip(CircleShape)
            .background(palette.slotContainer)
            .padding(horizontal = TavolaTheme.spacing.xs)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = count.toString(), style = MaterialTheme.typography.labelSmall, color = palette.content)
    }
}
