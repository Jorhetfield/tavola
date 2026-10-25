package com.tavola.core.testing.fake

import com.tavola.core.testing.data.MediterraneanSampleData
import com.tavola.domain.model.Recipe
import com.tavola.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRecipeRepository(initialRecipes: List<Recipe> = MediterraneanSampleData.allRecipes) : RecipeRepository {
    private val recipes = MutableStateFlow(initialRecipes)

    override fun observeRecipes(): Flow<List<Recipe>> = recipes
}
