package com.tavola.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.navigation.TavolaFloatingNavBar
import com.tavola.core.designsystem.navigation.TavolaNavBarAction
import com.tavola.core.designsystem.navigation.TavolaNavItem

internal const val CALENDAR_KEY = "calendar"
internal const val RECIPES_KEY = "recipes"
internal const val SHOPPING_KEY = "shopping"

/** Top-level destinations, in the order they sit in the bar. */
private val TopLevelItems = listOf(
    TavolaNavItem(
        key = CALENDAR_KEY,
        labelRes = R.string.nav_calendar,
        icon = Icons.Outlined.CalendarMonth,
        selectedIcon = Icons.Rounded.CalendarMonth,
    ),
    TavolaNavItem(
        key = RECIPES_KEY,
        labelRes = R.string.nav_recipes,
        icon = Icons.Outlined.MenuBook,
        selectedIcon = Icons.Rounded.MenuBook,
    ),
    TavolaNavItem(
        key = SHOPPING_KEY,
        labelRes = R.string.nav_shopping,
        icon = Icons.Outlined.ShoppingBasket,
        selectedIcon = Icons.Rounded.ShoppingBasket,
    ),
)

@Composable
internal fun TopLevelNavBar(selectedKey: String, onItemClick: (key: String) -> Unit, action: TavolaNavBarAction? = null) {
    TavolaFloatingNavBar(items = TopLevelItems, selectedKey = selectedKey, onItemClick = onItemClick, action = action)
}

/**
 * Switches to the top-level destination [key]. The back stack always starts at the calendar, so going back from
 * another tab returns to the calendar, and tapping a tab again drops whatever was opened above it.
 */
internal fun MutableList<NavKey>.navigateToTopLevel(key: String) {
    when (key) {
        CALENDAR_KEY -> while (size > 1) removeAt(lastIndex)

        RECIPES_KEY -> {
            val recipesIndex = indexOf(RecipesRoute)
            if (recipesIndex < 0) {
                add(RecipesRoute)
            } else {
                while (size > recipesIndex + 1) removeAt(lastIndex)
            }
        }

        SHOPPING_KEY -> {
            val shoppingIndex = indexOf(ShoppingRoute)
            if (shoppingIndex < 0) {
                add(ShoppingRoute)
            } else {
                while (size > shoppingIndex + 1) removeAt(lastIndex)
            }
        }
    }
}

/** The tabs, in the order they sit in the bar (which is also the way they slide). */
private val TopLevelOrder = listOf(CALENDAR_KEY, RECIPES_KEY, SHOPPING_KEY)

/**
 * Bar key of the tab this is: either the route of a tab or the content key its entry was registered with. `null` for
 * anything that is not a tab (a detail, the debug screens).
 */
internal fun Any?.topLevelKey(): String? = when (this) {
    CalendarRoute, CALENDAR_KEY -> CALENDAR_KEY
    RecipesRoute, RECIPES_KEY -> RECIPES_KEY
    ShoppingRoute, SHOPPING_KEY -> SHOPPING_KEY
    else -> null
}

/** Position of a tab in the bar (so which way the tabs slide), or `null` if this is not a tab. */
internal fun Any?.topLevelIndex(): Int? = TopLevelOrder.indexOf(topLevelKey()).takeIf { index -> index >= 0 }
