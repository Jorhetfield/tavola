package com.tavola.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ShoppingHistoryTest {
    private val hour = 60 * 60 * 1000L
    private val now = 100 * hour

    private fun bought(name: String, hoursAgo: Long, hidden: Boolean = false) = ShoppingHistoryEntry(
        id = ShoppingHistory.keyFor(name),
        name = name,
        lastBoughtAt = now - hoursAgo * hour,
        hidden = hidden,
    )

    @Test
    fun `the key of a catalog product is its id, whatever the language or the plural`() {
        assertEquals("tomato", ShoppingHistory.keyFor("Tomates"))
        assertEquals("tomato", ShoppingHistory.keyFor("tomato"))
        assertEquals("olive-oil", ShoppingHistory.keyFor("Aceite de oliva"))
    }

    @Test
    fun `the key of another product is its words, so spacing and case do not make a new entry`() {
        assertEquals("pollo-asado", ShoppingHistory.keyFor("Pollo asado"))
        assertEquals("pollo-asado", ShoppingHistory.keyFor("  pollo   ASADO!"))
        assertNotEquals(ShoppingHistory.keyFor("Pollo asado"), ShoppingHistory.keyFor("Pollo frito"))
    }

    @Test
    fun `a name with no letters still has a key that is the same each time`() {
        val key = ShoppingHistory.keyFor("1234")

        assertTrue(key.isNotEmpty())
        assertEquals(key, ShoppingHistory.keyFor("1234"))
        assertNotEquals(key, ShoppingHistory.keyFor("5678"))
    }

    @Test
    fun `names that differ only by a number or a symbol are different entries`() {
        assertNotEquals(ShoppingHistory.keyFor("Vitamina B6"), ShoppingHistory.keyFor("Vitamina B12"))
        assertNotEquals(ShoppingHistory.keyFor("Coca-Cola 0%"), ShoppingHistory.keyFor("Coca-Cola"))
        assertEquals("vitamina-b12", ShoppingHistory.keyFor("Vitamina  B12"))
    }

    @Test
    fun `a name in another script has a key made of its own words`() {
        assertEquals("φετα", ShoppingHistory.keyFor("Φέτα"))
        assertNotEquals("φετα", ShoppingHistory.keyFor("ελιές"))
    }

    @Test
    fun `a name with only symbols still has a key`() {
        assertTrue(ShoppingHistory.keyFor("🍅🍅").startsWith("other-"))
    }

    @Test
    fun `a key is never longer than the rules allow, counted in bytes`() {
        listOf("pollo ".repeat(100), "φέτα ".repeat(100)).forEach { name ->
            val key = ShoppingHistory.keyFor(name)

            assertTrue(key.toByteArray(Charsets.UTF_8).size <= ShoppingHistory.MAX_KEY_BYTES, "$key is too long")
            assertTrue(key.isNotEmpty() && !key.endsWith("-"))
        }
    }

    @Test
    fun `only the entry of a catalog product comes from the catalog`() {
        assertTrue(ShoppingHistoryEntry(id = "tomato", name = "tomate").isFromCatalog)
        assertEquals(false, ShoppingHistoryEntry(id = "pollo-asado", name = "Pollo asado").isFromCatalog)
        assertEquals(false, ShoppingHistoryEntry(id = "pollo", name = "Tomates").isFromCatalog, "the key decides, not the name")
    }

    @Test
    fun `the history is offered most recently used first, then most used, then by name`() {
        val entries = listOf(
            ShoppingHistoryEntry("a", "Zumo", lastUsedAt = 1, useCount = 9),
            ShoppingHistoryEntry("b", "Pan", lastUsedAt = 5, useCount = 1),
            ShoppingHistoryEntry("c", "Arroz", lastUsedAt = 5, useCount = 1),
            ShoppingHistoryEntry("d", "Sal", lastUsedAt = 5, useCount = 3),
        )

        assertEquals(listOf("Sal", "Arroz", "Pan", "Zumo"), entries.sortedWith(ShoppingHistory.mostRecentFirst).map { entry -> entry.name })
    }

    @Test
    fun `recently bought is the last day, the last bought first`() {
        val entries = listOf(bought("Leche", hoursAgo = 20), bought("Pan", hoursAgo = 2), bought("Sal", hoursAgo = 30))

        val recent = ShoppingHistory.recentlyBought(entries, items = emptyList(), now = now)

        assertEquals(listOf("Pan", "Leche"), recent.map { entry -> entry.name })
    }

    @Test
    fun `what is on the list again, forgotten or never bought is not recently bought`() {
        val entries = listOf(
            bought("Leche", hoursAgo = 1),
            bought("Pan", hoursAgo = 1),
            bought("Sal", hoursAgo = 1, hidden = true),
            ShoppingHistoryEntry(id = "arroz", name = "Arroz"),
        )
        val onTheList = listOf(ShoppingItem(id = "1", name = "pan"))

        val recent = ShoppingHistory.recentlyBought(entries, onTheList, now)

        assertEquals(listOf("Leche"), recent.map { entry -> entry.name })
    }

    @Test
    fun `an item on the list hides its entry even when the name is written differently`() {
        val entries = listOf(bought("Tomates", hoursAgo = 1))

        assertTrue(ShoppingHistory.recentlyBought(entries, listOf(ShoppingItem("1", "tomate")), now).isEmpty())
    }

    @Test
    fun `recently bought shows at most twelve`() {
        val entries = (1..20).map { number -> bought("Producto ${'a' + number}", hoursAgo = number.toLong()) }

        val recent = ShoppingHistory.recentlyBought(entries, emptyList(), now)

        assertEquals(ShoppingHistory.MAX_RECENTLY_BOUGHT, recent.size)
        assertEquals(entries.first().name, recent.first().name)
    }
}
