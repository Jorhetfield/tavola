package com.tavola.core.designsystem.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

private val BadgeSize = 32.dp
private val BadgeIconSize = 18.dp
private val SlotMinHeight = 112.dp
private const val EMPTY_BORDER_ALPHA = 0.4f
private const val ENTER_OFFSET_DIVISOR = 4

/**
 * Hueco de comida o cena. Lleno: título de la receta o comida y detalle. Vacío: borde discontinuo con "Añadir".
 * Al rellenarse o vaciarse, el borde se desvanece, el fondo cambia de color (animación de efectos) y el contenido entra
 * deslizándose con un resorte espacial. [containerColor] y [contentColor] permiten a la tarjeta de día adaptarlo a su estado.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaMealSlot(
    model: MealSlotModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = TavolaTheme.colors.mealSlot,
    contentColor: Color = TavolaTheme.colors.onMealSlot,
) {
    val spacing = TavolaTheme.spacing
    val shape = MaterialTheme.shapes.large
    val motion = MaterialTheme.motionScheme
    val background by animateColorAsState(
        if (model.isEmpty) Color.Transparent else containerColor,
        motion.defaultEffectsSpec(),
        label = "slotBackground",
    )
    val borderAlpha by animateFloatAsState(
        if (model.isEmpty) EMPTY_BORDER_ALPHA else 0f,
        motion.defaultEffectsSpec(),
        label = "slotBorder",
    )

    Column(
        modifier = modifier
            .clip(shape)
            .background(background)
            .dashedBorder(contentColor.copy(alpha = borderAlpha), shape)
            .clickable(onClick = onClick)
            .animateContentSize(motion.defaultSpatialSpec())
            .defaultMinSize(minHeight = SlotMinHeight)
            .padding(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        SlotHeader(type = model.type, contentColor = contentColor)
        Spacer(Modifier.height(spacing.xxs))
        AnimatedContent(
            targetState = model.title to model.subtitle,
            transitionSpec = {
                val slideUp = slideInVertically(motion.defaultSpatialSpec()) { fullHeight -> fullHeight / ENTER_OFFSET_DIVISOR }
                val enter = fadeIn(motion.defaultEffectsSpec()) + slideUp
                enter togetherWith fadeOut(motion.fastEffectsSpec())
            },
            label = "slotBody",
        ) { (title, subtitle) ->
            if (title == null) EmptySlotBody(contentColor) else FilledSlotBody(title, subtitle, contentColor)
        }
    }
}

@Composable
private fun SlotHeader(type: MealType, contentColor: Color) {
    val colors = TavolaTheme.colors
    val isLunch = type == MealType.Lunch
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(BadgeSize)
                .clip(CircleShape)
                .background(if (isLunch) colors.lunchAccent else colors.dinnerAccent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isLunch) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                contentDescription = null,
                tint = if (isLunch) colors.onDayCardActive else MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(BadgeIconSize),
            )
        }
        Text(
            text = stringResource(if (isLunch) R.string.meal_slot_lunch else R.string.meal_slot_dinner),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun FilledSlotBody(title: String, subtitle: String?, contentColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.xxs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = contentColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        subtitle?.let { detail ->
            Text(text = detail, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun EmptySlotBody(contentColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.xs),
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null, tint = contentColor, modifier = Modifier.size(BadgeIconSize))
        Text(
            text = stringResource(R.string.meal_slot_add),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}

private fun Modifier.dashedBorder(color: Color, shape: Shape): Modifier = drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val stroke = Stroke(
        width = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12.dp.toPx(), 8.dp.toPx())),
    )
    onDrawWithContent {
        drawContent()
        if (color.alpha > 0f) drawOutline(outline, color, style = stroke)
    }
}

@TavolaPreviews
@Composable
private fun TavolaMealSlotPreview() {
    TavolaTheme {
        Row(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md),
        ) {
            TavolaMealSlot(
                model = MealSlotModel(MealType.Lunch, "Paella valenciana", "50 min · Español"),
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            TavolaMealSlot(
                model = MealSlotModel(MealType.Dinner),
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
