package com.tavola.feature.widget

import com.tavola.domain.model.PlannedMeal
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaDayOfWeek

/** "MARTES" or "TUESDAY", or "MAR" and "TUE" when [short]: the weekday of [date] in capitals, in the language of [locale]. */
internal fun weekdayName(date: LocalDate, locale: Locale, short: Boolean = false): String =
    date.dayOfWeek.toJavaDayOfWeek().getDisplayName(if (short) JavaTextStyle.SHORT else JavaTextStyle.FULL, locale).uppercase(locale)

/**
 * The line under the title of a meal, as in the day card: a recipe's preparation time and first tag ("50 min · Español"), or the
 * note of a quick meal. `null` when there is nothing to say. [prepTime] writes the minutes in the language of the app.
 */
internal fun mealDetail(meal: PlannedMeal?, prepTime: (minutes: Int) -> String): String? = when (meal) {
    null -> null

    is PlannedMeal.FromRecipe -> listOfNotNull(meal.recipe.prepTimeMinutes?.let(prepTime), meal.recipe.tags.firstOrNull())
        .joinToString(" · ")
        .ifEmpty { null }

    is PlannedMeal.Quick -> meal.note?.trim()?.ifEmpty { null }
}
