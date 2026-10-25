package com.tavola.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.tavola.feature.calendar.CalendarScreen

/** Raíz de la navegación (Navigation 3). El calendario es la landing. */
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
                    floatingBar = { TopLevelNavBar(selectedKey = "calendar", onItemClick = {}) },
                    onOpenDebugMenu = debugMenuAction(backStack),
                )
            }
            debugEntries(backStack)
        },
    )
}
