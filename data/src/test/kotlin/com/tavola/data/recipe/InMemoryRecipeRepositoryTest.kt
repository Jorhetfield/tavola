package com.tavola.data.recipe

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InMemoryRecipeRepositoryTest {
    @Test
    fun `el recetario de ejemplo trae recetas completas y unicas`() = runTest {
        InMemoryRecipeRepository().observeRecipes().test {
            val recipes = awaitItem()

            assertEquals(12, recipes.size)
            assertEquals(recipes.size, recipes.map { recipe -> recipe.id }.toSet().size)
            assertTrue(recipes.all { recipe -> recipe.ingredients.isNotEmpty() && recipe.steps.isNotEmpty() })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
