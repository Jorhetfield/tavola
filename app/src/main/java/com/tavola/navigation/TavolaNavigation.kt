package com.tavola.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.motion.LocalPlayedEntrances
import com.tavola.core.designsystem.motion.rememberPlayedEntrances
import com.tavola.core.designsystem.navigation.TavolaNavBarAction
import com.tavola.core.designsystem.scaffold.LocalFloatingBarState
import com.tavola.core.designsystem.scaffold.TavolaFloatingBarLayer
import com.tavola.core.designsystem.scaffold.rememberFloatingBarState
import com.tavola.feature.calendar.CalendarScreen
import com.tavola.feature.groups.MyGroupScreen
import com.tavola.feature.recipes.MealPickerSheetContent
import com.tavola.feature.recipes.RecipeDetailScreen
import com.tavola.feature.recipes.RecipeEditorScreen
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
    val playedEntrances = rememberPlayedEntrances()

    val currentTabKey = backStack.lastOrNull().topLevelKey()
    // While a detail covers the bar it keeps showing the tab it will come back to. A plain holder, not state: it only
    // has to survive recompositions and the bar reads it in this same pass, so nothing is written during composition.
    val lastTab = remember { LastTab(CALENDAR_KEY) }
    if (currentTabKey != null) lastTab.key = currentTabKey

    // A new destination starts with the bar fully visible, even if the previous one had it hidden by scrolling.
    LaunchedEffect(currentTabKey) { barState.show() }

    // The recipes tab is the only one with a main action; it is docked to the bar and opens the editor on a blank recipe.
    val newRecipeAction = remember(backStack) {
        TavolaNavBarAction(Icons.Rounded.Add, R.string.nav_action_new_recipe) { backStack.add(RecipeEditorRoute()) }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CompositionLocalProvider(LocalFloatingBarState provides barState, LocalPlayedEntrances provides playedEntrances) {
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
                            onOpenRecipe = { recipeId -> backStack.add(RecipeDetailRoute(recipeId)) },
                            onOpenGroup = { backStack.add(GroupRoute) },
                            mealPicker = { target, onClose ->
                                MealPickerSheetContent(date = target.date, type = target.type, current = target.current, onClose = onClose)
                            },
                        )
                    }
                    entry<RecipesRoute>(clazzContentKey = { RECIPES_KEY }, metadata = motion.tabMetadata) {
                        RecipeListScreen(
                            onRecipeClick = { recipeId -> backStack.add(RecipeDetailRoute(recipeId)) },
                        )
                    }
                    entry<RecipeDetailRoute>(metadata = motion.sheetMetadata) { route ->
                        SheetEntrySurface {
                            RecipeDetailScreen(
                                recipeId = route.recipeId,
                                onBack = { backStack.removeLastOrNull() },
                                onEditClick = { recipeId -> backStack.add(RecipeEditorRoute(recipeId)) },
                            )
                        }
                    }
                    entry<GroupRoute>(metadata = motion.sheetMetadata) {
                        SheetEntrySurface {
                            MyGroupScreen(onBack = { backStack.removeLastOrNull() })
                        }
                    }
                    entry<RecipeEditorRoute>(metadata = motion.sheetMetadata) { route ->
                        SheetEntrySurface {
                            RecipeEditorScreen(recipeId = route.recipeId, onBack = { backStack.removeLastOrNull() })
                        }
                    }
                },
            )
        }
        TavolaFloatingBarLayer(
            state = barState,
            visible = currentTabKey != null,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            TopLevelNavBar(
                selectedKey = lastTab.key,
                onItemClick = { key -> backStack.navigateToTopLevel(key) },
                action = newRecipeAction.takeIf { lastTab.key == RECIPES_KEY },
            )
        }
    }
}

/** The last tab that was on screen, kept so the bar can keep showing it while a detail covers it. */
private class LastTab(var key: String)
