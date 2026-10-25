package com.tavola.core.designsystem.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

/**
 * Barra de navegación flotante en forma de píldora.
 *
 * Decisión consciente: M3 Expressive no tiene una barra de navegación flotante oficial, así que se usa
 * `HorizontalFloatingToolbar` (pensado para acciones contextuales) como navegación principal.
 * Va superpuesta al contenido (ver `TavolaScaffold`, que también se encarga de esconderla al hacer scroll)
 * y NO lleva `fillMaxWidth()`: calcula su propio ancho.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaFloatingNavBar(
    items: List<TavolaNavItem>,
    selectedKey: String,
    onItemClick: (key: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
    ) {
        items.forEach { item ->
            NavBarItem(
                item = item,
                selected = item.key == selectedKey,
                onClick = { onItemClick(item.key) },
            )
        }
    }
}

@Composable
private fun NavBarItem(item: TavolaNavItem, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    val colors = MaterialTheme.colorScheme
    val contentColor = if (selected) colors.onPrimary else colors.onSurfaceVariant
    val label = stringResource(item.labelRes)

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(if (selected) colors.primary else Color.Transparent)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .padding(horizontal = if (selected) spacing.lg else spacing.md)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.icon,
            contentDescription = if (selected) null else label,
            tint = contentColor,
        )
        AnimatedVisibility(visible = selected) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(spacing.sm))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    maxLines = 1,
                )
            }
        }
    }
}

private val PreviewItems = listOf(
    TavolaNavItem("calendar", R.string.nav_calendar, Icons.Outlined.CalendarMonth, Icons.Rounded.CalendarMonth),
    TavolaNavItem("recipes", R.string.nav_recipes, Icons.Outlined.MenuBook, Icons.Rounded.MenuBook),
    TavolaNavItem("shopping", R.string.nav_shopping, Icons.Outlined.ShoppingCart, Icons.Rounded.ShoppingCart),
)

@TavolaPreviews
@Composable
private fun TavolaFloatingNavBarPreview() {
    TavolaTheme {
        TavolaFloatingNavBar(items = PreviewItems, selectedKey = "calendar", onItemClick = {})
    }
}
