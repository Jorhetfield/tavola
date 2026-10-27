package com.tavola.core.designsystem.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.rounded.Add
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

private const val ICON_POP_START_SCALE = 0.7f

/**
 * Floating navigation bar shaped like a pill.
 *
 * Conscious decision: M3 Expressive has no official floating navigation bar, so `HorizontalFloatingToolbar` (meant for
 * contextual actions) is used as the main navigation. It overlaps the content (see `TavolaScaffold`, which also hides
 * it on scroll) and does NOT use `fillMaxWidth()`: it works out its own width.
 *
 * The selection is one number (a position along the bar) animated with a spatial spring. Every item opens or closes
 * its label according to how close that position is to it, and the pill is placed between the two items the position
 * is between, all in the same layout pass, so the pill always sits exactly under the icon it is moving to. The chosen
 * icon pops and choosing another destination gives a light haptic tick.
 *
 * With an [action] the toolbar docks a round button at its end (the M3 Expressive toolbar-with-FAB), so bar and action
 * read as one piece instead of two floating things stacked.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaFloatingNavBar(
    items: List<TavolaNavItem>,
    selectedKey: String,
    onItemClick: (key: String) -> Unit,
    modifier: Modifier = Modifier,
    action: TavolaNavBarAction? = null,
) {
    val motion = MaterialTheme.motionScheme
    val haptics = LocalHapticFeedback.current
    val selectedIndex = items.indexOfFirst { item -> item.key == selectedKey }.coerceAtLeast(0)
    val position = remember { Animatable(selectedIndex.toFloat()) }

    LaunchedEffect(selectedIndex) { position.animateTo(selectedIndex.toFloat(), motion.defaultSpatialSpec()) }

    val destinations: @Composable RowScope.() -> Unit = {
        NavBarLayout(position = { position.value }) {
            Box(Modifier.background(MaterialTheme.colorScheme.primary, CircleShape))
            items.forEachIndexed { index, item ->
                NavBarItem(
                    item = item,
                    selected = index == selectedIndex,
                    openness = { opennessAt(index, position.value) },
                    onClick = {
                        if (index != selectedIndex) haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onItemClick(item.key)
                    },
                )
            }
        }
    }
    val colors = FloatingToolbarDefaults.standardFloatingToolbarColors()

    if (action == null) {
        HorizontalFloatingToolbar(expanded = true, modifier = modifier, colors = colors, content = destinations)
    } else {
        HorizontalFloatingToolbar(
            expanded = true,
            floatingActionButton = { NavBarActionButton(action) },
            modifier = modifier,
            colors = colors,
            content = destinations,
        )
    }
}

/** The round button docked to the bar; the vibrant FAB is the one M3 pairs with the standard toolbar colors. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NavBarActionButton(action: TavolaNavBarAction) {
    FloatingToolbarDefaults.VibrantFloatingActionButton(onClick = action.onClick) {
        Icon(imageVector = action.icon, contentDescription = stringResource(action.contentDescriptionRes))
    }
}

/** 1 when the animated [position] is exactly on item [index], 0 when it is a whole item or more away. */
private fun opennessAt(index: Int, position: Float): Float = (1f - abs(position - index)).coerceIn(0f, 1f)

/**
 * Places the items in a row and the pill (the first child) between the two items that [position] lies between,
 * growing or shrinking with them. Reading [position] here keeps the animation in the layout phase.
 */
@Composable
private fun NavBarLayout(position: () -> Float, content: @Composable () -> Unit) {
    Layout(content = content) { measurables, constraints ->
        if (measurables.size <= 1) return@Layout layout(0, 0) {}

        val itemConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val items = measurables.drop(1).map { measurable -> measurable.measure(itemConstraints) }
        val height = items.maxOf { item -> item.height }
        val lefts = items.runningFold(0) { left, item -> left + item.width }
        val last = items.lastIndex

        val clamped = position().coerceIn(0f, last.toFloat())
        val lower = floor(clamped).toInt()
        val upper = min(lower + 1, last)
        val fraction = clamped - lower
        val pillLeft = lerp(lefts[lower].toFloat(), lefts[upper].toFloat(), fraction)
        val pillWidth = lerp(items[lower].width.toFloat(), items[upper].width.toFloat(), fraction)
        val pill = measurables.first().measure(Constraints.fixed(pillWidth.roundToInt(), height))

        layout(lefts.last(), height) {
            pill.place(pillLeft.roundToInt(), 0)
            items.forEachIndexed { index, item -> item.place(lefts[index], (height - item.height) / 2) }
        }
    }
}

/**
 * One destination. [openness] says how much it is the selected one (0 to 1): its horizontal padding and its label
 * grow with it, measured in the layout phase so they stay in step with the pill.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NavBarItem(item: TavolaNavItem, selected: Boolean, openness: () -> Float, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    val colors = MaterialTheme.colorScheme
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "navItemContent",
    )
    val label = stringResource(item.labelRes)

    Row(
        modifier = modifier
            .clip(CircleShape)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .layout { measurable, constraints ->
                val padding = lerp(spacing.md, spacing.lg, openness()).roundToPx()
                val placeable = measurable.measure(constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity))
                val width = (placeable.width + 2 * padding).coerceAtLeast(constraints.minWidth)
                val height = placeable.height.coerceAtLeast(constraints.minHeight)
                layout(width, height) { placeable.place((width - placeable.width) / 2, (height - placeable.height) / 2) }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The label is always in the tree (only clipped), so it names the item; the icon must not repeat it for screen readers.
        PoppingIcon(item = item, selected = selected, tint = contentColor)
        Row(
            modifier = Modifier
                .clipToBounds()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity))
                    val width = (placeable.width * openness()).roundToInt()
                    layout(width, placeable.height) { placeable.place(0, 0) }
                }
                .graphicsLayer { alpha = openness() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(spacing.sm))
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = contentColor, maxLines = 1)
        }
    }
}

/** The icon grows from a little smaller with a spring each time its item becomes the selected one. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PoppingIcon(item: TavolaNavItem, selected: Boolean, tint: Color) {
    val motion = MaterialTheme.motionScheme
    val scale = remember { Animatable(1f) }
    val wasSelected = remember { booleanArrayOf(selected) }

    LaunchedEffect(selected) {
        if (selected && !wasSelected[0]) {
            scale.snapTo(ICON_POP_START_SCALE)
            scale.animateTo(1f, motion.defaultSpatialSpec())
        }
        wasSelected[0] = selected
    }

    Icon(
        imageVector = if (selected) item.selectedIcon else item.icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.scale(scale.value),
    )
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
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.lg),
        ) {
            TavolaFloatingNavBar(items = PreviewItems, selectedKey = "calendar", onItemClick = {})
            TavolaFloatingNavBar(
                items = PreviewItems,
                selectedKey = "recipes",
                onItemClick = {},
                action = TavolaNavBarAction(Icons.Rounded.Add, R.string.nav_action_new_recipe) {},
            )
        }
    }
}
