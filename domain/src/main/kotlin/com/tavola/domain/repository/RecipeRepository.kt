package com.tavola.domain.repository

import com.tavola.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

/** The group's recipe book. */
interface RecipeRepository {
    /** Every recipe, sorted by title and updated each time one changes. */
    fun observeRecipes(): Flow<List<Recipe>>

    /** The recipe [id], or `null` if it does not exist (or was deleted); emits again each time it changes. */
    fun observeRecipe(id: String): Flow<Recipe?>

    /**
     * Creates or replaces [recipe]. If its id is blank a new id is generated. Returns the id it was saved under.
     * Saving a recipe that was deleted brings it back, which is how undoing a delete works.
     */
    suspend fun saveRecipe(recipe: Recipe): String

    /** Deletes the recipe [id]; does nothing if it does not exist. */
    suspend fun deleteRecipe(id: String)
}
