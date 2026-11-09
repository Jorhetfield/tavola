package com.tavola.feature.recipes

import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.IngredientUnit
import com.tavola.domain.model.ShoppingCandidate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AddToShoppingTest {
    @Test
    fun `an ingredient with an amount is offered with it`() {
        val ingredient = Ingredient("Arroz bomba", 400.0, IngredientUnit.Gram)

        assertEquals(ShoppingCandidate("Arroz bomba", "400 g"), ingredient.toShoppingCandidate(amount = "400 g"))
    }

    @Test
    fun `an ingredient to taste is offered without a quantity`() {
        val ingredient = Ingredient("Sal", unit = IngredientUnit.ToTaste)

        assertEquals(ShoppingCandidate("Sal", null), ingredient.toShoppingCandidate(amount = "al gusto"))
    }

    @Test
    fun `an ingredient with no amount to show has no quantity`() {
        val ingredient = Ingredient("Harina", unit = IngredientUnit.Gram)

        assertEquals(ShoppingCandidate("Harina", null), ingredient.toShoppingCandidate(amount = null))
    }

    @Test
    fun `the name is trimmed`() {
        assertEquals("Aceite", Ingredient("  Aceite ").toShoppingCandidate(amount = null).name)
    }
}
