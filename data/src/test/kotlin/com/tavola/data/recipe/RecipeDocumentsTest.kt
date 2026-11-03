package com.tavola.data.recipe

import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.IngredientUnit
import com.tavola.domain.model.Recipe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RecipeDocumentsTest {
    private val paella = Recipe(
        id = "paella",
        title = "Paella valenciana",
        prepTimeMinutes = 50,
        tags = listOf("Español"),
        ingredients = listOf(Ingredient("Arroz bomba", 400.0, IngredientUnit.Gram), Ingredient("Sal", unit = IngredientUnit.ToTaste)),
        steps = listOf("Sofríe el pollo.", "Añade el arroz."),
        createdBy = "user-marta",
    )

    @Test
    fun `a recipe is read back as it was written, with the author`() {
        val document = paella.toDocument(createdBy = "user-marta")

        assertEquals(paella, document.toRecipe("paella"))
    }

    @Test
    fun `without an author the document leaves the field out, so a merge keeps who made the recipe`() {
        val document = paella.toDocument(createdBy = null)

        assertFalse(document.containsKey("createdBy"))
        assertNull(document.toRecipe("paella")?.createdBy)
    }

    @Test
    fun `the document has the shape the rules ask for`() {
        val document = paella.toDocument(createdBy = "user-marta")

        assertEquals(setOf("title", "prepTimeMinutes", "tags", "ingredients", "steps", "createdBy"), document.keys)
    }

    @Test
    fun `numbers from Firestore come as longs and are read as the right type`() {
        val document = paella.toDocument(createdBy = null).toMutableMap()
        document["prepTimeMinutes"] = 50L
        document["ingredients"] = listOf(mapOf("name" to "Arroz", "quantity" to 400L, "unit" to "Gram"))

        val recipe = document.toRecipe("paella")

        assertEquals(50, recipe?.prepTimeMinutes)
        assertEquals(Ingredient("Arroz", 400.0, IngredientUnit.Gram), recipe?.ingredients?.single())
    }

    @Test
    fun `an unknown unit is read as to taste and an ingredient without a name is dropped`() {
        val document = paella.toDocument(createdBy = null).toMutableMap()
        document["ingredients"] = listOf(
            mapOf("name" to "Azafrán", "unit" to "Pinchito"),
            mapOf("quantity" to 3.0, "unit" to "Gram"),
            "not an ingredient",
        )

        val ingredients = document.toRecipe("paella")?.ingredients

        assertEquals(listOf(Ingredient("Azafrán", null, IngredientUnit.ToTaste)), ingredients)
    }

    @Test
    fun `a document without a title is not a recipe, and any other missing field reads as empty`() {
        assertNull(mapOf<String, Any?>("steps" to listOf("a")).toRecipe("x"))

        val recipe = mapOf<String, Any?>("title" to "Solo título").toRecipe("x")

        assertEquals(Recipe(id = "x", title = "Solo título"), recipe)
    }
}
