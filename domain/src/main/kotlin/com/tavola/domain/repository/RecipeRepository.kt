package com.tavola.domain.repository

import com.tavola.domain.model.RecipeRef

/** Recetas del grupo. Por ahora solo ofrece sugerencias; el CRUD llega con la Fase 3. */
interface RecipeRepository {
    suspend fun getSuggestions(): List<RecipeRef>
}
