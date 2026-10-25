package com.tavola.feature.calendar

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import com.tavola.core.designsystem.calendar.DayCardModel
import com.tavola.core.designsystem.calendar.MealSlotModel
import com.tavola.core.designsystem.calendar.MealType as UiMealType
import com.tavola.core.designsystem.calendar.TaskChipModel
import com.tavola.domain.model.MealType as DomainMealType
import com.tavola.domain.model.PlannedMeal
import java.util.Locale

@Composable
internal fun rememberLocale(): Locale = LocalConfiguration.current.locales[0]

/** Convierte los días del dominio en los modelos del design system (con textos según el idioma actual). */
@Composable
internal fun rememberDayCardModels(days: List<DayItem>): List<DayCardModel> {
    val locale = rememberLocale()
    val resources = LocalResources.current
    return remember(days, locale) { days.map { day -> day.toCardModel(locale, resources) } }
}

internal fun DayItem.toCardModel(locale: Locale, resources: Resources): DayCardModel = DayCardModel(
    id = date.toString(),
    weekday = date.weekdayName(locale),
    dayNumber = date.dayOfMonth.toString(),
    state = state,
    lunch = plan.lunch.toSlotModel(UiMealType.Lunch, resources),
    dinner = plan.dinner.toSlotModel(UiMealType.Dinner, resources),
    tasks = plan.tasks.map { task -> TaskChipModel(id = task.id, text = task.text, done = task.done) },
)

private fun PlannedMeal?.toSlotModel(type: UiMealType, resources: Resources): MealSlotModel = when (this) {
    null -> MealSlotModel(type)

    is PlannedMeal.FromRecipe -> {
        val details = listOfNotNull(
            recipe.prepTimeMinutes?.let { minutes -> resources.getString(R.string.calendar_prep_time, minutes) },
            recipe.tags.firstOrNull(),
        )
        MealSlotModel(type = type, title = title, subtitle = details.joinToString(" · ").ifEmpty { null })
    }

    is PlannedMeal.Quick -> MealSlotModel(type = type, title = title, subtitle = note)
}

internal fun UiMealType.toDomain(): DomainMealType = when (this) {
    UiMealType.Lunch -> DomainMealType.Lunch
    UiMealType.Dinner -> DomainMealType.Dinner
}
