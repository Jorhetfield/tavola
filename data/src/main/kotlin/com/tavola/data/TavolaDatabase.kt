package com.tavola.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tavola.data.recipe.RecipeDao
import com.tavola.data.recipe.RecipeEntity

/** The app's local database. Schemas are exported to `data/schemas` so every version change gets a migration. */
@Database(entities = [RecipeEntity::class], version = 1, exportSchema = true)
internal abstract class TavolaDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
}
