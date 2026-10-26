package com.tavola.data.recipe

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A recipe as stored in Room. Tags, ingredients and steps are kept as JSON text columns: they are always read and
 * written together with the recipe, and the shopping list will aggregate ingredients in memory.
 */
@Entity(tableName = "recipes")
internal data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val prepTimeMinutes: Int?,
    val tagsJson: String,
    val ingredientsJson: String,
    val stepsJson: String,
)
