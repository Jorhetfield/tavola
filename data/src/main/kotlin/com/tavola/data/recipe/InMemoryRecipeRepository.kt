package com.tavola.data.recipe

import com.tavola.domain.model.RecipeRef
import com.tavola.domain.repository.RecipeRepository
import javax.inject.Inject

internal class InMemoryRecipeRepository @Inject constructor() : RecipeRepository {
    override suspend fun getSuggestions(): List<RecipeRef> = MediterraneanCatalog.recipes
}
