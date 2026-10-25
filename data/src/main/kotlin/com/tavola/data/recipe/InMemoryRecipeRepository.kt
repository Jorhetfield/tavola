package com.tavola.data.recipe

import com.tavola.domain.model.Recipe
import com.tavola.domain.repository.RecipeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class InMemoryRecipeRepository @Inject constructor() : RecipeRepository {
    private val recipes = MutableStateFlow(MediterraneanCatalog.recipes)

    override fun observeRecipes(): Flow<List<Recipe>> = recipes
}
