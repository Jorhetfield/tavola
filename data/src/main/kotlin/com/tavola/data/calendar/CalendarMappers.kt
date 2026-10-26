package com.tavola.data.calendar

import com.tavola.data.JsonColumns
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.RecipeRef
import kotlinx.datetime.LocalDate
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

private val tagsSerializer = ListSerializer(String.serializer())

internal fun PlannedMeal.toEntity(date: LocalDate, type: MealType): PlannedMealEntity = when (this) {
    is PlannedMeal.FromRecipe -> PlannedMealEntity(
        date = date.toString(),
        type = type.name,
        kind = PlannedMealEntity.KIND_RECIPE,
        title = recipe.title,
        recipeId = recipe.recipeId,
        prepTimeMinutes = recipe.prepTimeMinutes,
        tagsJson = JsonColumns.encode(tagsSerializer, recipe.tags),
        note = null,
    )

    is PlannedMeal.Quick -> PlannedMealEntity(
        date = date.toString(),
        type = type.name,
        kind = PlannedMealEntity.KIND_QUICK,
        title = title,
        recipeId = null,
        prepTimeMinutes = null,
        tagsJson = JsonColumns.encode(tagsSerializer, emptyList()),
        note = note,
    )
}

/** A recipe row without its id (corrupt data) is shown as a quick meal, and malformed tags are read as empty, rather than failing. */
internal fun PlannedMealEntity.toDomain(): PlannedMeal {
    val id = recipeId
    return if (kind == PlannedMealEntity.KIND_RECIPE && id != null) {
        PlannedMeal.FromRecipe(
            RecipeRef(id, title, prepTimeMinutes, JsonColumns.decodeOrElse(tagsSerializer, tagsJson, fallback = emptyList())),
        )
    } else {
        PlannedMeal.Quick(title, note)
    }
}

internal fun DayTask.toEntity(date: LocalDate, position: Int): DayTaskEntity =
    DayTaskEntity(id = id, date = date.toString(), position = position, text = text, done = done)

internal fun DayTaskEntity.toDomain(): DayTask = DayTask(id = id, text = text, done = done)
