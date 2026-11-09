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
        assertEquals(SHOPPING_KEY, ShoppingRoute.topLevelKey())
        assertEquals(SHOPPING_KEY, SHOPPING_KEY.topLevelKey())
    }

    @Test
    fun `the tabs are ordered as they sit in the bar`() {
        assertEquals(0, CalendarRoute.topLevelIndex())
        assertEquals(1, RecipesRoute.topLevelIndex())
        assertEquals(2, ShoppingRoute.topLevelIndex())
        assertTrue(CalendarRoute.topLevelIndex()!! < RecipesRoute.topLevelIndex()!!)
        assertTrue(RecipesRoute.topLevelIndex()!! < ShoppingRoute.topLevelIndex()!!)
    }

    @Test
    fun `a detail, an unknown key and null are not tabs`() {
        assertNull(RecipeDetailRoute("paella").topLevelKey())
        assertNull(RecipeDetailRoute("paella").topLevelIndex())
        assertNull("settings".topLevelIndex())
        assertNull(null.topLevelKey())
    }
}
