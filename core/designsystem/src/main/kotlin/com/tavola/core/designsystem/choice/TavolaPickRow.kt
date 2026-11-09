package com.tavola.core.designsystem.choice

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

/**
 * An option that can be ticked in a list of things to pick from (an ingredient to add to the shopping list). [detail] is a small pill on
 * the end ("400 g"); [supporting] is a line under the [label] that says why it cannot be picked ("Already on the list"), and goes
 * with [enabled] off.
 */
@Immutable
data class PickRowModel(
    val id: String,
    val label: String,
    val detail: String? = null,
    val supporting: String? = null,
    val selected: Boolean = false,
    val enabled: Boolean = true,
)

private val CheckSize = 28.dp
private val CheckIconSize = 18.dp
private val CheckBorderWidth = 2.dp
private val RowMinHeight = 56.dp
private const val PRESSED_SCALE = 0.97f
private const val DISABLED_ALPHA = 0.55f

/**
 * A row to tick or untick with a spring-driven tick that fills the circle with the primary color, like [TavolaChoiceTile] but for a
 * long list: the whole row is the checkbox, and it shrinks a little under the finger. A row that is not [PickRowModel.enabled] shows
 * its reason and does not answer to touch.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaPickRow(model: PickRowModel, onSelectedChange: (selected: Boolean) -> Unit, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(if (pressed) PRESSED_SCALE else 1f, motion.fastSpatialSpec(), label = "pickRowScale")
    val container by animateColorAsState(
        if (model.selected) scheme.primaryContainer else scheme.surfaceContainerHigh,
        motion.defaultEffectsSpec(),
        label = "pickRowContainer",
    )
    val content by animateColorAsState(
        if (model.selected) scheme.onPrimaryContainer else scheme.onSurface,
        motion.defaultEffectsSpec(),
        label = "pickRowContent",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (model.enabled) 1f else DISABLED_ALPHA
            }
            .clip(MaterialTheme.shapes.large)
            .toggleable(
                value = model.selected,
                enabled = model.enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Checkbox,
                onValueChange = onSelectedChange,
            ),
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = RowMinHeight)
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            PickCircle(selected = model.selected)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = model.label, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                model.supporting?.let { reason ->
                    Text(text = reason, style = MaterialTheme.typography.bodyMedium, color = content.copy(alpha = 0.75f))
                }
            }
            model.detail?.let { detail ->
                Text(
                    text = detail,
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

/** The round tick box: empty with an outline, or filled with the primary color and a tick that pops in. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PickCircle(selected: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val fill by animateColorAsState(
        if (selected) scheme.primary else scheme.surfaceContainerHigh,
        motion.defaultEffectsSpec(),
        label = "pickRowCircleFill",
    )
    val border by animateColorAsState(
        if (selected) scheme.primary else scheme.outline,
        motion.defaultEffectsSpec(),
        label = "pickRowCircleBorder",
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
            visible = selected,
            enter = fadeIn(motion.defaultEffectsSpec()) + scaleIn(motion.fastSpatialSpec()),
            exit = fadeOut(motion.fastEffectsSpec()) + scaleOut(motion.fastSpatialSpec()),
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(CheckIconSize))
        }
    }
}

@TavolaPreviews
@Composable
private fun TavolaPickRowPreview() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        ) {
            TavolaPickRow(PickRowModel("1", "Arroz bomba", "400 g", selected = true), onSelectedChange = {})
            TavolaPickRow(PickRowModel("2", "Caldo de pollo", "1,2 l"), onSelectedChange = {})
            TavolaPickRow(PickRowModel("3", "Aceite de oliva", supporting = "Ya está en la lista", enabled = false), onSelectedChange = {})
        }
    }
}
