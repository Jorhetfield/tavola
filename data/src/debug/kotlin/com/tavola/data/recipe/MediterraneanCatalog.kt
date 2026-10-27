package com.tavola.data.recipe

import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.IngredientUnit
import com.tavola.domain.model.IngredientUnit.Gram
import com.tavola.domain.model.IngredientUnit.Liter
import com.tavola.domain.model.IngredientUnit.Milliliter
import com.tavola.domain.model.IngredientUnit.Piece
import com.tavola.domain.model.IngredientUnit.Tablespoon
import com.tavola.domain.model.IngredientUnit.ToTaste
import com.tavola.domain.model.Recipe

/** Sample recipes (Spain, Italy, Greece) seeded into the database in debug builds only. */
internal object MediterraneanCatalog {
    private val spain = listOf("Español")
    private val greece = listOf("Griego")
    private val italy = listOf("Italiano")

    val recipes = listOf(
        Recipe(
            id = "gazpacho",
            title = "Gazpacho",
            prepTimeMinutes = 15,
            tags = spain,
            ingredients = listOf(
                Ingredient("Tomates maduros", 1.0, IngredientUnit.Kilogram),
                Ingredient("Pepino", 1.0, Piece),
                Ingredient("Aceite de oliva", 4.0, Tablespoon),
                Ingredient("Sal", unit = ToTaste),
            ),
            steps = listOf("Trocea las verduras.", "Tritura todo con el aceite y la sal.", "Enfría al menos dos horas."),
        ),
        Recipe(
            id = "tortilla",
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
            id = "paella",
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
        Recipe(
            id = "pisto",
            title = "Pisto con huevo",
            prepTimeMinutes = 35,
            tags = spain,
            ingredients = listOf(
                Ingredient("Calabacín", 2.0, Piece),
                Ingredient("Pimientos", 2.0, Piece),
                Ingredient("Tomate frito", 200.0, Milliliter),
                Ingredient("Huevos", 4.0, Piece),
            ),
            steps = listOf("Pocha las verduras.", "Añade el tomate y cocina 15 minutos.", "Sirve con un huevo a la plancha."),
        ),
        Recipe(
            id = "moussaka",
            title = "Moussaka",
            prepTimeMinutes = 70,
            tags = greece,
            ingredients = listOf(
                Ingredient("Berenjenas", 3.0, Piece),
                Ingredient("Carne picada", 500.0, Gram),
                Ingredient("Bechamel", 500.0, Milliliter),
            ),
            steps = listOf("Asa las berenjenas.", "Prepara el ragú de carne.", "Monta las capas, cubre con bechamel y hornea."),
        ),
        Recipe(
            id = "greek-salad",
            title = "Ensalada griega",
            prepTimeMinutes = 10,
            tags = greece,
            ingredients = listOf(
                Ingredient("Tomates", 4.0, Piece),
                Ingredient("Queso feta", 150.0, Gram),
                Ingredient("Aceitunas kalamata", 80.0, Gram),
                Ingredient("Orégano", unit = ToTaste),
            ),
            steps = listOf("Corta las verduras.", "Añade el feta y las aceitunas.", "Aliña con aceite y orégano."),
        ),
        Recipe(
            id = "tzatziki",
            title = "Tzatziki con pita",
            prepTimeMinutes = 15,
            tags = greece,
            ingredients = listOf(
                Ingredient("Yogur griego", 400.0, Gram),
                Ingredient("Pepino", 1.0, Piece),
                Ingredient("Ajo", 1.0, Piece),
                Ingredient("Pan de pita", 4.0, Piece),
            ),
            steps = listOf("Ralla y escurre el pepino.", "Mezcla con el yogur y el ajo picado.", "Sirve frío con pan de pita."),
        ),
        Recipe(
            id = "souvlaki",
            title = "Souvlaki de pollo",
            prepTimeMinutes = 40,
            tags = greece,
            ingredients = listOf(
                Ingredient("Pechuga de pollo", 600.0, Gram),
                Ingredient("Limón", 1.0, Piece),
                Ingredient("Orégano", unit = ToTaste),
            ),
            steps = listOf("Marina el pollo con limón y orégano.", "Ensarta en brochetas.", "Cocina a la plancha."),
        ),
        Recipe(
            id = "risotto",
            title = "Risotto de setas",
            prepTimeMinutes = 40,
            tags = italy,
            ingredients = listOf(
                Ingredient("Arroz arborio", 320.0, Gram),
                Ingredient("Setas variadas", 300.0, Gram),
                Ingredient("Caldo", 1.0, Liter),
                Ingredient("Parmesano", 60.0, Gram),
            ),
            steps = listOf("Saltea las setas.", "Tuesta el arroz y añade el caldo poco a poco.", "Termina con parmesano."),
        ),
        Recipe(
            id = "pomodoro",
            title = "Pasta al pomodoro",
            prepTimeMinutes = 25,
            tags = italy,
            ingredients = listOf(
                Ingredient("Espaguetis", 400.0, Gram),
                Ingredient("Tomate triturado", 400.0, Gram),
                Ingredient("Albahaca", unit = ToTaste),
            ),
            steps = listOf("Cuece la pasta al dente.", "Haz la salsa con ajo y tomate.", "Mezcla y termina con albahaca."),
        ),
        Recipe(
            id = "pizza",
            title = "Pizza margarita",
            prepTimeMinutes = 45,
            tags = italy,
            ingredients = listOf(
                Ingredient("Masa de pizza", 1.0, Piece),
                Ingredient("Mozzarella", 250.0, Gram),
                Ingredient("Tomate triturado", 200.0, Gram),
            ),
            steps = listOf("Estira la masa.", "Cubre con tomate y mozzarella.", "Hornea a máxima temperatura."),
        ),
        Recipe(
            id = "tiramisu",
            title = "Tiramisú",
            prepTimeMinutes = 30,
            tags = italy,
            ingredients = listOf(
                Ingredient("Mascarpone", 500.0, Gram),
                Ingredient("Bizcochos de soletilla", 24.0, Piece),
                Ingredient("Café", 300.0, Milliliter),
                Ingredient("Cacao en polvo", unit = ToTaste),
            ),
            steps = listOf(
                "Moja los bizcochos en café.",
                "Alterna capas con crema de mascarpone.",
                "Reposa en nevera 6 horas y espolvorea cacao.",
            ),
        ),
    )
}
