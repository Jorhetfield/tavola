package com.tavola.feature.recipes

import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IngredientFormattingTest {
    private val spanish = Locale.forLanguageTag("es-ES")
    private val english = Locale.forLanguageTag("en-GB")

    @Test
    fun `whole numbers have no decimals`() {
        assertEquals("400", formatQuantity(400.0, spanish))
        assertEquals("1", formatQuantity(1.0, english))
    }

    @Test
    fun `the decimal separator follows the locale`() {
        assertEquals("0,5", formatQuantity(0.5, spanish))
        assertEquals("0.5", formatQuantity(0.5, english))
    }

    @Test
    fun `at most two decimals are kept and there is no thousands grouping`() {
        assertEquals("1,25", formatQuantity(1.25, spanish))
        assertEquals("0.33", formatQuantity(0.3333, english))
        assertEquals("1200", formatQuantity(1200.0, english))
    }
}
