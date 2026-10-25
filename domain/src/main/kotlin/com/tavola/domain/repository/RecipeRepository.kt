package com.tavola.domain.repository

import com.tavola.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

/** Recetario del grupo. Por ahora solo lectura; crear y editar recetas llega con el editor. */
interface RecipeRepository {
    /** Todas las recetas, actualizadas cada vez que cambian. */
    fun observeRecipes(): Flow<List<Recipe>>
}
