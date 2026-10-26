package com.tavola.data.calendar

import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.RecipeRef
import kotlinx.datetime.LocalDate
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }
private val tagsSerializer = ListSerializer(String.serializer())

internal fun PlannedMeal.toEntity(date: LocalDate, type: MealType): PlannedMealEntity = when (this) {
    is PlannedMeal.FromRecipe -> PlannedMealEntity(
        date = date.toString(),
        type = type.name,
        kind = PlannedMealEntity.KIND_RECIPE,
        title = recipe.title,
        recipeId = recipe.recipeId,
        prepTimeMinutes = recipe.prepTimeMinutes,
        tagsJson = json.encodeToString(tagsSerializer, recipe.tags),
        note = null,
    )

    is PlannedMeal.Quick -> PlannedMealEntity(
        date = date.toString(),
        type = type.name,
        kind = PlannedMealEntity.KIND_QUICK,
        title = title,
        recipeId = null,
        prepTimeMinutes = null,
        tagsJson = json.encodeToString(tagsSerializer, emptyList()),
        note = note,
    )
}

/** A recipe row without its id (corrupt data) is shown as a quick meal rather than dropped. */
internal fun PlannedMealEntity.toDomain(): PlannedMeal {
    val id = recipeId
    return if (kind == PlannedMealEntity.KIND_RECIPE && id != null) {
        PlannedMeal.FromRecipe(RecipeRef(id, title, prepTimeMinutes, json.decodeFromString(tagsSerializer, tagsJson)))
    } else {
        PlannedMeal.Quick(title, note)
    }
}

internal fun DayTask.toEntity(date: LocalDate, position: Int): DayTaskEntity =
    DayTaskEntity(id = id, date = date.toString(), position = position, text = text, done = done)

internal fun DayTaskEntity.toDomain(): DayTask = DayTask(id = id, text = text, done = done)
