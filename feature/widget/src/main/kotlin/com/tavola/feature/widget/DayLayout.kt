package com.tavola.feature.widget

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/** How the day is laid out at the size the widget has now. */
internal enum class DayLayout {
    /** Tall and wide enough for the full slots, side by side. */
    Full,

    /** Short (a 4x2): the slots are compact and side by side. */
    Short,

    /** Narrow (a 2x2): the compact slots go one under the other. */
    Narrow,
}

/** Under this width the two slots do not fit side by side. */
private val NarrowWidth = 200.dp

/** Under this height the full slots do not fit under the header. */
private val ShortHeight = 200.dp

internal fun dayLayoutFor(size: DpSize): DayLayout = when {
    size.width < NarrowWidth -> DayLayout.Narrow
    size.height < ShortHeight -> DayLayout.Short
    else -> DayLayout.Full
}

// What the parts of the day take, in dp, as the layout draws them. They are measured by eye on a phone, not by the system, so they
// are a little generous: a day that is thought not to fit is drawn as a list that scrolls, which is always safe.
private const val FULL_HEADER = 14f + 46f
private const val COMPACT_HEADER = 10f + 40f
private const val FULL_SLOTS = 10f + 118f
private const val SHORT_SLOTS = 6f + 52f
private const val NARROW_SLOTS = 6f + 52f + 6f + 52f
private const val TASK_GAP = 8f
private const val TASK_BASE = 18f
private const val TASK_LINE = 17f
private const val NO_TASKS = 8f + 20f
private const val CARD_PADDING = 14f
private const val CHIP_SIDES = 2 * 14f + 2 * 12f + 20f + 10f
private const val AVERAGE_CHARACTER = 7f
private const val MINIMUM_CHARACTERS_PER_LINE = 8
private const val TASK_MAX_LINES = 2

/**
 * About how tall the whole day is in [layout] at [width], with a chip for each of [taskTexts], when the text is [fontScale] times its
 * normal size. A chip takes two lines when its text is longer than what a line holds.
 */
internal fun estimatedDayHeight(layout: DayLayout, taskTexts: List<String>, width: Dp, fontScale: Float): Dp {
    val top = if (layout == DayLayout.Full) FULL_HEADER else COMPACT_HEADER
    val slots = when (layout) {
        DayLayout.Full -> FULL_SLOTS
        DayLayout.Short -> SHORT_SLOTS
        DayLayout.Narrow -> NARROW_SLOTS
    }
    val charactersPerLine = ((width.value - CHIP_SIDES) / (AVERAGE_CHARACTER * fontScale)).toInt().coerceAtLeast(
        MINIMUM_CHARACTERS_PER_LINE,
    )
    val tasks = if (taskTexts.isEmpty()) {
        NO_TASKS
    } else {
        taskTexts.sumOf { text ->
            val lines = if (text.length > charactersPerLine) TASK_MAX_LINES else 1
            (TASK_GAP + TASK_BASE + TASK_LINE * lines).toDouble()
        }.toFloat()
    }
    return ((top + slots + tasks) * fontScale.coerceAtLeast(1f) + CARD_PADDING).dp
}

/** Whether the whole day is thought to fit in [size], which is when it can be drawn without a list (and so without scrolling). */
internal fun dayFits(layout: DayLayout, taskTexts: List<String>, size: DpSize, fontScale: Float): Boolean =
    estimatedDayHeight(layout, taskTexts, size.width, fontScale) <= size.height - FIT_MARGIN

private val FIT_MARGIN = 8.dp
