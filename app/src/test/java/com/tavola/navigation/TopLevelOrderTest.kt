package com.tavola.navigation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TopLevelOrderTest {
    @Test
    fun `a route and its content key identify the same tab`() {
        assertEquals(CALENDAR_KEY, CalendarRoute.topLevelKey())
        assertEquals(CALENDAR_KEY, CALENDAR_KEY.topLevelKey())
        assertEquals(RECIPES_KEY, RecipesRoute.topLevelKey())
        assertEquals(RECIPES_KEY, RECIPES_KEY.topLevelKey())
    }

    @Test
    fun `the tabs are ordered as they sit in the bar`() {
        assertEquals(0, CalendarRoute.topLevelIndex())
        assertEquals(1, RecipesRoute.topLevelIndex())
        assertTrue(CalendarRoute.topLevelIndex()!! < RecipesRoute.topLevelIndex()!!)
    }

    @Test
    fun `a detail, an unknown key and null are not tabs`() {
        assertNull(RecipeDetailRoute("paella").topLevelKey())
        assertNull(RecipeDetailRoute("paella").topLevelIndex())
        assertNull("shopping".topLevelIndex())
        assertNull(null.topLevelKey())
    }
}
