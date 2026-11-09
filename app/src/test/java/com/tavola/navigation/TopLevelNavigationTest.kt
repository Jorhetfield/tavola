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

        backStack.navigateToTopLevel("settings")

        assertEquals(listOf(CalendarRoute, RecipesRoute), backStack)
    }

    @Test
    fun `going to shopping from the calendar pushes it on top`() {
        val backStack = stack()

        backStack.navigateToTopLevel(SHOPPING_KEY)

        assertEquals(listOf(CalendarRoute, ShoppingRoute), backStack)
    }

    @Test
    fun `going to the calendar from shopping leaves only the calendar`() {
        val backStack = stack(RecipesRoute, ShoppingRoute)

        backStack.navigateToTopLevel(CALENDAR_KEY)

        assertEquals(listOf<NavKey>(CalendarRoute), backStack)
    }

    @Test
    fun `going to recipes from shopping drops shopping`() {
        val backStack = stack(RecipesRoute, ShoppingRoute)

        backStack.navigateToTopLevel(RECIPES_KEY)

        assertEquals(listOf(CalendarRoute, RecipesRoute), backStack)
    }

    @Test
    fun `going to recipes from shopping replaces shopping instead of piling up`() {
        val backStack = stack(ShoppingRoute)

        backStack.navigateToTopLevel(RECIPES_KEY)

        assertEquals(listOf(CalendarRoute, RecipesRoute), backStack)
    }

    @Test
    fun `going to shopping from a recipe detail drops the recipes tab and the detail`() {
        val backStack = stack(RecipesRoute, RecipeDetailRoute("paella"))

        backStack.navigateToTopLevel(SHOPPING_KEY)

        assertEquals(listOf(CalendarRoute, ShoppingRoute), backStack)
    }

    @Test
    fun `tapping shopping twice changes nothing`() {
        val backStack = stack(ShoppingRoute)

        backStack.navigateToTopLevel(SHOPPING_KEY)

        assertEquals(listOf(CalendarRoute, ShoppingRoute), backStack)
    }
}
