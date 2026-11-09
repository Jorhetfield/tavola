package com.tavola.core.designsystem.choice

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.SetMeal
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

private val TileMinHeight = 88.dp
private val BadgeSize = 40.dp
private val BadgeIconSize = 22.dp
private const val PRESSED_SCALE = 0.94f

/**
 * One option of a small set of choices laid out in a grid (a category): a round badge with the [icon] over its [label]. Exactly one
 * option is [selected] at a time, so it is announced as a radio button. The selected one turns primary with a spring-driven color
 * change (effects), and every tile shrinks a little under the finger (spatial spring).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaChoiceTile(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(if (pressed) PRESSED_SCALE else 1f, motion.fastSpatialSpec(), label = "choiceTileScale")
    val container by animateColorAsState(
        if (selected) scheme.primaryContainer else scheme.surfaceContainerHigh,
        motion.defaultEffectsSpec(),
        label = "choiceTileContainer",
    )
    val content by animateColorAsState(
        if (selected) scheme.onPrimaryContainer else scheme.onSurface,
        motion.defaultEffectsSpec(),
        label = "choiceTileContent",
    )
    val badge by animateColorAsState(
        if (selected) scheme.primary else scheme.tertiaryContainer,
        motion.defaultEffectsSpec(),
        label = "choiceTileBadge",
    )
    val badgeContent by animateColorAsState(
        if (selected) scheme.onPrimary else scheme.onTertiaryContainer,
        motion.defaultEffectsSpec(),
        label = "choiceTileBadgeContent",
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(MaterialTheme.shapes.large)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = content,
    ) {
        Column(
            modifier = Modifier
                .heightIn(min = TileMinHeight)
                .padding(horizontal = spacing.sm, vertical = spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.sm, Alignment.CenterVertically),
        ) {
            Box(
                modifier = Modifier
                    .size(BadgeSize)
                    .clip(CircleShape)
                    .background(badge),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = badgeContent, modifier = Modifier.size(BadgeIconSize))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                // Two lines are always reserved, so the tiles of a row are as tall as each other whatever their label.
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@TavolaPreviews
@Composable
private fun TavolaChoiceTilePreview() {
    TavolaTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TavolaTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        ) {
            TavolaChoiceTile("Frutas y verduras", Icons.Rounded.Eco, selected = true, onClick = {}, modifier = Modifier.weight(1f))
            TavolaChoiceTile("Carne y pescado", Icons.Rounded.SetMeal, selected = false, onClick = {}, modifier = Modifier.weight(1f))
            TavolaChoiceTile("Despensa", Icons.Rounded.Inventory2, selected = false, onClick = {}, modifier = Modifier.weight(1f))
        }
    }
}
