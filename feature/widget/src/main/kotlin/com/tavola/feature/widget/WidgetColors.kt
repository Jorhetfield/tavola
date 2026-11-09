package com.tavola.feature.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider
import com.tavola.core.designsystem.theme.TavolaColors
import com.tavola.core.designsystem.theme.TavolaDarkExtraColors
import com.tavola.core.designsystem.theme.TavolaLightExtraColors

/**
 * The colors of the day card of the app, for the widget: the card of today (saffron), the olive meal slots and the task chips, in
 * their light and dark versions. The system picks the one that matches the theme of the phone.
 */
internal object WidgetColors {
    val card = provider { dayCardActive }
    val onCard = provider { onDayCardActive }
    val slot = provider { mealSlot }
    val onSlot = provider { onMealSlot }

    /** The slot of a meal that is not planned: the card's own ink, very faint, so it reads as a hollow. */
    val emptySlot = ColorProvider(
        day = TavolaLightExtraColors.onDayCardActive.copy(alpha = EMPTY_SLOT_ALPHA),
        night = TavolaDarkExtraColors.onDayCardActive.copy(alpha = EMPTY_SLOT_ALPHA),
    )
    val chip = provider { taskChip }
    val onChip = provider { onTaskChip }
    val chipDone = provider { taskChipDone }
    val onChipDone = provider { onTaskChipDone }

    private const val EMPTY_SLOT_ALPHA = 0.14f

    private fun provider(select: TavolaColors.() -> Color): ColorProvider =
        ColorProvider(day = TavolaLightExtraColors.select(), night = TavolaDarkExtraColors.select())
}
