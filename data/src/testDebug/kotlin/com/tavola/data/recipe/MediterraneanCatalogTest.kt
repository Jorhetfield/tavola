package com.tavola.data.recipe

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MediterraneanCatalogTest {
    @Test
    fun `the sample catalog has complete and unique recipes`() {
        val recipes = MediterraneanCatalog.recipes

        assertEquals(12, recipes.size)
        assertEquals(recipes.size, recipes.map { recipe -> recipe.id }.toSet().size)
        assertTrue(recipes.all { recipe -> recipe.ingredients.isNotEmpty() && recipe.steps.isNotEmpty() })
    }
}
