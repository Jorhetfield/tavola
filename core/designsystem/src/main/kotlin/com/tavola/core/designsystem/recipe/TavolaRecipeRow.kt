package com.tavola.core.designsystem.recipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Restaurant
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

/** Receta como fila de lista. [subtitle] llega ya formateado ("50 min · Español"); [selected] marca la opción actual. */
@Immutable
data class RecipeRowModel(val id: String, val title: String, val subtitle: String? = null, val selected: Boolean = false)

private val BadgeSize = 44.dp
private val BadgeIconSize = 22.dp
private const val PRESSED_SCALE = 0.97f

/**
 * Fila de receta con respuesta táctil: se encoge un poco al pulsar (resorte espacial) y, al quedar seleccionada, cambia
 * de color (animación de efectos) y muestra un check que entra con un resorte.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaRecipeRow(model: RecipeRowModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(if (pressed) PRESSED_SCALE else 1f, motion.fastSpatialSpec(), label = "rowScale")
    val container by animateColorAsState(
        if (model.selected) scheme.primaryContainer else scheme.surfaceContainerHigh,
        motion.defaultEffectsSpec(),
        label = "rowContainer",
    )
    val content by animateColorAsState(
        if (model.selected) scheme.onPrimaryContainer else scheme.onSurface,
        motion.defaultEffectsSpec(),
        label = "rowContent",
    )
    val badge by animateColorAsState(
        if (model.selected) scheme.primary else scheme.tertiaryContainer,
        motion.defaultEffectsSpec(),
        label = "rowBadge",
    )
    val badgeContent by animateColorAsState(
        if (model.selected) scheme.onPrimary else scheme.onTertiaryContainer,
        motion.defaultEffectsSpec(),
        label = "rowBadgeContent",
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = content,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(BadgeSize)
                    .clip(CircleShape)
                    .background(badge),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = badgeContent, modifier = Modifier.size(BadgeIconSize))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                model.subtitle?.let { detail ->
                    Text(text = detail, style = MaterialTheme.typography.bodyMedium, color = content.copy(alpha = 0.75f))
                }
            }
            AnimatedVisibility(
                visible = model.selected,
                enter = fadeIn(motion.defaultEffectsSpec()) + scaleIn(motion.fastSpatialSpec()),
                exit = fadeOut(motion.defaultEffectsSpec()) + scaleOut(motion.fastSpatialSpec()),
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = content)
            }
        }
    }
}

@TavolaPreviews
@Composable
private fun TavolaRecipeRowPreview() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        ) {
            TavolaRecipeRow(RecipeRowModel("1", "Paella valenciana", "50 min · Español", selected = true), onClick = {})
            TavolaRecipeRow(RecipeRowModel("2", "Moussaka", "70 min · Griego"), onClick = {})
            TavolaRecipeRow(RecipeRowModel("3", "Ensalada de tomate, feta y aceitunas kalamata con orégano"), onClick = {})
        }
    }
}
