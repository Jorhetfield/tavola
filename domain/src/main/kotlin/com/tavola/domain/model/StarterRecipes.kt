package com.tavola.domain.model

import com.tavola.domain.model.IngredientUnit.Gram
import com.tavola.domain.model.IngredientUnit.Kilogram
import com.tavola.domain.model.IngredientUnit.Liter
import com.tavola.domain.model.IngredientUnit.Piece
import com.tavola.domain.model.IngredientUnit.Tablespoon
import com.tavola.domain.model.IngredientUnit.ToTaste

/**
 * The example recipes a new group starts with, so its recipe book is not empty the first time it is opened. They are ordinary
 * recipes: the person who creates the group can edit or delete them like any other. They have no id, so each group gets its own.
 */
object StarterRecipes {
    private val spain = listOf("Español")

    val all: List<Recipe> = listOf(
        Recipe(
            id = "",
            title = "Gazpacho",
            prepTimeMinutes = 15,
            tags = spain,
            ingredients = listOf(
                Ingredient("Tomates maduros", 1.0, Kilogram),
                Ingredient("Pepino", 1.0, Piece),
                Ingredient("Aceite de oliva", 4.0, Tablespoon),
                Ingredient("Sal", unit = ToTaste),
            ),
            steps = listOf("Trocea las verduras.", "Tritura todo con el aceite y la sal.", "Enfría al menos dos horas."),
        ),
        Recipe(
            id = "",
            title = "Tortilla de patatas",
            prepTimeMinutes = 30,
            tags = spain,
            ingredients = listOf(
                Ingredient("Patatas", 600.0, Gram),
                Ingredient("Huevos", 6.0, Piece),
                Ingredient("Cebolla", 1.0, Piece),
            ),
            steps = listOf("Fríe la patata y la cebolla a fuego lento.", "Mezcla con los huevos batidos.", "Cuaja por ambos lados."),
        ),
        Recipe(
            id = "",
            title = "Paella valenciana",
            prepTimeMinutes = 50,
            tags = spain,
            ingredients = listOf(
                Ingredient("Arroz bomba", 400.0, Gram),
                Ingredient("Pollo troceado", 500.0, Gram),
                Ingredient("Judías verdes", 200.0, Gram),
                Ingredient("Caldo", 1.0, Liter),
            ),
            steps = listOf("Sofríe el pollo y las judías.", "Añade el arroz y el caldo.", "Cocina sin remover y deja reposar."),
        ),
    )
}
