package com.tavola.domain.model

/**
 * A common product the person can add with one tap. [id] is the English name written as a key ("olive-oil"); it never changes, so
 * the image of the product can be found by it. [emoji] is the Noto Emoji the product is drawn with. [spanish] and [english] are
 * what the tile says in each language, and [aliases] are other names people know it by ("papa" for the potato).
 */
data class CatalogProduct(
    val id: String,
    val emoji: String,
    val category: ShoppingCategory,
    val spanish: String,
    val english: String,
    val aliases: List<String> = emptyList(),
) {
    /** The name to show for a language tag such as `es` or `en-GB`: Spanish for `es`, English for anything else. */
    fun name(languageTag: String): String = if (languageTag.startsWith("es", ignoreCase = true)) spanish else english

    internal fun allNames(): List<String> = listOf(spanish, english) + aliases
}

/** The built-in catalog of products (see [catalogProducts]), and how to find one by what a person types. */
object ShoppingCatalog {
    val products: List<CatalogProduct> = catalogProducts

    private val keysById: Map<String, List<String>> = products.associate { product ->
        product.id to product.allNames().map { name -> ShoppingNames.key(name) }.distinct()
    }

    private val productByKey: Map<String, CatalogProduct> = buildMap {
        products.forEach { product -> keysById.getValue(product.id).forEach { key -> putIfAbsent(key, product) } }
    }

    private val productById: Map<String, CatalogProduct> = products.associateBy { product -> product.id }

    /** The product named [name] in either language or by an alias, ignoring case, accents and a plural ending; `null` if unknown. */
    fun find(name: String): CatalogProduct? {
        val key = ShoppingNames.key(name)
        if (key.isEmpty()) return null
        return ShoppingNames.spellings(key).firstNotNullOfOrNull { spelling -> productByKey[spelling] }
    }

    fun byId(id: String): CatalogProduct? = productById[id]

    fun inCategory(category: ShoppingCategory): List<CatalogProduct> = products.filter { product -> product.category == category }

    /**
     * The products a person who has typed [query] may mean, best first: the ones with a name that starts with it, then the ones
     * where every typed word starts a word of a name. Both languages and the aliases are searched, whatever the language of the app.
     */
    fun search(query: String): List<CatalogProduct> {
        val queryWords = ShoppingNames.words(query)
        if (queryWords.isEmpty()) return emptyList()
        val queryKey = queryWords.joinToString(" ")
        return products
            .mapNotNull { product ->
                val rank = keysById.getValue(product.id).mapNotNull { key -> rankOf(key, queryKey, queryWords) }.minOrNull()
                rank?.let { found -> product to found }
            }
            .sortedBy { (_, rank) -> rank }
            .map { (product, _) -> product }
    }

    private fun rankOf(key: String, queryKey: String, queryWords: List<String>): Int? {
        if (key.startsWith(queryKey)) return 0
        val nameWords = key.split(' ')
        return if (queryWords.all { queryWord -> nameWords.any { nameWord -> nameWord.startsWith(queryWord) } }) 1 else null
    }
}
