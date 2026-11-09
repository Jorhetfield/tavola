package com.tavola.data.shopping

import com.google.firebase.Timestamp
import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingHistory
import com.tavola.domain.model.ShoppingHistoryEntry
import com.tavola.domain.model.ShoppingItem
import java.io.File
import java.util.Date
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ShoppingHistoryDocumentsTest {
    private val rules: String = File("../firestore.rules").readText()
    private val milk = ShoppingItem(id = "milk", name = "Leche", category = ShoppingCategory.DairyAndEggs, quantity = "2 l")

    @Test
    fun `a use writes the latest name and category, not bought and not hidden, and leaves the item's quantity out`() {
        val document = milk.toUseDocument()

        assertEquals("Leche", document["name"])
        assertEquals("DairyAndEggs", document["category"])
        assertNull(document["lastBoughtAt"])
        assertEquals(false, document["hidden"])
        assertFalse(document.containsKey("quantity"))
    }

    @Test
    fun `buying writes only what changes, so the uses are left alone`() {
        val document = milk.toBoughtDocument()

        assertEquals(setOf("name", "category", "lastUsedAt", "lastBoughtAt", "hidden"), document.keys)
        assertEquals(false, document["hidden"])
    }

    @Test
    fun `forgetting hides the entry and carries the name so the write is valid on its own`() {
        val document = ShoppingHistoryEntry(id = "milk", name = "Leche", category = ShoppingCategory.DairyAndEggs).toHiddenDocument()

        assertEquals(mapOf<String, Any?>("name" to "Leche", "category" to "DairyAndEggs", "hidden" to true), document)
    }

    @Test
    fun `an entry is read from its document`() {
        val document = mapOf<String, Any?>(
            "name" to "Leche",
            "category" to "DairyAndEggs",
            "useCount" to 3L,
            "lastUsedAt" to Timestamp(Date(2_000)),
            "lastBoughtAt" to Timestamp(Date(3_000)),
            "hidden" to true,
        )

        assertEquals(
            ShoppingHistoryEntry(
                "milk",
                "Leche",
                ShoppingCategory.DairyAndEggs,
                useCount = 3,
                lastUsedAt = 2_000,
                lastBoughtAt = 3_000,
                hidden = true,
            ),
            document.toHistoryEntry("milk"),
        )
    }

    @Test
    fun `missing fields are read as never used, never bought and not hidden, and an unknown category as other`() {
        val document = mapOf<String, Any?>("name" to "Algo raro", "category" to "FromTheFuture", "lastBoughtAt" to null)

        assertEquals(ShoppingHistoryEntry(id = "x", name = "Algo raro"), document.toHistoryEntry("x"))
    }

    @Test
    fun `a document without a name is not an entry`() {
        assertNull(mapOf<String, Any?>("category" to "Pantry").toHistoryEntry("x"))
    }

    @Test
    fun `the rules know every field the documents write`() {
        val fields = milk.toUseDocument().keys + milk.toBoughtDocument().keys +
            ShoppingHistoryEntry(id = "milk", name = "Leche").toHiddenDocument().keys

        fields.forEach { field -> assertTrue(rules.contains("'$field'"), "firestore.rules does not list the field $field") }
    }

    @Test
    fun `the rules cap the key of an entry and its name at the limits the app uses`() {
        assertTrue(rules.contains("entryId.size() <= ${ShoppingHistory.MAX_KEY_BYTES}"), "key limit differs from the rules")
        assertTrue(rules.contains("entry.name.size() <= ${ShoppingItem.MAX_NAME_LENGTH}"), "name limit differs from the rules")
    }
}
