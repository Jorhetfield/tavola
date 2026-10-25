package com.tavola.core.testing.fake

import com.tavola.core.testing.data.MediterraneanSampleData
import com.tavola.domain.model.RecipeRef
import com.tavola.domain.repository.RecipeRepository

class FakeRecipeRepository(private val suggestions: List<RecipeRef> = MediterraneanSampleData.allRecipes) : RecipeRepository {
    override suspend fun getSuggestions(): List<RecipeRef> = suggestions
}
