package com.tavola.data.calendar

import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.RecipeRef
import kotlinx.datetime.LocalDate

// How the calendar of a group is stored in Firestore. The security rules in `firestore.rules` depend on these names and shapes.
//
//   groups/{groupId}/meals/{date}_{type}  { date, type, title, recipeId?, prepTimeMinutes?, tags[], note?, createdBy? }
//   groups/{groupId}/tasks/{taskId}       { date, text, done, createdAt, createdBy? }
//
// `date` is an ISO date (2026-10-21), so a range of dates is a range of strings; `type` is "Lunch" or "Dinner". A meal has the id
// of its slot, so a slot never holds two meals. Every task is a document of its own, so two people can change the tasks of the
// same day at once without overwriting each other.

internal const val MEALS_COLLECTION = "meals"
internal const val TASKS_COLLECTION = "tasks"
internal const val DATE_FIELD = "date"

internal object MealFields {
    const val DATE = DATE_FIELD
    const val TYPE = "type"
    const val TITLE = "title"
    const val RECIPE_ID = "recipeId"
    const val PREP_TIME_MINUTES = "prepTimeMinutes"
    const val TAGS = "tags"
    const val NOTE = "note"
    const val CREATED_BY = "createdBy"
}

internal object TaskFields {
    const val DATE = DATE_FIELD
    const val TEXT = "text"
    const val DONE = "done"
    const val CREATED_AT = "createdAt"
    const val CREATED_BY = "createdBy"
}

/** The id of the document of a slot: the day and the meal ("2026-10-21_Lunch"). */
internal fun mealDocumentId(date: LocalDate, type: MealType): String = "${date}_${type.name}"

internal fun PlannedMeal.toDocument(date: LocalDate, type: MealType, createdBy: String?): Map<String, Any?> = buildMap {
    put(MealFields.DATE, date.toString())
    put(MealFields.TYPE, type.name)
    put(MealFields.TITLE, title)
    when (this@toDocument) {
        is PlannedMeal.FromRecipe -> {
            put(MealFields.RECIPE_ID, recipe.recipeId)
            put(MealFields.PREP_TIME_MINUTES, recipe.prepTimeMinutes)
            put(MealFields.TAGS, recipe.tags)
        }

        is PlannedMeal.Quick -> {
            put(MealFields.TAGS, emptyList<String>())
            put(MealFields.NOTE, note)
        }
    }
    if (createdBy != null) put(MealFields.CREATED_BY, createdBy)
}

/** A meal found in a document, with the slot it belongs to. */
internal data class StoredMeal(val date: LocalDate, val type: MealType, val meal: PlannedMeal)

/**
 * The meal stored in a document, or `null` if it has no valid date, type or title. A document with a recipe id is a recipe (with the
 * copy of its title, time and tags that the calendar draws without loading the recipe); without one it is a quick meal.
 */
internal fun Map<String, Any?>.toStoredMeal(): StoredMeal? {
    val date = (this[MealFields.DATE] as? String)?.let { text -> runCatching { LocalDate.parse(text) }.getOrNull() } ?: return null
    val type = MealType.entries.firstOrNull { entry -> entry.name == this[MealFields.TYPE] } ?: return null
    val title = this[MealFields.TITLE] as? String ?: return null
    val recipeId = this[MealFields.RECIPE_ID] as? String
    val meal = if (recipeId != null) {
        PlannedMeal.FromRecipe(
            RecipeRef(
                recipeId = recipeId,
                title = title,
                prepTimeMinutes = (this[MealFields.PREP_TIME_MINUTES] as? Number)?.toInt(),
                tags = (this[MealFields.TAGS] as? List<*>)?.filterIsInstance<String>().orEmpty(),
            ),
        )
    } else {
        PlannedMeal.Quick(title, this[MealFields.NOTE] as? String)
    }
    return StoredMeal(date, type, meal)
}

/** The document of a task. [createdBy] is only included when it is known, so a write that merges it leaves whoever made the task. */
internal fun DayTask.toDocument(date: LocalDate, createdBy: String?): Map<String, Any?> = buildMap {
    put(TaskFields.DATE, date.toString())
    put(TaskFields.TEXT, text)
    put(TaskFields.DONE, done)
    put(TaskFields.CREATED_AT, createdAt)
    if (createdBy != null) put(TaskFields.CREATED_BY, createdBy)
}

/** A task found in a document, with the day it belongs to. */
internal data class StoredTask(val date: LocalDate, val task: DayTask)

/** The task stored in a document, or `null` if it has no valid date or text. A missing `done` is read as pending. */
internal fun Map<String, Any?>.toStoredTask(id: String): StoredTask? {
    val date = (this[TaskFields.DATE] as? String)?.let { text -> runCatching { LocalDate.parse(text) }.getOrNull() } ?: return null
    val text = this[TaskFields.TEXT] as? String ?: return null
    return StoredTask(
        date = date,
        task = DayTask(
            id = id,
            text = text,
            done = this[TaskFields.DONE] as? Boolean ?: false,
            createdAt = (this[TaskFields.CREATED_AT] as? Number)?.toLong() ?: 0,
            createdBy = this[TaskFields.CREATED_BY] as? String,
        ),
    )
}
