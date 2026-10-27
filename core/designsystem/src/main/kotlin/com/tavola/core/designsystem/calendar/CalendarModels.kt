package com.tavola.core.designsystem.calendar

import androidx.compose.runtime.Immutable

// UI models of the calendar. They do not depend on :domain: the screens map the domain to these models.

enum class MealType { Lunch, Dinner }

enum class DayState { Past, Today, Upcoming }

/** Lunch or dinner slot. Without a [title] it is empty (drawn as "Add"). */
@Immutable
data class MealSlotModel(val type: MealType, val title: String? = null, val subtitle: String? = null) {
    val isEmpty: Boolean get() = title == null
}

@Immutable
data class TaskChipModel(val id: String, val text: String, val done: Boolean = false)

/**
 * A day of the calendar.
 *
 * @property monthShort abbreviated month ("oct"); shown on past days, which have no room for anything else to place the date.
 * @property expanded whether the card shows its meals and tasks. Only past days can be collapsed ([isCollapsible]); today and
 *   the days to come are always shown in full whatever this says.
 */
@Immutable
data class DayCardModel(
    val id: String,
    val weekday: String,
    val dayNumber: String,
    val state: DayState,
    val lunch: MealSlotModel = MealSlotModel(MealType.Lunch),
    val dinner: MealSlotModel = MealSlotModel(MealType.Dinner),
    val tasks: List<TaskChipModel> = emptyList(),
    val monthShort: String = "",
    val expanded: Boolean = true,
) {
    /** Day without any meal planned. */
    val isUnplanned: Boolean get() = lunch.isEmpty && dinner.isEmpty

    /** Only a day that is over can be collapsed. */
    val isCollapsible: Boolean get() = state == DayState.Past

    /** Whether the meals and the tasks are on show. */
    val isShownInFull: Boolean get() = !isCollapsible || expanded

    /** Tasks still to do; a collapsed day keeps showing how many are left. */
    val pendingTasks: Int get() = tasks.count { task -> !task.done }
}
