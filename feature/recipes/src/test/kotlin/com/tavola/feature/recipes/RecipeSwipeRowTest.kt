package com.tavola.feature.recipes

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RecipeSwipeRowTest {
    @Test
    fun `a row is not deleted by a short or a medium drag`() {
        assertFalse(isPastDeleteThreshold(offsetPx = 0f, widthPx = 1000))
        assertFalse(isPastDeleteThreshold(offsetPx = -200f, widthPx = 1000))
        assertFalse(isPastDeleteThreshold(offsetPx = -500f, widthPx = 1000))
        assertFalse(isPastDeleteThreshold(offsetPx = -790f, widthPx = 1000))
    }

    @Test
    fun `a row is deleted only when it is almost all the way across`() {
        assertTrue(isPastDeleteThreshold(offsetPx = -800f, widthPx = 1000))
        assertTrue(isPastDeleteThreshold(offsetPx = -1000f, widthPx = 1000))
    }

    @Test
    fun `a row that has not been measured yet is never deleted`() {
        assertFalse(isPastDeleteThreshold(offsetPx = -50f, widthPx = 0))
    }
}
