package com.tavola.domain.model

/** Unidad de un ingrediente. [ToTaste] ("al gusto") y [Piece] no llevan cantidad en gramos o litros. */
enum class IngredientUnit { Gram, Kilogram, Milliliter, Liter, Teaspoon, Tablespoon, Piece, Pinch, ToTaste }

/** Ingrediente estructurado (así la lista de la compra podrá sumar y agrupar). [quantity] es `null` con [IngredientUnit.ToTaste]. */
data class Ingredient(val name: String, val quantity: Double? = null, val unit: IngredientUnit = IngredientUnit.ToTaste)

/** A recipe of the group's book. [createdBy] is the id of whoever made it (stored, not shown yet); `null` when it is not known. */
data class Recipe(
    val id: String,
    val title: String,
    val prepTimeMinutes: Int? = null,
    val tags: List<String> = emptyList(),
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<String> = emptyList(),
    val createdBy: String? = null,
) {
    fun toRef(): RecipeRef = RecipeRef(recipeId = id, title = title, prepTimeMinutes = prepTimeMinutes, tags = tags)
}
