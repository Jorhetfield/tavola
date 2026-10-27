package com.tavola.feature.recipes

import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.IngredientUnit
import com.tavola.domain.model.Recipe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RecipeEditorMappingTest {
    @Test
    fun `quantities accept a comma or a dot and reject anything else`() {
        assertEquals(1.5, parseQuantity("1,5"))
        assertEquals(1.5, parseQuantity(" 1.5 "))
        assertNull(parseQuantity(""))
        assertNull(parseQuantity("abc"))
        assertNull(parseQuantity("0"))
        assertNull(parseQuantity("-2"))
    }

    @Test
    fun `whole quantities are edited without a trailing zero`() {
        assertEquals("400", quantityText(400.0))
        assertEquals("1.25", quantityText(1.25))
    }

    @Test
    fun `the prep time keeps only digits`() {
        assertEquals("45", sanitizePrepTime("4a5 "))
        assertEquals("1234", sanitizePrepTime("123456"))
    }

    @Test
    fun `a recipe survives a round trip through the form`() {
        val recipe = Recipe(
            id = "paella",
            title = "Paella",
            prepTimeMinutes = 50,
            tags = listOf("Espanol"),
            ingredients = listOf(Ingredient("Arroz", 400.0, IngredientUnit.Gram), Ingredient("Sal", unit = IngredientUnit.ToTaste)),
            steps = listOf("Sofreir", "Cocer"),
        )
        var key = 0

        assertEquals(recipe, recipe.toEditing { key++ }.toRecipe("paella"))
    }

    @Test
    fun `empty rows are dropped, a zero time is unset and to taste has no quantity`() {
        val form = RecipeEditorUiState.Editing(
            isNew = true,
            title = "  Tortilla  ",
            prepTime = "0",
            ingredients = listOf(
                IngredientDraft(0, name = "Huevos", quantity = "6", unit = IngredientUnit.Piece),
                IngredientDraft(1, name = "  ", quantity = "3"),
                IngredientDraft(2, name = "Sal", quantity = "9", unit = IngredientUnit.ToTaste),
            ),
            steps = listOf(StepDraft(3, " Batir "), StepDraft(4, "  ")),
        )

        val recipe = form.toRecipe("")

        assertEquals("Tortilla", recipe.title)
        assertNull(recipe.prepTimeMinutes)
        assertEquals(
            listOf(Ingredient("Huevos", 6.0, IngredientUnit.Piece), Ingredient("Sal", null, IngredientUnit.ToTaste)),
            recipe.ingredients,
        )
        assertEquals(listOf("Batir"), recipe.steps)
    }
}
