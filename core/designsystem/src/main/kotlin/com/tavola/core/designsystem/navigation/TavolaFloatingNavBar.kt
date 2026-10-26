package com.tavola.core.designsystem.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private const val ICON_POP_START_SCALE = 0.7f

/**
 * Floating navigation bar shaped like a pill.
 *
 * Conscious decision: M3 Expressive has no official floating navigation bar, so `HorizontalFloatingToolbar` (meant for
 * contextual actions) is used as the main navigation. It overlaps the content (see `TavolaScaffold`, which also hides
 * it on scroll) and does NOT use `fillMaxWidth()`: it works out its own width.
 *
 * The selection is one pill that slides to the chosen item with a spatial spring while its label opens, and the
 * chosen icon pops. Choosing another destination gives a light haptic tick.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaFloatingNavBar(
    items: List<TavolaNavItem>,
    selectedKey: String,
    onItemClick: (key: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val itemBounds = remember { mutableStateMapOf<String, Rect>() }
    // Until the pill has been measured and placed, the selected item paints its own background so there is no blank frame.
    var pillPlaced by remember { mutableStateOf(false) }

    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
    ) {
        Box(contentAlignment = Alignment.CenterStart) {
            SelectionPill(bounds = itemBounds[selectedKey], onPlaced = { pillPlaced = true })
            Row(verticalAlignment = Alignment.CenterVertically) {
                items.forEach { item ->
                    NavBarItem(
                        item = item,
                        selected = item.key == selectedKey,
                        paintsOwnBackground = !pillPlaced,
                        onClick = {
                            if (item.key != selectedKey) haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onItemClick(item.key)
                        },
                        modifier = Modifier.onGloballyPositioned { coordinates -> itemBounds[item.key] = coordinates.boundsInParent() },
                    )
                }
            }
        }
    }
}

/** The filled pill behind the selected item. It follows the item's bounds with a spring, so it slides and stretches. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SelectionPill(bounds: Rect?, onPlaced: () -> Unit) {
    val motion = MaterialTheme.motionScheme
    val density = LocalDensity.current
    val left = remember { Animatable(0f) }
    val width = remember { Animatable(0f) }
    val height = remember { Animatable(0f) }
    val placed = remember { booleanArrayOf(false) }

    LaunchedEffect(bounds) {
        if (bounds == null) return@LaunchedEffect
        if (!placed[0]) {
            // The first measurement just puts the pill in place: it must not slide in from the corner.
            left.snapTo(bounds.left)
            width.snapTo(bounds.width)
            height.snapTo(bounds.height)
            placed[0] = true
            onPlaced()
        } else {
            launch { left.animateTo(bounds.left, motion.defaultSpatialSpec()) }
            launch { width.animateTo(bounds.width, motion.defaultSpatialSpec()) }
            launch { height.animateTo(bounds.height, motion.defaultSpatialSpec()) }
        }
    }

    if (bounds != null) {
        Box(
            modifier = Modifier
                .offset { IntOffset(left.value.roundToInt(), 0) }
                .width(with(density) { width.value.toDp() })
                .defaultMinSize(minHeight = with(density) { height.value.toDp() })
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NavBarItem(
    item: TavolaNavItem,
    selected: Boolean,
    paintsOwnBackground: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = TavolaTheme.spacing
    val colors = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        animationSpec = motion.defaultEffectsSpec(),
        label = "navItemContent",
    )
    val label = stringResource(item.labelRes)

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(if (selected && paintsOwnBackground) colors.primary else Color.Transparent)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .padding(horizontal = if (selected) spacing.lg else spacing.md)
            .animateContentSize(motion.defaultSpatialSpec()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PoppingIcon(item = item, selected = selected, tint = contentColor, contentDescription = if (selected) null else label)
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

/** The icon grows from a little smaller with a spring each time its item becomes the selected one. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PoppingIcon(item: TavolaNavItem, selected: Boolean, tint: Color, contentDescription: String?) {
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
        contentDescription = contentDescription,
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
        TavolaFloatingNavBar(items = PreviewItems, selectedKey = "calendar", onItemClick = {})
    }
}
