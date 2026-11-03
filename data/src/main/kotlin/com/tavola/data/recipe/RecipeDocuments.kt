package com.tavola.data.recipe

import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.IngredientUnit
import com.tavola.domain.model.Recipe

// How the recipes of a group are stored in Firestore: groups/{groupId}/recipes/{recipeId}, with the ingredients and the steps
// inside the document. The security rules in `firestore.rules` depend on these names and shapes.
//
//   { title, prepTimeMinutes?, tags[], ingredients[{ name, quantity?, unit }], steps[], createdBy? }

internal object RecipeFields {
    const val TITLE = "title"
    const val PREP_TIME_MINUTES = "prepTimeMinutes"
    const val TAGS = "tags"
    const val INGREDIENTS = "ingredients"
    const val STEPS = "steps"

    /** Who made the recipe. It is written when the recipe is created and left alone by later edits. */
    const val CREATED_BY = "createdBy"
    const val INGREDIENT_NAME = "name"
    const val INGREDIENT_QUANTITY = "quantity"
    const val INGREDIENT_UNIT = "unit"
}

internal const val RECIPES_COLLECTION = "recipes"

/**
 * The document of this recipe. [createdBy] is only included when it is known, so a write that merges it into an existing
 * document leaves whoever made the recipe as they were. The unit is stored by name so reordering the enum never corrupts data.
 */
internal fun Recipe.toDocument(createdBy: String?): Map<String, Any?> = buildMap {
    put(RecipeFields.TITLE, title)
    put(RecipeFields.PREP_TIME_MINUTES, prepTimeMinutes)
    put(RecipeFields.TAGS, tags)
    put(
        RecipeFields.INGREDIENTS,
        ingredients.map { ingredient ->
            mapOf(
                RecipeFields.INGREDIENT_NAME to ingredient.name,
                RecipeFields.INGREDIENT_QUANTITY to ingredient.quantity,
                RecipeFields.INGREDIENT_UNIT to ingredient.unit.name,
            )
        },
    )
    put(RecipeFields.STEPS, steps)
    if (createdBy != null) put(RecipeFields.CREATED_BY, createdBy)
}

/**
 * The recipe stored in a document, or `null` if it has no title. Anything else that is missing or malformed is read as empty so
 * the recipe still opens, and an unknown unit (from a newer app version, say) falls back to "to taste".
 */
internal fun Map<String, Any?>.toRecipe(id: String): Recipe? {
    val title = this[RecipeFields.TITLE] as? String ?: return null
    return Recipe(
        id = id,
        title = title,
        prepTimeMinutes = (this[RecipeFields.PREP_TIME_MINUTES] as? Number)?.toInt(),
        tags = (this[RecipeFields.TAGS] as? List<*>)?.filterIsInstance<String>().orEmpty(),
        ingredients = (this[RecipeFields.INGREDIENTS] as? List<*>).orEmpty().mapNotNull { stored ->
            (stored as? Map<*, *>)?.toIngredient()
        },
        steps = (this[RecipeFields.STEPS] as? List<*>)?.filterIsInstance<String>().orEmpty(),
        createdBy = this[RecipeFields.CREATED_BY] as? String,
    )
}

private fun Map<*, *>.toIngredient(): Ingredient? {
    val name = this[RecipeFields.INGREDIENT_NAME] as? String ?: return null
    val unitName = this[RecipeFields.INGREDIENT_UNIT] as? String
    return Ingredient(
        name = name,
        quantity = (this[RecipeFields.INGREDIENT_QUANTITY] as? Number)?.toDouble(),
        unit = IngredientUnit.entries.firstOrNull { unit -> unit.name == unitName } ?: IngredientUnit.ToTaste,
    )
}
