package com.tavola.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ShoppingNamesTest {
    @Test
    fun `case, accents and spacing do not matter`() {
        assertTrue(ShoppingNames.same("Limón", "  limon "))
        assertTrue(ShoppingNames.same("aceite  de oliva", "Aceite de Oliva"))
    }

    @Test
    fun `a plural s or es on the whole name is the same thing`() {
        assertTrue(ShoppingNames.same("Tomate", "tomates"))
        assertTrue(ShoppingNames.same("Pimientos", "pimiento"))
        assertTrue(ShoppingNames.same("Limón", "limones"))
    }

    @Test
    fun `different things are not the same`() {
        assertFalse(ShoppingNames.same("Leche", "Leche de almendras"))
        assertFalse(ShoppingNames.same("Pan", "Pasta"))
    }

    @Test
    fun `digits count, so names that differ by a number are different things`() {
        assertFalse(ShoppingNames.same("Pack 6", "Pack 12"))
        assertTrue(ShoppingNames.same("Pack 6", "pack  6"))
        assertEquals(listOf("vitamina", "b12"), ShoppingNames.words("Vitamina B12"))
    }

    @Test
    fun `names in other scripts are compared by their letters`() {
        assertTrue(ShoppingNames.same("Φέτα", "φετα"))
    }

    @Test
    fun `an empty or symbol-only name is the same as nothing`() {
        assertFalse(ShoppingNames.same("", ""))
        assertFalse(ShoppingNames.same("  ", "!!!"))
    }

    @Test
    fun `the words of a text come without accents, case or punctuation`() {
        assertEquals(listOf("pan", "de", "payes"), ShoppingNames.words("  Pan, de PAYÉS! "))
    }
}
