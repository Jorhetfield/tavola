package com.tavola.data.recipe

import com.tavola.data.JsonColumns
import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.IngredientUnit
import com.tavola.domain.model.Recipe
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

private val stringListSerializer = ListSerializer(String.serializer())
private val ingredientListSerializer = ListSerializer(IngredientJson.serializer())

/** Storage shape of an [Ingredient]. The unit is stored by name so reordering the enum never corrupts data. */
@Serializable
private data class IngredientJson(val name: String, val quantity: Double? = null, val unit: String)

internal fun Recipe.toEntity(): RecipeEntity = RecipeEntity(
    id = id,
    title = title,
    prepTimeMinutes = prepTimeMinutes,
    tagsJson = JsonColumns.encode(stringListSerializer, tags),
    ingredientsJson = JsonColumns.encode(
        ingredientListSerializer,
        ingredients.map { ingredient -> IngredientJson(ingredient.name, ingredient.quantity, ingredient.unit.name) },
    ),
    stepsJson = JsonColumns.encode(stringListSerializer, steps),
)

/** A malformed JSON column is read as an empty list instead of failing, so the recipe still opens. */
internal fun RecipeEntity.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    prepTimeMinutes = prepTimeMinutes,
    tags = JsonColumns.decodeOrElse(stringListSerializer, tagsJson, fallback = emptyList()),
    ingredients = JsonColumns.decodeOrElse(ingredientListSerializer, ingredientsJson, fallback = emptyList()).map { stored ->
        Ingredient(stored.name, stored.quantity, unitNamed(stored.unit))
    },
    steps = JsonColumns.decodeOrElse(stringListSerializer, stepsJson, fallback = emptyList()),
)

/** An unknown unit name (for example from a newer app version) falls back to "to taste" instead of failing. */
private fun unitNamed(name: String): IngredientUnit =
    IngredientUnit.entries.firstOrNull { unit -> unit.name == name } ?: IngredientUnit.ToTaste
