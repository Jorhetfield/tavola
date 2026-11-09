package com.tavola.data.shopping

import com.google.firebase.Timestamp
import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingItem
import java.util.Date
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ShoppingDocumentsTest {
    private val tomatoes = ShoppingItem(
        id = "tomatoes",
        name = "Tomates",
        category = ShoppingCategory.FruitAndVegetables,
        quantity = "1 kg",
        checked = true,
        createdAt = 1_700_000_000_000,
        addedBy = "user-marta",
    )

    @Test
    fun `an item is read back as it was written, with who added it`() {
        val document = tomatoes.toDocument(addedBy = "user-marta")

        assertEquals(tomatoes, document.toShoppingItem("tomatoes"))
    }

    @Test
    fun `the time is stored as a timestamp, so the rules and the server clock can handle it`() {
        val document = tomatoes.toDocument(addedBy = null)

        assertEquals(Timestamp(Date(1_700_000_000_000)), document["createdAt"])
    }

    @Test
    fun `without an author the document leaves the field out, so a merge keeps who added the item`() {
        val document = tomatoes.toDocument(addedBy = null)

        assertFalse(document.containsKey("addedBy"))
        assertNull(document.toShoppingItem("tomatoes")?.addedBy)
    }

    @Test
    fun `an item without a quantity writes it as null, so saving it clears the quantity it had`() {
        val document = tomatoes.copy(quantity = null).toDocument(addedBy = null)

        assertEquals(true, document.containsKey("quantity"))
        assertNull(document["quantity"])
    }

    @Test
    fun `the document has the shape the rules ask for`() {
        val document = tomatoes.toDocument(addedBy = "user-marta")

        assertEquals(setOf("name", "category", "quantity", "checked", "createdAt", "addedBy"), document.keys)
        assertEquals("FruitAndVegetables", document["category"])
    }

    @Test
    fun `an unknown category falls back to other and missing fields are read as defaults`() {
        val document = mapOf<String, Any?>("name" to "Algo raro", "category" to "FromTheFuture")

        assertEquals(ShoppingItem(id = "x", name = "Algo raro"), document.toShoppingItem("x"))
    }

    @Test
    fun `a document without a name is not an item`() {
        assertNull(mapOf<String, Any?>("category" to "Pantry").toShoppingItem("x"))
    }
}
