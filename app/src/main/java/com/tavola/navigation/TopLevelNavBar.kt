package com.tavola.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.navigation.TavolaFloatingNavBar
import com.tavola.core.designsystem.navigation.TavolaNavItem

internal const val CALENDAR_KEY = "calendar"
internal const val RECIPES_KEY = "recipes"

/** Top-level destinations. Shopping is added when its screen exists. */
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
)

@Composable
internal fun TopLevelNavBar(selectedKey: String, onItemClick: (key: String) -> Unit) {
    TavolaFloatingNavBar(items = TopLevelItems, selectedKey = selectedKey, onItemClick = onItemClick)
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
    }
}
