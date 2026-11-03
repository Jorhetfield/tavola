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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
 * Lunch or dinner slot. Filled: the title of the recipe or meal and a detail. Empty: a dashed border with "Add".
 * When it is filled or emptied, the border fades, the background changes color (effects animation) and the content slides
 * in with a spatial spring. [containerColor] and [contentColor] let the day card adapt it to its state.
 *
 * A `null` [onClick] makes the slot read-only (a day that is over): it has no touch response and an empty one shows a dash
 * instead of "Add", since nothing can be planned in it. [clickLabel] says what a tap does, for screen readers.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaMealSlot(
    model: MealSlotModel,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    clickLabel: String? = null,
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
        if (model.isEmpty && onClick != null) EMPTY_BORDER_ALPHA else 0f,
        motion.defaultEffectsSpec(),
        label = "slotBorder",
    )

    Column(
        modifier = modifier
            .clip(shape)
            .background(background)
            .dashedBorder(contentColor.copy(alpha = borderAlpha), shape)
            .then(if (onClick == null) Modifier else Modifier.clickable(onClickLabel = clickLabel, role = Role.Button, onClick = onClick))
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
            when {
                title != null -> FilledSlotBody(title, subtitle, contentColor)
                onClick == null -> ReadOnlyEmptySlotBody(contentColor)
                else -> EmptySlotBody(contentColor)
            }
        }
    }
}

@Composable
private fun SlotHeader(type: MealType, contentColor: Color) {
    val isLunch = type == MealType.Lunch
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
    ) {
        MealBadge(type)
        Text(
            text = stringResource(if (isLunch) R.string.meal_slot_lunch else R.string.meal_slot_dinner),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor.copy(alpha = 0.8f),
        )
    }
}

/** The round sun (lunch) or moon (dinner) badge that tells the two meals apart; [contentDescription] names it when no text does. */
@Composable
internal fun MealBadge(
    type: MealType,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = BadgeSize,
    iconSize: Dp = BadgeIconSize,
) {
    val colors = TavolaTheme.colors
    val isLunch = type == MealType.Lunch
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isLunch) colors.lunchAccent else colors.dinnerAccent),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isLunch) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
            contentDescription = contentDescription,
            tint = if (isLunch) colors.onDayCardActive else MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.size(iconSize),
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

@Composable
private fun ReadOnlyEmptySlotBody(contentColor: Color) {
    Text(
        text = stringResource(R.string.day_card_summary_empty),
        style = MaterialTheme.typography.titleSmall,
        color = contentColor.copy(alpha = 0.7f),
    )
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
