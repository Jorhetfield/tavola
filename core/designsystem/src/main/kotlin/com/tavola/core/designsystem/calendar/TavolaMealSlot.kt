package com.tavola.core.designsystem.calendar

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/**
 * Hueco de comida o cena. Lleno: título de la receta y detalle. Vacío: borde discontinuo con "Añadir".
 * [containerColor] y [contentColor] permiten a la tarjeta de día adaptarlo a su estado.
 */
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

    Column(
        modifier = modifier
            .clip(shape)
            .then(
                if (model.isEmpty) {
                    Modifier.dashedBorder(contentColor.copy(alpha = 0.4f), shape)
                } else {
                    Modifier.background(containerColor)
                },
            )
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = SlotMinHeight)
            .padding(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        SlotHeader(type = model.type, contentColor = contentColor)
        Spacer(Modifier.height(spacing.xxs))
        if (model.isEmpty) {
            EmptySlotBody(contentColor)
        } else {
            FilledSlotBody(model, contentColor)
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
                tint = if (isLunch) Color(0xFF3D2200) else Color.White,
                modifier = Modifier.size(BadgeIconSize),
            )
        }
        Text(
            text = stringResource(if (isLunch) R.string.calendar_meal_lunch else R.string.calendar_meal_dinner),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun FilledSlotBody(model: MealSlotModel, contentColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.xxs)) {
        Text(
            text = model.title.orEmpty(),
            style = MaterialTheme.typography.titleSmall,
            color = contentColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        model.subtitle?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.7f))
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
            text = stringResource(R.string.calendar_meal_add),
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
        drawOutline(outline, color, style = stroke)
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
