package com.tavola.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.runtime.Composable
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.navigation.TavolaFloatingNavBar
import com.tavola.core.designsystem.navigation.TavolaNavItem

/** Destinos de primer nivel. Hoy solo el calendario; Recetas y Compra se añaden cuando existan sus pantallas. */
private val TopLevelItems = listOf(
    TavolaNavItem(
        key = "calendar",
        labelRes = R.string.nav_calendar,
        icon = Icons.Outlined.CalendarMonth,
        selectedIcon = Icons.Rounded.CalendarMonth,
    ),
)

@Composable
internal fun TopLevelNavBar(selectedKey: String, onItemClick: (key: String) -> Unit) {
    TavolaFloatingNavBar(items = TopLevelItems, selectedKey = selectedKey, onItemClick = onItemClick)
}
