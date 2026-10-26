package com.tavola.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.tavola.core.designsystem.scaffold.LocalFloatingBarState
import com.tavola.core.designsystem.scaffold.TavolaFloatingBarLayer
import com.tavola.core.designsystem.scaffold.rememberFloatingBarState
import com.tavola.feature.calendar.CalendarScreen
import com.tavola.feature.recipes.MealPickerSheetContent
import com.tavola.feature.recipes.RecipeDetailScreen
import com.tavola.feature.recipes.RecipeListScreen

/**
 * Navigation root (Navigation 3). The calendar is the landing screen.
 *
 * The floating bar is a single one drawn above the whole navigation, so changing tab only moves the content and the
 * bar animates its own selection. It slides away while a full-screen detail is open.
 */
@Composable
fun TavolaNavigation() {
    val backStack = rememberNavBackStack(CalendarRoute)
    val barState = rememberFloatingBarState()
    val motion = rememberNavigationMotion()

    val currentTabKey = backStack.lastOrNull().topLevelKey()
    // While a detail covers the bar it keeps showing the tab it will come back to.
    var lastTabKey by remember { mutableStateOf(CALENDAR_KEY) }
    if (currentTabKey != null) lastTabKey = currentTabKey

    // A new destination starts with the bar fully visible, even if the previous one had it hidden by scrolling.
    LaunchedEffect(currentTabKey) { barState.show() }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CompositionLocalProvider(LocalFloatingBarState provides barState) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider<NavKey> {
                    entry<CalendarRoute>(clazzContentKey = { CALENDAR_KEY }, metadata = motion.tabMetadata) {
                        CalendarScreen(
                            onOpenDebugMenu = debugMenuAction(backStack),
                            mealPicker = { target, onClose ->
                                MealPickerSheetContent(date = target.date, type = target.type, current = target.current, onClose = onClose)
                            },
                        )
                    }
                    entry<RecipesRoute>(clazzContentKey = { RECIPES_KEY }, metadata = motion.tabMetadata) {
                        RecipeListScreen(onRecipeClick = { recipeId -> backStack.add(RecipeDetailRoute(recipeId)) })
                    }
                    entry<RecipeDetailRoute>(metadata = motion.sheetMetadata) { route ->
                        SheetEntrySurface {
                            RecipeDetailScreen(recipeId = route.recipeId, onBack = { backStack.removeLastOrNull() })
                        }
                    }
                    debugEntries(backStack)
                },
            )
        }
        TavolaFloatingBarLayer(
            state = barState,
            visible = currentTabKey != null,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            TopLevelNavBar(selectedKey = lastTabKey, onItemClick = { key -> backStack.navigateToTopLevel(key) })
        }
    }
}
