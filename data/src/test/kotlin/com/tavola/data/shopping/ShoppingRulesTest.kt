package com.tavola.data.shopping

import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingItem
import java.io.File
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * `firestore.rules` is published by hand and not run against an emulator, so these tests only keep what the app and the rules
 * both know in step: a category or a limit changed on one side and not on the other would make the server refuse every write.
 */
class ShoppingRulesTest {
    private val rules: String = File("../firestore.rules").readText()

    @Test
    fun `the rules accept every category of the app`() {
        ShoppingCategory.entries.forEach { category ->
            assertTrue(rules.contains("'${category.name}'"), "firestore.rules does not list the category ${category.name}")
        }
    }

    @Test
    fun `the rules cap the name and the quantity at the limits the app trims to`() {
        assertTrue(rules.contains("item.name.size() <= ${ShoppingItem.MAX_NAME_LENGTH}"), "name limit differs from the rules")
        assertTrue(rules.contains("item.quantity.size() <= ${ShoppingItem.MAX_QUANTITY_LENGTH}"), "quantity limit differs from the rules")
    }

    @Test
    fun `the rules know every field the document writes`() {
        val document = ShoppingItem(id = "x", name = "Pan").toDocument(addedBy = "someone")

        document.keys.forEach { field -> assertTrue(rules.contains("'$field'"), "firestore.rules does not list the field $field") }
    }
}
