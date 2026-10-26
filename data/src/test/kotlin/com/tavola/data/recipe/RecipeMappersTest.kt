package com.tavola.data.recipe

import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.IngredientUnit
import com.tavola.domain.model.Recipe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RecipeMappersTest {
    private val recipe = Recipe(
        id = "paella",
        title = "Paella valenciana",
        prepTimeMinutes = 50,
        tags = listOf("Español", "Arroz"),
        ingredients = listOf(
            Ingredient("Arroz bomba", 400.0, IngredientUnit.Gram),
            Ingredient("Azafrán", 1.0, IngredientUnit.Pinch),
            Ingredient("Sal", unit = IngredientUnit.ToTaste),
        ),
        steps = listOf("Sofríe el pollo.", "Añade el arroz, y el caldo \"caliente\"."),
    )

    @Test
    fun `a recipe survives a round trip through its entity`() {
        assertEquals(recipe, recipe.toEntity().toDomain())
    }

    @Test
    fun `an empty recipe round trips with no prep time`() {
        val empty = Recipe(id = "empty", title = "Vacía")

        assertEquals(empty, empty.toEntity().toDomain())
    }

    @Test
    fun `an unknown ingredient unit falls back to to taste`() {
        val entity = recipe.toEntity().copy(ingredientsJson = """[{"name":"Mystery","quantity":2.0,"unit":"Bushel"}]""")

        assertEquals(listOf(Ingredient("Mystery", 2.0, IngredientUnit.ToTaste)), entity.toDomain().ingredients)
    }

    @Test
    fun `a malformed json column is read as empty instead of failing`() {
        val damaged = recipe.toEntity().copy(tagsJson = "", ingredientsJson = "not json", stepsJson = "{\"oops\":1}")

        val read = damaged.toDomain()

        assertEquals(recipe.copy(tags = emptyList(), ingredients = emptyList(), steps = emptyList()), read)
    }

    @Test
    fun `unknown json keys from a newer version are ignored`() {
        val entity = recipe.toEntity().copy(ingredientsJson = """[{"name":"Sal","unit":"ToTaste","note":"fine"}]""")

        assertEquals(listOf(Ingredient("Sal", null, IngredientUnit.ToTaste)), entity.toDomain().ingredients)
    }
}
