package com.tavola.domain.model

/** The fixed categories a shopping item can belong to, in the order they are shown. [Other] is the default. */
enum class ShoppingCategory { FruitAndVegetables, MeatAndFish, DairyAndEggs, Pantry, Bakery, Frozen, Drinks, Household, Other }

/**
 * An item of the group's shopping list. [quantity] is free text ("2 kg", "a dozen"): quantities are never summed or converted.
 * Items are ordered by [createdAt] (milliseconds since the epoch, from the server's clock so every phone agrees on the order).
 * [addedBy] is the id of whoever added it (stored, not shown yet).
 */
data class ShoppingItem(
    val id: String,
    val name: String,
    val category: ShoppingCategory = ShoppingCategory.Other,
    val quantity: String? = null,
    val checked: Boolean = false,
    val createdAt: Long = 0,
    val addedBy: String? = null,
) {
    /**
     * The item as it is saved: the name and the quantity trimmed and cut to the limits the security rules enforce (a longer text
     * would be refused by the server without anyone noticing), and a blank quantity turned into none. An item with no name cannot
     * be saved, so the screen must not offer it.
     */
    fun normalized(): ShoppingItem {
        val cleanName = name.trim().take(MAX_NAME_LENGTH).trim()
        require(cleanName.isNotEmpty()) { "A shopping item needs a name" }
        return copy(name = cleanName, quantity = quantity?.trim()?.take(MAX_QUANTITY_LENGTH)?.trim()?.ifEmpty { null })
    }

    companion object {
        const val MAX_NAME_LENGTH = 120
        const val MAX_QUANTITY_LENGTH = 60
    }
}
