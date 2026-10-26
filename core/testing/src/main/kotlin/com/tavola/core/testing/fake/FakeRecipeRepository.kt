package com.tavola.core.testing.fake

import com.tavola.core.testing.data.MediterraneanSampleData
import com.tavola.domain.model.Recipe
import com.tavola.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [RecipeRepository] for tests. Starts with the Mediterranean sample recipes unless [initialRecipes] says otherwise. */
class FakeRecipeRepository(initialRecipes: List<Recipe> = MediterraneanSampleData.allRecipes) : RecipeRepository {
    private val recipes = MutableStateFlow(initialRecipes.sortedByTitle())
    private var nextId = 0

    override fun observeRecipes(): Flow<List<Recipe>> = recipes

    override fun observeRecipe(id: String): Flow<Recipe?> = recipes.map { stored -> stored.firstOrNull { recipe -> recipe.id == id } }

    override suspend fun saveRecipe(recipe: Recipe): String {
        val saved = if (recipe.id.isBlank()) recipe.copy(id = "fake-recipe-${nextId++}") else recipe
        recipes.update { stored -> (stored.filterNot { existing -> existing.id == saved.id } + saved).sortedByTitle() }
        return saved.id
    }

    override suspend fun deleteRecipe(id: String) {
        recipes.update { stored -> stored.filterNot { recipe -> recipe.id == id } }
    }

    private fun List<Recipe>.sortedByTitle() = sortedBy { recipe -> recipe.title.lowercase() }
}
