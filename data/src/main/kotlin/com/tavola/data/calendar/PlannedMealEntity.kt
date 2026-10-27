package com.tavola.data.calendar

import androidx.room.Entity

/** A planned meal: one row per day and slot ([type] is the `MealType` name, [kind] is [KIND_RECIPE] or [KIND_QUICK]). */
@Entity(tableName = "planned_meals", primaryKeys = ["date", "type"])
internal data class PlannedMealEntity(
    val date: String,
    val type: String,
    val kind: String,
    val title: String,
    val recipeId: String?,
    val prepTimeMinutes: Int?,
    val tagsJson: String,
    val note: String?,
) {
    companion object {
        const val KIND_RECIPE = "Recipe"
        const val KIND_QUICK = "Quick"
    }
}
