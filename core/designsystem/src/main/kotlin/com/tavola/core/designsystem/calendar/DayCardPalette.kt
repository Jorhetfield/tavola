package com.tavola.core.designsystem.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.tavola.core.designsystem.theme.TavolaTheme

/** The colors of a day card: the card itself and the meal slots inside it. */
internal data class DayCardPalette(val container: Color, val content: Color, val slotContainer: Color, val slotContent: Color)

private const val MUTED_SLOT_ALPHA = 0.12f

/** The palette of a day in [state], animated so that a day that stops being today (or is edited) changes color smoothly. */
@Composable
internal fun dayCardPalette(state: DayState): DayCardPalette {
    val colors = TavolaTheme.colors
    val target = when (state) {
        DayState.Today -> DayCardPalette(colors.dayCardActive, colors.onDayCardActive, colors.mealSlot, colors.onMealSlot)

        DayState.Upcoming -> DayCardPalette(colors.dayCard, colors.onDayCard, colors.mealSlot, colors.onMealSlot)

        DayState.Past -> DayCardPalette(
            container = colors.dayCardMuted,
            content = colors.onDayCardMuted,
            slotContainer = colors.onDayCardMuted.copy(alpha = MUTED_SLOT_ALPHA),
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
