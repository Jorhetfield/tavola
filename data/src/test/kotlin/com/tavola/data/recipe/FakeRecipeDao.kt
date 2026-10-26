package com.tavola.data.recipe

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [RecipeDao] that sorts like the real query, so [RoomRecipeRepository] can be tested on the JVM. */
internal class FakeRecipeDao : RecipeDao {
    private val stored = MutableStateFlow<List<RecipeEntity>>(emptyList())

    override fun observeAll(): Flow<List<RecipeEntity>> = stored.map { entities ->
        entities.sortedBy { entity -> entity.title.lowercase() }
    }

    override fun observeById(id: String): Flow<RecipeEntity?> = stored.map { entities ->
        entities.firstOrNull { entity -> entity.id == id }
    }

    override suspend fun upsert(recipe: RecipeEntity) = upsertAll(listOf(recipe))

    override suspend fun upsertAll(recipes: List<RecipeEntity>) {
        stored.update { entities ->
            entities.filterNot { entity -> recipes.any { recipe -> recipe.id == entity.id } } + recipes
        }
    }

    override suspend fun deleteById(id: String) {
        stored.update { entities -> entities.filterNot { entity -> entity.id == id } }
    }
}
