package com.tavola.feature.recipes

import com.tavola.core.testing.data.MediterraneanSampleData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RecipeSearchTest {
    private val tiramisu = MediterraneanSampleData.tiramisu
    private val paella = MediterraneanSampleData.paella

    @Test
    fun `una busqueda vacia o en blanco admite todas las recetas`() {
        assertTrue(paella.matches(""))
        assertTrue(paella.matches("   "))
    }

    @Test
    fun `ignora tildes y mayusculas`() {
        assertTrue(tiramisu.matches("tiramisu"))
        assertTrue(tiramisu.matches("TIRAMISÚ"))
        assertEquals("tiramisu", "Tiramisú".normalizedForSearch())
    }

    @Test
    fun `busca por titulo, etiqueta e ingrediente`() {
        assertTrue(paella.matches("valenciana"))
        assertTrue(paella.matches("espanol"))
        assertTrue(paella.matches("arroz"))
    }

    @Test
    fun `no coincide si el texto no aparece en ningun sitio`() {
        assertFalse(paella.matches("sushi"))
    }

    @Test
    fun `una parte de una palabra tambien coincide`() {
        assertTrue(paella.matches("valen"))
    }
}
