package com.tavola.core.designsystem.calendar

import androidx.compose.runtime.Immutable

// Modelos de UI del calendario. No dependen de :domain: las pantallas mapean el dominio a estos modelos.

enum class MealType { Lunch, Dinner }

enum class DayState { Past, Today, Upcoming }

/** Hueco de comida o cena. Sin [title] se considera vacío (se pinta como "Añadir"). */
@Immutable
data class MealSlotModel(val type: MealType, val title: String? = null, val subtitle: String? = null) {
    val isEmpty: Boolean get() = title == null
}

@Immutable
data class TaskChipModel(val id: String, val text: String, val done: Boolean = false)

@Immutable
data class DayCardModel(
    val id: String,
    val weekday: String,
    val dayNumber: String,
    val state: DayState,
    val lunch: MealSlotModel = MealSlotModel(MealType.Lunch),
    val dinner: MealSlotModel = MealSlotModel(MealType.Dinner),
    val tasks: List<TaskChipModel> = emptyList(),
) {
    /** Día sin ninguna comida planificada. */
    val isUnplanned: Boolean get() = lunch.isEmpty && dinner.isEmpty
}
