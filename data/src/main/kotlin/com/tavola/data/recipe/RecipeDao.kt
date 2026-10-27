package com.tavola.data.recipe

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal abstract class RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY title COLLATE NOCASE")
    abstract fun observeAll(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    abstract fun observeById(id: String): Flow<RecipeEntity?>

    @Upsert
    abstract suspend fun upsert(recipe: RecipeEntity)

    @Upsert
    abstract suspend fun upsertAll(recipes: List<RecipeEntity>)

    @Query("DELETE FROM recipes WHERE id = :id")
    abstract suspend fun deleteById(id: String)

    /** Planned meals keep a copy of the recipe's title, time and tags; this brings that copy up to date after an edit. */
    @Query("UPDATE planned_meals SET title = :title, prepTimeMinutes = :prepTimeMinutes, tagsJson = :tagsJson WHERE recipeId = :recipeId")
    abstract suspend fun refreshPlannedMeals(recipeId: String, title: String, prepTimeMinutes: Int?, tagsJson: String)

    /** Stores [recipe] and refreshes the planned meals that use it, in one transaction, so the calendar never shows stale data. */
    @Transaction
    open suspend fun save(recipe: RecipeEntity) {
        upsert(recipe)
        refreshPlannedMeals(recipe.id, recipe.title, recipe.prepTimeMinutes, recipe.tagsJson)
    }
}
