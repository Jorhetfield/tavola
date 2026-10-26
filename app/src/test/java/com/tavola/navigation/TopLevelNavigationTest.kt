package com.tavola.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TopLevelNavigationTest {
    private fun stack(vararg keys: NavKey): MutableList<NavKey> = mutableListOf(CalendarRoute, *keys)

    @Test
    fun `going to recipes from the calendar pushes it on top`() {
        val backStack = stack()

        backStack.navigateToTopLevel(RECIPES_KEY)

        assertEquals(listOf(CalendarRoute, RecipesRoute), backStack)
    }

    @Test
    fun `going to the calendar from recipes leaves only the calendar`() {
        val backStack = stack(RecipesRoute)

        backStack.navigateToTopLevel(CALENDAR_KEY)

        assertEquals(listOf<NavKey>(CalendarRoute), backStack)
    }

    @Test
    fun `going to the calendar from a recipe detail drops everything above it`() {
        val backStack = stack(RecipesRoute, RecipeDetailRoute("paella"))

        backStack.navigateToTopLevel(CALENDAR_KEY)

        assertEquals(listOf<NavKey>(CalendarRoute), backStack)
    }

    @Test
    fun `tapping recipes again drops the detail opened above it`() {
        val backStack = stack(RecipesRoute, RecipeDetailRoute("paella"))

        backStack.navigateToTopLevel(RECIPES_KEY)

        assertEquals(listOf(CalendarRoute, RecipesRoute), backStack)
    }

    @Test
    fun `tapping the current tab twice changes nothing`() {
        val backStack = stack(RecipesRoute)

        backStack.navigateToTopLevel(RECIPES_KEY)
        backStack.navigateToTopLevel(RECIPES_KEY)

        assertEquals(listOf(CalendarRoute, RecipesRoute), backStack)
    }

    @Test
    fun `an unknown key changes nothing`() {
        val backStack = stack(RecipesRoute)

        backStack.navigateToTopLevel("shopping")

        assertEquals(listOf(CalendarRoute, RecipesRoute), backStack)
    }
}
