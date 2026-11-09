package com.tavola.domain.model

/** The fixed categories a shopping item can belong to, in the order they are shown. [Other] is the default. */
enum class ShoppingCategory { FruitAndVegetables, MeatAndFish, DairyAndEggs, Pantry, Bakery, Frozen, Drinks, Household, Other }

/**
 * An item of the group's shopping list. [quantity] is free text ("2 kg", "a dozen"): quantities are never summed or converted.
 * Items are ordered by [createdAt] (milliseconds since the epoch). [addedBy] is the id of whoever added it (stored, not shown yet).
 */
data class ShoppingItem(
    val id: String,
    val name: String,
    val category: ShoppingCategory = ShoppingCategory.Other,
    val quantity: String? = null,
    val checked: Boolean = false,
    val createdAt: Long = 0,
    val addedBy: String? = null,
)
