package com.tavola.feature.widget

import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import kotlinx.datetime.LocalDate

/** What the "Today" widget has to show. It is loaded in one go each time the widget is refreshed. */
internal sealed interface TodayWidgetState {
    /** Nobody is signed in: the widget only invites to open the app. */
    data object SignedOut : TodayWidgetState

    /** The person has not created or joined a group yet. */
    data object NoGroup : TodayWidgetState

    /** The day could not be loaded (no connection and nothing in the cache, say). */
    data object Unavailable : TodayWidgetState

    /** Today's [lunch], [dinner] and [tasks], with the tasks in the order they have in the calendar. */
    data class Content(val date: LocalDate, val lunch: PlannedMeal?, val dinner: PlannedMeal?, val tasks: List<DayTask>) :
        TodayWidgetState {
        /** The meal the small widget shows at [hourOfDay]: lunch until the afternoon, dinner after, whichever of them is planned. */
        fun nextMeal(hourOfDay: Int): Pair<MealType, PlannedMeal>? {
            val lunchPair = lunch?.let { meal -> MealType.Lunch to meal }
            val dinnerPair = dinner?.let { meal -> MealType.Dinner to meal }
            return if (hourOfDay < DINNER_HOUR) lunchPair ?: dinnerPair else dinnerPair ?: lunchPair
        }
    }

    companion object {
        /** From this hour on, "the next meal" is dinner. */
        const val DINNER_HOUR = 16
    }
}
