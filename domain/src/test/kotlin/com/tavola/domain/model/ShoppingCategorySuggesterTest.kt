package com.tavola.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ShoppingCategorySuggesterTest {
    @Test
    fun `common Spanish names find their category`() {
        assertEquals(ShoppingCategory.DairyAndEggs, ShoppingCategorySuggester.suggest("Leche"))
        assertEquals(ShoppingCategory.FruitAndVegetables, ShoppingCategorySuggester.suggest("Tomates"))
        assertEquals(ShoppingCategory.MeatAndFish, ShoppingCategorySuggester.suggest("Merluza"))
        assertEquals(ShoppingCategory.Pantry, ShoppingCategorySuggester.suggest("Arroz bomba"))
        assertEquals(ShoppingCategory.Bakery, ShoppingCategorySuggester.suggest("Pan"))
        assertEquals(ShoppingCategory.Drinks, ShoppingCategorySuggester.suggest("Cerveza"))
        assertEquals(ShoppingCategory.Household, ShoppingCategorySuggester.suggest("Detergente"))
        assertEquals(ShoppingCategory.Frozen, ShoppingCategorySuggester.suggest("Helado de vainilla"))
    }

    @Test
    fun `common English names find their category`() {
        assertEquals(ShoppingCategory.DairyAndEggs, ShoppingCategorySuggester.suggest("milk"))
        assertEquals(ShoppingCategory.FruitAndVegetables, ShoppingCategorySuggester.suggest("Strawberry"))
        assertEquals(ShoppingCategory.Drinks, ShoppingCategorySuggester.suggest("sparkling water"))
        assertEquals(ShoppingCategory.MeatAndFish, ShoppingCategorySuggester.suggest("chicken breast"))
    }

    @Test
    fun `case, accents and plurals do not matter`() {
        assertEquals(ShoppingCategory.FruitAndVegetables, ShoppingCategorySuggester.suggest("  TOMATES  "))
        assertEquals(ShoppingCategory.MeatAndFish, ShoppingCategorySuggester.suggest("Mejillones"))
        assertEquals(ShoppingCategory.DairyAndEggs, ShoppingCategorySuggester.suggest("huevos"))
        assertEquals(ShoppingCategory.Pantry, ShoppingCategorySuggester.suggest("Café"))
        assertEquals(ShoppingCategory.FruitAndVegetables, ShoppingCategorySuggester.suggest("Limón"))
    }

    @Test
    fun `the first known word decides`() {
        assertEquals(ShoppingCategory.DairyAndEggs, ShoppingCategorySuggester.suggest("Leche de almendras"))
        assertEquals(ShoppingCategory.Pantry, ShoppingCategorySuggester.suggest("Aceite de oliva"))
    }

    @Test
    fun `a name it does not know has no suggestion`() {
        assertNull(ShoppingCategorySuggester.suggest("Cosa rara"))
        assertNull(ShoppingCategorySuggester.suggest(""))
        assertNull(ShoppingCategorySuggester.suggest("   "))
    }

    @Test
    fun `a word that is only part of a known one is not enough`() {
        assertNull(ShoppingCategorySuggester.suggest("lec"))
        assertNull(ShoppingCategorySuggester.suggest("toma"))
    }
}
