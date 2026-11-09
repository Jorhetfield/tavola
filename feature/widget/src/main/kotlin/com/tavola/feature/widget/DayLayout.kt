package com.tavola.feature.widget

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.tavola.domain.model.DayTask

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

/** The most tasks the widget shows. */
internal const val MAX_VISIBLE_TASKS = 2

// What the parts of the day take, in dp, as the layout draws them. They are measured by eye on a phone, not by the system, so they
// are a little generous: a part that takes less than thought only leaves a gap, while one that takes more would be cut.
private const val FULL_HEADER = 14f + 46f
private const val COMPACT_HEADER = 10f + 40f
private const val FULL_SLOTS = 10f + 118f
private const val SHORT_SLOTS = 6f + 52f
private const val NARROW_SLOTS = 6f + 52f + 6f + 52f
private const val TASK_GAP = 8f
private const val TASK_BASE = 18f
private const val TASK_LINE = 17f
private const val CHIP_SIDES = 2 * 14f + 2 * 12f + 20f + 10f
private const val AVERAGE_CHARACTER = 7f
private const val MINIMUM_CHARACTERS_PER_LINE = 8
private const val TASK_MAX_LINES = 2
private const val BOTTOM_PADDING = 14f

/** Left free at the bottom, in dp, so a rounding in the estimate does not cut the last chip. */
private const val SAFETY_MARGIN = 4f

/**
 * The tasks the widget shows, or `null` when it shows none. It never scrolls: it shows the first [MAX_VISIBLE_TASKS] tasks that are
 * still to do (a task that is done leaves the widget), and only if the widget is tall enough to show two of them under the header and
 * the slots. If it is not, the tasks are left out altogether, even when there is only one. An empty list means there is room for the
 * tasks but none is pending, which the widget says in a line.
 *
 * [size] is the size of the widget and [fontScale] how much the text is scaled, since the chips take more room with large text.
 */
internal fun visibleTasks(layout: DayLayout, tasks: List<DayTask>, size: DpSize, fontScale: Float): List<DayTask>? {
    val scale = fontScale.coerceAtLeast(1f)
    val pending = tasks.filter { task -> !task.done }.take(MAX_VISIBLE_TASKS)
    val charactersPerLine = ((size.width.value - CHIP_SIDES) / (AVERAGE_CHARACTER * scale)).toInt().coerceAtLeast(
        MINIMUM_CHARACTERS_PER_LINE,
    )

    // The room two chips need: the real ones, and a one-line chip for each that is missing.
    val chips = pending.map { task -> if (task.text.length > charactersPerLine) TASK_MAX_LINES else 1 } +
        List(MAX_VISIBLE_TASKS - pending.size) { 1 }
    val needed = chips.sumOf { lines -> (TASK_GAP + TASK_BASE + TASK_LINE * lines).toDouble() }.toFloat() * scale

    val header = (if (layout == DayLayout.Full) FULL_HEADER else COMPACT_HEADER) * scale
    val slots = when (layout) {
        DayLayout.Full -> FULL_SLOTS
        DayLayout.Short -> SHORT_SLOTS
        DayLayout.Narrow -> NARROW_SLOTS
    } * scale

    val fits = header + slots + needed + BOTTOM_PADDING + SAFETY_MARGIN <= size.height.value
    return if (fits) pending else null
}
