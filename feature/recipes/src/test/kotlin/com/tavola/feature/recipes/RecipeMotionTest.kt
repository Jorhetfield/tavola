package com.tavola.feature.recipes

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RecipeMotionTest {
    @Test
    fun `the first item enters at once and each next one a little later`() {
        assertEquals(0L, entranceDelayMillis(0))
        assertTrue(entranceDelayMillis(1) > entranceDelayMillis(0))
        assertTrue(entranceDelayMillis(2) > entranceDelayMillis(1))
    }

    @Test
    fun `the delay is capped so a long list does not drag on`() {
        assertEquals(entranceDelayMillis(8), entranceDelayMillis(100))
    }

    @Test
    fun `a negative index counts as the first item`() {
        assertEquals(0L, entranceDelayMillis(-3))
    }
}
