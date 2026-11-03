package com.tavola.core.designsystem.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

private val TodayElevation = 8.dp
private const val FLIP_DEGREES = 180f
private const val MUTED_CAPTION_ALPHA = 0.7f

/** The weekday column is at least this many steps of the largest spacing wide, enough for "MIÉRCOLES", so the meals line up. */
private const val WEEKDAY_MIN_WIDTH_STEPS = 2

/** With text larger than this the meals of a collapsed day go under the date instead of beside it, where they would not fit. */
private const val STACKED_FONT_SCALE = 1.3f

/**
 * Card of a day of the calendar: a header with the big number in serif, lunch and dinner side by side and the quick tasks
 * below. The state of the day changes its look: today (saffron, highlighted), upcoming (Mediterranean blue) and past (muted).
 *
 * A past day can be collapsed ([DayCardModel.expanded]) to save room: it becomes one compact, flat row with its date, its
 * weekday, the names of its two meals and how many tasks are left. A tap on the header ([onToggleExpanded]) opens it into the
 * full card. The header is the only control that expands it, so once it is open the meals and the tasks keep their own
 * controls. The shape, the padding, the number, the chevron and the content move together with the springs of the theme.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaDayCard(
    model: DayCardModel,
    onMealClick: (MealType) -> Unit,
    onTaskCheckedChange: (taskId: String, checked: Boolean) -> Unit,
    onTaskRemoveClick: (taskId: String) -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleExpanded: () -> Unit = {},
) {
    val spacing = TavolaTheme.spacing
    val motion = MaterialTheme.motionScheme
    val palette = dayCardPalette(model.state)
    // 1 when the day is shown in full, 0 when it is the compact row; it drives the shape, the padding, the number and the chevron.
    val openness by animateFloatAsState(if (model.isShownInFull) 1f else 0f, motion.defaultSpatialSpec(), label = "dayCardOpenness")
    val isCompactRow = model.isCollapsible && !model.expanded
    val mealsBesideDate = LocalDensity.current.fontScale <= STACKED_FONT_SCALE

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = morphedShape(from = MaterialTheme.shapes.large, to = MaterialTheme.shapes.extraLarge, fraction = openness),
        color = palette.container,
        contentColor = palette.content,
        shadowElevation = if (model.state == DayState.Today) TodayElevation else 0.dp,
    ) {
        Column(
            modifier = Modifier
                // A collapsible card changes its height with the visibility of its content below; the others (when a task is
                // added) let the content size animate.
                .then(if (model.isCollapsible) Modifier else Modifier.animateContentSize(motion.defaultSpatialSpec()))
                .padding(horizontal = spacing.lg, vertical = lerp(spacing.md, spacing.lg, openness).coerceAtLeast(0.dp)),
        ) {
            DayCardHeader(model, palette, openness, showMeals = isCompactRow && mealsBesideDate, onToggleExpanded)
            AnimatedVisibility(
                visible = isCompactRow && !mealsBesideDate,
                enter = expandVertically(motion.defaultSpatialSpec<IntSize>()) + fadeIn(motion.defaultEffectsSpec()),
                exit = shrinkVertically(motion.fastSpatialSpec<IntSize>()) + fadeOut(motion.fastEffectsSpec()),
            ) {
                CompactMeals(model, palette, Modifier.padding(top = spacing.sm))
            }
            AnimatedVisibility(
                visible = model.isShownInFull,
                enter = expandVertically(motion.defaultSpatialSpec<IntSize>()) + fadeIn(motion.defaultEffectsSpec()),
                exit = shrinkVertically(motion.fastSpatialSpec<IntSize>()) + fadeOut(motion.fastEffectsSpec()),
            ) {
                Column(modifier = Modifier.padding(top = spacing.lg), verticalArrangement = Arrangement.spacedBy(spacing.lg)) {
                    DayCardMeals(model, palette, onMealClick)
                    // A day that is over only shows its tasks if it has any; the others always show the zone, where "Add task" lives.
                    if (model.isEditable || model.tasks.isNotEmpty()) {
                        DayCardTasks(model, palette, onTaskCheckedChange, onTaskRemoveClick, onAddTaskClick)
                    }
                }
            }
        }
    }
}

/** A rounded shape whose corners go from those of [from] to those of [to] as [fraction] goes from 0 to 1. */
@Composable
private fun morphedShape(from: CornerBasedShape, to: CornerBasedShape, fraction: Float): Shape {
    val density = LocalDensity.current
    val fromRadius = with(density) { from.topStart.toPx(Size.Zero, density).toDp() }
    val toRadius = with(density) { to.topStart.toPx(Size.Zero, density).toDp() }
    return RoundedCornerShape(lerp(fromRadius, toRadius, fraction).coerceAtLeast(0.dp))
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DayCardHeader(
    model: DayCardModel,
    palette: DayCardPalette,
    openness: Float,
    showMeals: Boolean,
    onToggleExpanded: () -> Unit,
) {
    val spacing = TavolaTheme.spacing
    val typography = MaterialTheme.typography
    val motion = MaterialTheme.motionScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (model.isCollapsible) Modifier.expandsOnClick(model.expanded, onToggleExpanded) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = model.dayNumber,
            style = morphedStyle(from = typography.titleLarge, to = typography.displaySmall, fraction = openness),
            color = palette.content,
            // A minimum width, so the weekday and the meals start at the same place in every row whatever the digits.
            modifier = Modifier.widthIn(min = spacing.xxl),
        )
        Column(modifier = Modifier.padding(start = spacing.md).widthIn(min = spacing.xxxl * WEEKDAY_MIN_WIDTH_STEPS)) {
            Text(
                text = model.weekday.uppercase(),
                style = typography.labelLarge,
                color = palette.content.copy(alpha = 0.85f),
            )
            DayCaption(model, palette)
        }
        CompactMealsReveal(
            visible = showMeals,
            model = model,
            palette = palette,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.md),
        )
        if (model.isCollapsible) {
            AnimatedVisibility(
                visible = model.isCollapsible && !model.expanded && model.pendingTasks > 0,
                enter = fadeIn(motion.defaultEffectsSpec()) + scaleIn(motion.defaultSpatialSpec()),
                exit = fadeOut(motion.fastEffectsSpec()) + scaleOut(motion.fastSpatialSpec()),
            ) {
                PendingTasksCount(model.pendingTasks, palette, Modifier.padding(end = spacing.sm))
            }
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = palette.content,
                modifier = Modifier.graphicsLayer { rotationZ = openness * FLIP_DEGREES },
            )
        }
    }
}

/** The meals of the compact row fade in and out in the space the header leaves free between the date and the controls. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CompactMealsReveal(visible: Boolean, model: DayCardModel, palette: DayCardPalette, modifier: Modifier = Modifier) {
    val motion = MaterialTheme.motionScheme
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(motion.defaultEffectsSpec()),
            exit = fadeOut(motion.fastEffectsSpec()),
        ) {
            CompactMeals(model, palette)
        }
    }
}

/** The text style of the number of the day while it grows from the collapsed size ([from]) to the full one ([to]). */
private fun morphedStyle(from: TextStyle, to: TextStyle, fraction: Float): TextStyle = to.copy(
    fontSize = lerp(from.fontSize, to.fontSize, fraction),
    lineHeight = lerp(from.lineHeight, to.lineHeight, fraction),
)

/** Makes the header a button that opens or closes the day, announced as expanded or collapsed. */
@Composable
private fun Modifier.expandsOnClick(expanded: Boolean, onToggle: () -> Unit): Modifier {
    val actionLabel = stringResource(if (expanded) R.string.day_card_collapse else R.string.day_card_expand)
    val stateText = stringResource(if (expanded) R.string.day_card_state_expanded else R.string.day_card_state_collapsed)
    return this
        .clip(MaterialTheme.shapes.large)
        .minimumInteractiveComponentSize()
        .clickable(onClickLabel = actionLabel, role = Role.Button, onClick = onToggle)
        .semantics { stateDescription = stateText }
}

@Composable
private fun DayCaption(model: DayCardModel, palette: DayCardPalette) {
    when {
        model.state == DayState.Today -> TodayBadge(palette)

        model.state == DayState.Past -> if (model.monthShort.isNotEmpty()) {
            Text(
                text = model.monthShort,
                style = MaterialTheme.typography.bodySmall,
                color = palette.content.copy(alpha = MUTED_CAPTION_ALPHA),
            )
        }

        model.isUnplanned -> Text(
            text = stringResource(R.string.day_card_unplanned),
            style = MaterialTheme.typography.bodySmall,
            color = palette.content.copy(alpha = MUTED_CAPTION_ALPHA),
        )
    }
}

@Composable
private fun TodayBadge(palette: DayCardPalette) {
    Text(
        text = stringResource(R.string.day_card_today),
        style = MaterialTheme.typography.labelSmall,
        color = palette.container,
        modifier = Modifier
            .padding(top = TavolaTheme.spacing.xs)
            .clip(CircleShape)
            .background(palette.content)
            .padding(horizontal = TavolaTheme.spacing.sm, vertical = TavolaTheme.spacing.xxs),
    )
}

@Composable
private fun DayCardMeals(model: DayCardModel, palette: DayCardPalette, onMealClick: (MealType) -> Unit) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md),
    ) {
        listOf(model.lunch, model.dinner).forEach { slot ->
            // A day that is over only lets you look at a recipe; an empty slot or a quick meal has nothing to open.
            val clickable = model.isEditable || slot.hasRecipe
            TavolaMealSlot(
                model = slot,
                onClick = if (clickable) ({ onMealClick(slot.type) }) else null,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                containerColor = palette.slotContainer,
                contentColor = palette.slotContent,
            )
        }
    }
}

@Composable
private fun DayCardTasks(
    model: DayCardModel,
    palette: DayCardPalette,
    onTaskCheckedChange: (String, Boolean) -> Unit,
    onTaskRemoveClick: (String) -> Unit,
    onAddTaskClick: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
    ) {
        model.tasks.forEach { task ->
            TavolaTaskChip(
                model = task,
                onCheckedChange = { checked -> onTaskCheckedChange(task.id, checked) },
                onRemoveClick = { onTaskRemoveClick(task.id) },
                removable = model.isEditable,
            )
        }
        if (model.isEditable) TavolaAddTaskChip(onClick = onAddTaskClick, contentColor = palette.content)
    }
}

private val PreviewDays = listOf(
    DayCardModel(
        id = "mon",
        weekday = "Lunes",
        dayNumber = "19",
        state = DayState.Past,
        monthShort = "oct",
        expanded = false,
        lunch = MealSlotModel(MealType.Lunch, "Gazpacho", "15 min"),
        dinner = MealSlotModel(MealType.Dinner, "Tortilla de patatas", "30 min"),
        tasks = listOf(TaskChipModel("t0", "Comprar huevos"), TaskChipModel("t00", "Sacar la basura", done = true)),
    ),
    DayCardModel(
        id = "mon2",
        weekday = "Lunes",
        dayNumber = "19",
        state = DayState.Past,
        monthShort = "oct",
        expanded = true,
        lunch = MealSlotModel(MealType.Lunch, "Gazpacho", "15 min"),
        dinner = MealSlotModel(MealType.Dinner),
        tasks = listOf(TaskChipModel("t0", "Comprar huevos", done = true)),
    ),
    DayCardModel(
        id = "tue",
        weekday = "Martes",
        dayNumber = "20",
        state = DayState.Today,
        lunch = MealSlotModel(MealType.Lunch, "Paella valenciana", "50 min · Español"),
        dinner = MealSlotModel(MealType.Dinner),
        tasks = listOf(
            TaskChipModel("t1", "Sacar pollo a descongelar"),
            TaskChipModel("t2", "Comprar pimientos", done = true),
        ),
    ),
    DayCardModel(
        id = "wed",
        weekday = "Miércoles",
        dayNumber = "21",
        state = DayState.Upcoming,
        lunch = MealSlotModel(MealType.Lunch, "Moussaka", "70 min · Griego"),
        dinner = MealSlotModel(MealType.Dinner, "Risotto de setas", "40 min · Italiano"),
    ),
    DayCardModel(id = "thu", weekday = "Jueves", dayNumber = "22", state = DayState.Upcoming),
)

@TavolaPreviews
@Composable
private fun TavolaDayCardPreview() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.lg),
        ) {
            PreviewDays.forEach { day ->
                TavolaDayCard(day, onMealClick = {}, onTaskCheckedChange = { _, _ -> }, onTaskRemoveClick = {}, onAddTaskClick = {})
            }
        }
    }
}
