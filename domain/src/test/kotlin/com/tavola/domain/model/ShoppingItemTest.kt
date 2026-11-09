package com.tavola.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ShoppingItemTest {
    @Test
    fun `the name and the quantity are trimmed`() {
        val item = ShoppingItem(id = "", name = "  Leche entera ", quantity = " 2 l ").normalized()

        assertEquals("Leche entera", item.name)
        assertEquals("2 l", item.quantity)
    }

    @Test
    fun `a blank quantity means no quantity`() {
        assertNull(ShoppingItem(id = "", name = "Pan", quantity = "   ").normalized().quantity)
    }

    @Test
    fun `a name or a quantity that is too long is cut to the limit instead of being refused`() {
        val item = ShoppingItem(
            id = "",
            name = "a".repeat(ShoppingItem.MAX_NAME_LENGTH + 30),
            quantity = "b".repeat(ShoppingItem.MAX_QUANTITY_LENGTH + 30),
        ).normalized()

        assertEquals(ShoppingItem.MAX_NAME_LENGTH, item.name.length)
        assertEquals(ShoppingItem.MAX_QUANTITY_LENGTH, item.quantity?.length)
    }

    @Test
    fun `an item without a name cannot be saved`() {
        assertThrows(IllegalArgumentException::class.java) { ShoppingItem(id = "", name = "   ").normalized() }
    }

    @Test
    fun `the other fields are left as they were`() {
        val item = ShoppingItem("id", "Pan", ShoppingCategory.Bakery, "1", checked = true, createdAt = 7, addedBy = "marta")

        assertEquals(item, item.normalized())
    }
}
