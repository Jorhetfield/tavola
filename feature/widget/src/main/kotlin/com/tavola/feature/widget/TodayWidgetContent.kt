package com.tavola.feature.widget

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal

private val CardCorner = 28.dp
private val SlotCorner = 20.dp
private val ChipCorner = 22.dp
private val TagCorner = 10.dp
private val CardPadding = 14.dp
private val SlotPadding = 10.dp
private val SlotHeight = 118.dp
private val BadgeSize = 26.dp
private val TaskIconSize = 20.dp
private val Gap = 8.dp
private val ShortTopPadding = 10.dp
private val CompactGap = 6.dp
private val CompactSlotPadding = 8.dp
private val CompactBadgeSize = 22.dp

/** How the day is laid out at the size the widget has now. */
private enum class DayLayout {
    /** Tall and wide enough for the full slots, side by side. */
    Full,

    /** Short (a 4x2): the slots are compact and side by side. */
    Short,

    /** Narrow (a 2x2): the compact slots go one under the other. */
    Narrow,
}

/** Under this width the two slots do not fit side by side. */
private val NarrowWidth = 200.dp

/** Under this height the full slots do not fit under the header. */
private val ShortHeight = 200.dp

@Composable
private fun currentLayout(): DayLayout {
    val size = LocalSize.current
    return when {
        size.width < NarrowWidth -> DayLayout.Narrow
        size.height < ShortHeight -> DayLayout.Short
        else -> DayLayout.Full
    }
}

/**
 * The widget for [state], drawn like the card of today in the app: the day, the lunch and dinner slots and the task chips. It is the
 * same whole day at every size, laid out to fit ([DayLayout]), and it scrolls when it does not fit.
 */
@Composable
internal fun TodayWidgetContent(state: TodayWidgetState, launcher: WidgetLauncher, scrollable: Boolean = true) {
    val openApp = actionStartActivity(launcher.openAppIntent())
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(WidgetColors.card)
            .cornerRadius(CardCorner)
            .clickable(openApp),
    ) {
        when (state) {
            TodayWidgetState.SignedOut, TodayWidgetState.NoGroup -> Message(R.string.widget_open_app)
            TodayWidgetState.Unavailable -> Message(R.string.widget_unavailable)
            is TodayWidgetState.Content -> DayCard(state, launcher, openApp, scrollable)
        }
    }
}

/** A meal as a compact slot: its badge beside its title. It is the slot of the widgets that have no room for the full one. */
@Composable
private fun CompactMeal(
    type: MealType,
    meal: PlannedMeal?,
    launcher: WidgetLauncher,
    openApp: Action,
    modifier: GlanceModifier = GlanceModifier.fillMaxWidth(),
) {
    val context = LocalContext.current
    val onClick = (meal as? PlannedMeal.FromRecipe)?.let { fromRecipe ->
        actionStartActivity(launcher.openRecipeIntent(fromRecipe.recipe.recipeId))
    } ?: openApp
    val ink = if (meal == null) WidgetColors.onCard else WidgetColors.onSlot
    Row(
        modifier = modifier
            .background(if (meal == null) WidgetColors.emptySlot else WidgetColors.slot)
            .cornerRadius(SlotCorner)
            .padding(CompactSlotPadding)
            .clickable(onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(type.badgeRes()),
            contentDescription = null,
            modifier = GlanceModifier.size(CompactBadgeSize),
        )
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = meal?.title ?: context.getString(R.string.widget_nothing_planned),
            style = TextStyle(color = ink, fontSize = 13.sp, fontWeight = FontWeight.Bold),
            maxLines = 2,
        )
    }
}

/**
 * The whole day: header, the two slots and a chip per task. When [scrollable] it is one list that scrolls if the widget is not tall
 * enough (what the launcher shows); the debug preview cannot draw lists, so it asks for it laid out in full.
 */
@Composable
private fun DayCard(state: TodayWidgetState.Content, launcher: WidgetLauncher, openApp: Action, scrollable: Boolean) {
    val layout = currentLayout()
    if (scrollable) {
        LazyColumn(modifier = GlanceModifier.fillMaxSize().padding(horizontal = CardPadding)) {
            item { TopBlock(state, layout) }
            item { SlotsBlock(state, launcher, openApp, layout) }
            if (state.tasks.isEmpty()) {
                item { NoTasks() }
            } else {
                items(state.tasks, itemId = { task -> task.id.hashCode().toLong() }) { task -> TaskChip(state, task) }
            }
            item { Spacer(GlanceModifier.height(CardPadding)) }
        }
    } else {
        Column(modifier = GlanceModifier.fillMaxSize().padding(horizontal = CardPadding)) {
            TopBlock(state, layout)
            SlotsBlock(state, launcher, openApp, layout)
            if (state.tasks.isEmpty()) NoTasks() else state.tasks.forEach { task -> TaskChip(state, task) }
        }
    }
}

@Composable
private fun TopBlock(state: TodayWidgetState.Content, layout: DayLayout) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Spacer(GlanceModifier.height(if (layout == DayLayout.Full) CardPadding else ShortTopPadding))
        Header(state, numberSize = if (layout == DayLayout.Full) 36 else 30, compact = layout == DayLayout.Narrow)
    }
}

@Composable
private fun SlotsBlock(state: TodayWidgetState.Content, launcher: WidgetLauncher, openApp: Action, layout: DayLayout) {
    val top = if (layout == DayLayout.Full) SlotPadding else CompactGap
    when (layout) {
        DayLayout.Full -> Row(modifier = GlanceModifier.fillMaxWidth().padding(top = top)) {
            MealSlot(MealType.Lunch, state.lunch, launcher, openApp, GlanceModifier.defaultWeight().height(SlotHeight))
            Spacer(GlanceModifier.width(Gap))
            MealSlot(MealType.Dinner, state.dinner, launcher, openApp, GlanceModifier.defaultWeight().height(SlotHeight))
        }

        DayLayout.Short -> Row(modifier = GlanceModifier.fillMaxWidth().padding(top = top)) {
            CompactMeal(MealType.Lunch, state.lunch, launcher, openApp, GlanceModifier.defaultWeight())
            Spacer(GlanceModifier.width(Gap))
            CompactMeal(MealType.Dinner, state.dinner, launcher, openApp, GlanceModifier.defaultWeight())
        }

        DayLayout.Narrow -> Column(modifier = GlanceModifier.fillMaxWidth().padding(top = top)) {
            CompactMeal(MealType.Lunch, state.lunch, launcher, openApp)
            Spacer(GlanceModifier.height(CompactGap))
            CompactMeal(MealType.Dinner, state.dinner, launcher, openApp)
        }
    }
}

/** The big day number, the weekday and the "today" tag, as at the top of the day card. */
@Composable
private fun Header(state: TodayWidgetState.Content, numberSize: Int, compact: Boolean) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = state.date.dayOfMonth.toString(),
            style = TextStyle(
                color = WidgetColors.onCard,
                fontSize = numberSize.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
            ),
        )
        Spacer(GlanceModifier.width(Gap))
        Column {
            Text(
                text = weekdayName(state.date, context.resources.configuration.locales[0], short = compact),
                style = TextStyle(color = WidgetColors.onCard, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
            Box(
                modifier = GlanceModifier
                    .background(WidgetColors.onCard)
                    .cornerRadius(TagCorner)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = context.getString(R.string.widget_today_badge),
                    style = TextStyle(color = WidgetColors.card, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * A slot of the day with its sun or moon, its name and what is planned: the recipe and its details, or the note of a quick meal. A
 * recipe opens when tapped; anything else opens the app. A slot with nothing planned is a faint hollow that says so.
 */
@Composable
private fun MealSlot(type: MealType, meal: PlannedMeal?, launcher: WidgetLauncher, openApp: Action, modifier: GlanceModifier) {
    val context = LocalContext.current
    val onClick = (meal as? PlannedMeal.FromRecipe)?.let { fromRecipe ->
        actionStartActivity(launcher.openRecipeIntent(fromRecipe.recipe.recipeId))
    } ?: openApp
    val ink = if (meal == null) WidgetColors.onCard else WidgetColors.onSlot
    Column(
        modifier = modifier
            .background(if (meal == null) WidgetColors.emptySlot else WidgetColors.slot)
            .cornerRadius(SlotCorner)
            .padding(SlotPadding)
            .clickable(onClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(type.badgeRes()),
                contentDescription = null,
                modifier = GlanceModifier.size(BadgeSize),
            )
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = context.getString(type.labelRes()),
                style = TextStyle(color = ink, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                maxLines = 1,
            )
        }
        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = meal?.title ?: context.getString(R.string.widget_nothing_planned),
            style = TextStyle(color = ink, fontSize = 14.sp, fontWeight = FontWeight.Bold),
            maxLines = 2,
        )
        mealDetail(meal) { minutes -> context.getString(R.string.widget_prep_time, minutes) }?.let { detail ->
            Text(text = detail, style = TextStyle(color = ink, fontSize = 11.sp), maxLines = 1)
        }
    }
}

/** A task as a chip, like in the app: a circle that is empty or ticked, and its text. Tapping it ticks or unticks it everywhere. */
@Composable
private fun TaskChip(state: TodayWidgetState.Content, task: DayTask) {
    val ink: ColorProvider = if (task.done) WidgetColors.onChipDone else WidgetColors.onChip
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Spacer(GlanceModifier.height(Gap))
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(if (task.done) WidgetColors.chipDone else WidgetColors.chip)
                .cornerRadius(ChipCorner)
                .padding(horizontal = 12.dp, vertical = 9.dp)
                .clickable(
                    actionRunCallback<ToggleTaskAction>(
                        actionParametersOf(TaskDateKey to state.date.toString(), TaskIdKey to task.id, TaskDoneKey to !task.done),
                    ),
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(if (task.done) R.drawable.ic_widget_task_done else R.drawable.ic_widget_task_open),
                contentDescription = null,
                colorFilter = ColorFilter.tint(ink),
                modifier = GlanceModifier.size(TaskIconSize),
            )
            Spacer(GlanceModifier.width(10.dp))
            Text(
                text = task.text,
                style = TextStyle(
                    color = ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null,
                ),
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun NoTasks() {
    val context = LocalContext.current
    Column(modifier = GlanceModifier.fillMaxWidth().padding(top = Gap)) {
        Text(
            text = context.getString(R.string.widget_no_tasks),
            style = TextStyle(color = WidgetColors.onCard, fontSize = 13.sp),
        )
    }
}

@Composable
private fun Message(@StringRes textRes: Int) {
    val context: Context = LocalContext.current
    Box(modifier = GlanceModifier.fillMaxSize().padding(CardPadding), contentAlignment = Alignment.Center) {
        Text(
            text = context.getString(textRes),
            style = TextStyle(color = WidgetColors.onCard, fontSize = 14.sp, fontWeight = FontWeight.Medium),
        )
    }
}

@StringRes
private fun MealType.labelRes(): Int = when (this) {
    MealType.Lunch -> R.string.widget_lunch
    MealType.Dinner -> R.string.widget_dinner
}

@DrawableRes
private fun MealType.badgeRes(): Int = when (this) {
    MealType.Lunch -> R.drawable.ic_widget_lunch
    MealType.Dinner -> R.drawable.ic_widget_dinner
}
