package com.tavola.data.recipe

import com.tavola.domain.model.Recipe
import com.tavola.domain.repository.RecipeRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
internal class RoomRecipeRepository @Inject constructor(private val recipeDao: RecipeDao) : RecipeRepository {
    override fun observeRecipes(): Flow<List<Recipe>> = recipeDao.observeAll().map { entities -> entities.map(RecipeEntity::toDomain) }

    override fun observeRecipe(id: String): Flow<Recipe?> = recipeDao.observeById(id).map { entity -> entity?.toDomain() }

    override suspend fun saveRecipe(recipe: Recipe): String {
        val saved = if (recipe.id.isBlank()) recipe.copy(id = UUID.randomUUID().toString()) else recipe
        recipeDao.save(saved.toEntity())
        return saved.id
    }

    override suspend fun deleteRecipe(id: String) {
        recipeDao.deleteById(id)
    }
}
