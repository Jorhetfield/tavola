package com.tavola.domain.model

/**
 * Something the group has put on its shopping list before, kept so it can be added again with one tap. [id] is the key of the
 * product (see [ShoppingHistory.keyFor]), so the same product is always the same entry however it is written.
 *
 * [useCount] is how many times it was added and [lastUsedAt] when, [lastBoughtAt] is when it was last bought (`null` once it is added
 * again), and all of them are milliseconds since the epoch from the server's clock. A [hidden] entry was forgotten: it stays out of
 * the suggestions until it is used again.
 */
data class ShoppingHistoryEntry(
    val id: String,
    val name: String,
    val category: ShoppingCategory = ShoppingCategory.Other,
    val useCount: Int = 0,
    val lastUsedAt: Long = 0,
    val lastBoughtAt: Long? = null,
    val hidden: Boolean = false,
)

object ShoppingHistory {
    /** How long a bought item stays in "Bought recently", and how many of them are shown. */
    const val RECENTLY_BOUGHT_WINDOW_MILLIS = 24 * 60 * 60 * 1000L
    const val MAX_RECENTLY_BOUGHT = 12

    /** The security rules cap the id of an entry at this many characters. */
    const val MAX_KEY_LENGTH = 100

    /**
     * The id of the history entry of a product called [name]: the id of its catalog product when it is one ("Tomates" and "tomato"
     * are both `tomato`), or else its words, so "Pollo asado" and "pollo  asado" are the same entry. A name with no letters at all
     * still gets a key, from the text itself.
     */
    fun keyFor(name: String): String {
        ShoppingCatalog.find(name)?.let { product -> return product.id }
        val key = ShoppingNames.words(name).joinToString("-").take(MAX_KEY_LENGTH).trim('-')
        return key.ifEmpty { "other-${name.trim().lowercase().hashCode().toUInt()}" }
    }

    /**
     * What was bought in the last [RECENTLY_BOUGHT_WINDOW_MILLIS] before [now] and is not on the list ([items]) any more, the last
     * bought first and at most [MAX_RECENTLY_BOUGHT]. Forgotten entries are left out.
     */
    fun recentlyBought(entries: List<ShoppingHistoryEntry>, items: List<ShoppingItem>, now: Long): List<ShoppingHistoryEntry> = entries
        .filter { entry -> !entry.hidden && entry.lastBoughtAt != null && now - entry.lastBoughtAt <= RECENTLY_BOUGHT_WINDOW_MILLIS }
        .filter { entry -> items.none { item -> ShoppingNames.same(item.name, entry.name) } }
        .sortedByDescending { entry -> entry.lastBoughtAt }
        .take(MAX_RECENTLY_BOUGHT)
}
