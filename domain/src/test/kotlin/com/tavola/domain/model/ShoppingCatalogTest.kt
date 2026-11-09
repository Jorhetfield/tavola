package com.tavola.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ShoppingCatalogTest {
    @Test
    fun `the catalog is big enough and every category has products`() {
        assertTrue(ShoppingCatalog.products.size in 120..250, "the catalog has ${ShoppingCatalog.products.size} products")
        ShoppingCategory.entries.forEach { category ->
            assertTrue(ShoppingCatalog.inCategory(category).isNotEmpty(), "no products in $category")
        }
    }

    @Test
    fun `ids are the english name as a key and are never repeated`() {
        val ids = ShoppingCatalog.products.map { product -> product.id }

        assertEquals(ids.size, ids.toSet().size)
        ShoppingCatalog.products.forEach { product ->
            assertEquals(ShoppingNames.key(product.english).replace(' ', '-'), product.id)
            assertTrue(product.id.length <= ShoppingHistory.MAX_KEY_LENGTH)
            assertTrue(product.id.all { character -> character in 'a'..'z' || character == '-' }, "bad id ${product.id}")
        }
    }

    @Test
    fun `no name or alias points to two products`() {
        val owners = mutableMapOf<String, String>()
        ShoppingCatalog.products.forEach { product ->
            product.allNames().map { name -> ShoppingNames.key(name) }.distinct().forEach { key ->
                val previous = owners.put(key, product.id)
                assertTrue(previous == null, "'$key' names both $previous and ${product.id}")
            }
        }
    }

    @Test
    fun `every product has an emoji and names in both languages`() {
        ShoppingCatalog.products.forEach { product ->
            assertTrue(product.emoji.isNotBlank(), "${product.id} has no emoji")
            assertTrue(ShoppingNames.key(product.spanish).isNotEmpty() && ShoppingNames.key(product.english).isNotEmpty(), product.id)
        }
    }

    @Test
    fun `a product is found by its name in either language, an alias, case, accents and a plural`() {
        assertEquals("tomato", ShoppingCatalog.find("Tomates")?.id)
        assertEquals("tomato", ShoppingCatalog.find("tomatoes")?.id)
        assertEquals("potato", ShoppingCatalog.find("papa")?.id)
        assertEquals("zucchini", ShoppingCatalog.find("CALABACÍN")?.id)
        assertEquals("olive-oil", ShoppingCatalog.find("aceite  de oliva")?.id)
        assertEquals("pineapple", ShoppingCatalog.find("piña")?.id)
        assertEquals("egg", ShoppingCatalog.find("huevo")?.id)
    }

    @Test
    fun `a name that is not in the catalog finds nothing, not even a part of it`() {
        assertNull(ShoppingCatalog.find("Tarta de la abuela"))
        assertNull(ShoppingCatalog.find("tomate triturado"))
        assertNull(ShoppingCatalog.find("   "))
        assertNull(ShoppingCatalog.find("🍅"))
    }

    @Test
    fun `byId finds a product by its id`() {
        assertEquals("pasta", ShoppingCatalog.byId("pasta")?.id)
        assertNull(ShoppingCatalog.byId("nope"))
    }

    @Test
    fun `the name follows the language, spanish for es and english for anything else`() {
        val tomato = ShoppingCatalog.byId("tomato")!!

        assertEquals("tomate", tomato.name("es"))
        assertEquals("tomate", tomato.name("es-ES"))
        assertEquals("tomato", tomato.name("en"))
        assertEquals("tomato", tomato.name("fr"))
    }

    @Test
    fun `search puts the names that start with the query before the ones that only have a word that does`() {
        val ids = ShoppingCatalog.search("de").map { product -> product.id }

        assertEquals("detergent", ids.first())
        assertTrue("olive-oil" in ids, "a word of the name starts with the query")
    }

    @Test
    fun `search matches every typed word against the words of a name, in either language`() {
        assertTrue(ShoppingCatalog.search("oliva ace").any { product -> product.id == "olive-oil" })
        assertTrue(ShoppingCatalog.search("olive").any { product -> product.id == "olive-oil" })
        assertTrue(ShoppingCatalog.search("PINA").any { product -> product.id == "pineapple" })
    }

    @Test
    fun `search finds nothing for a blank query or one that matches nothing`() {
        assertTrue(ShoppingCatalog.search("").isEmpty())
        assertTrue(ShoppingCatalog.search("  ").isEmpty())
        assertTrue(ShoppingCatalog.search("zzzz").isEmpty())
    }

    @Test
    fun `every product can be found by its own name`() {
        ShoppingCatalog.products.forEach { product ->
            assertNotNull(ShoppingCatalog.find(product.spanish), "${product.spanish} is not found")
            assertEquals(product.id, ShoppingCatalog.find(product.english)?.id)
            assertTrue(ShoppingCatalog.search(product.spanish).any { found -> found.id == product.id })
        }
    }
}
