package com.tavola.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.tavola.feature.calendar.CalendarScreen
import com.tavola.feature.recipes.MealPickerSheetContent
import com.tavola.feature.recipes.RecipeDetailScreen
import com.tavola.feature.recipes.RecipeListScreen

/** Navigation root (Navigation 3). The calendar is the landing screen. */
@Composable
fun TavolaNavigation() {
    val backStack = rememberNavBackStack(CalendarRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider<NavKey> {
            entry<CalendarRoute> {
                CalendarScreen(
                    floatingBar = { TopLevelNavBar(CALENDAR_KEY, onItemClick = { key -> backStack.navigateToTopLevel(key) }) },
                    onOpenDebugMenu = debugMenuAction(backStack),
                    mealPicker = { target, onClose ->
                        MealPickerSheetContent(date = target.date, type = target.type, current = target.current, onClose = onClose)
                    },
                )
            }
            entry<RecipesRoute> {
                RecipeListScreen(
                    onRecipeClick = { recipeId -> backStack.add(RecipeDetailRoute(recipeId)) },
                    floatingBar = { TopLevelNavBar(RECIPES_KEY, onItemClick = { key -> backStack.navigateToTopLevel(key) }) },
                )
            }
            entry<RecipeDetailRoute> { route ->
                RecipeDetailScreen(recipeId = route.recipeId, onBack = { backStack.removeLastOrNull() })
            }
            debugEntries(backStack)
        },
    )
}
