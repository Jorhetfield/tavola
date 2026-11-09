package com.tavola.core.designsystem.shopping

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

/** An item of the shopping list as a row. [quantity] arrives already formatted ("2 kg"); [checked] is the item already in the basket. */
@Immutable
data class ShoppingRowModel(val id: String, val name: String, val quantity: String? = null, val checked: Boolean = false)

private val CheckSize = 28.dp
private val CheckIconSize = 18.dp
private val CheckBorderWidth = 2.dp
private val RowMinHeight = 56.dp
private const val PRESSED_SCALE = 0.97f
private const val CHECKED_CONTENT_ALPHA = 0.6f

/**
 * An item of the shopping list. The whole row is the checkbox: tapping it ticks the item with a spring (the circle fills with
 * the tertiary color and the tick pops in), the name is struck through and the row calms down to a lower surface so what is
 * still missing stands out. It shrinks a little while pressed. [quantity] is a small pill on the end.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaShoppingRow(model: ShoppingRowModel, onCheckedChange: (checked: Boolean) -> Unit, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(if (pressed) PRESSED_SCALE else 1f, motion.fastSpatialSpec(), label = "shoppingRowScale")
    val container by animateColorAsState(
        if (model.checked) scheme.surfaceContainerLow else scheme.surfaceContainerHigh,
        motion.defaultEffectsSpec(),
        label = "shoppingRowContainer",
    )
    val contentAlpha by animateFloatAsState(
        if (model.checked) CHECKED_CONTENT_ALPHA else 1f,
        motion.defaultEffectsSpec(),
        label = "shoppingRowContentAlpha",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(MaterialTheme.shapes.large)
            .toggleable(
                value = model.checked,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = scheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = RowMinHeight)
                .padding(horizontal = spacing.md, vertical = spacing.sm)
                .graphicsLayer { alpha = contentAlpha },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            CheckCircle(checked = model.checked)
            Text(
                text = model.name,
                style = MaterialTheme.typography.titleMedium,
                textDecoration = if (model.checked) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            model.quantity?.let { quantity ->
                Text(
                    text = quantity,
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(scheme.secondaryContainer)
                        .padding(horizontal = spacing.md, vertical = spacing.xs),
                )
            }
        }
    }
}

/** The round tick box: empty with an outline, or filled with the tertiary color and a tick that pops in. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CheckCircle(checked: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val fill by animateColorAsState(
        if (checked) scheme.tertiary else scheme.surfaceContainerHigh,
        motion.defaultEffectsSpec(),
        label = "shoppingRowCheckFill",
    )
    val border by animateColorAsState(
        if (checked) scheme.tertiary else scheme.outline,
        motion.defaultEffectsSpec(),
        label = "shoppingRowCheckBorder",
    )
    Box(
        modifier = Modifier
            .size(CheckSize)
            .clip(CircleShape)
            .background(fill)
            .border(CheckBorderWidth, border, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = fadeIn(motion.defaultEffectsSpec()) + scaleIn(motion.fastSpatialSpec()),
            exit = fadeOut(motion.fastEffectsSpec()) + scaleOut(motion.fastSpatialSpec()),
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = scheme.onTertiary, modifier = Modifier.size(CheckIconSize))
        }
    }
}

@TavolaPreviews
@Composable
private fun TavolaShoppingRowPreview() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        ) {
            TavolaShoppingRow(ShoppingRowModel("1", "Tomates", "1 kg"), onCheckedChange = {})
            TavolaShoppingRow(ShoppingRowModel("2", "Aceite de oliva virgen extra", checked = true), onCheckedChange = {})
            TavolaShoppingRow(ShoppingRowModel("3", "Queso feta, aceitunas kalamata y orégano para la ensalada griega"), onCheckedChange = {
            })
        }
    }
}
