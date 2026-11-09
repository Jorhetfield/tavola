package com.tavola.feature.widget

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.CheckboxDefaults
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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal

private val WidgetCorner = 24.dp
private val BlockCorner = 16.dp
private val WidgetPadding = 12.dp
private val BlockPadding = 8.dp
private val Gap = 8.dp

/** The widget for [state], laid out for the size it has now: the next meal when it is small, the day when it is wider or taller. */
@Composable
internal fun TodayWidgetContent(state: TodayWidgetState, hourOfDay: Int, launcher: WidgetLauncher) {
    val openApp = actionStartActivity(launcher.openAppIntent())
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.background)
            .cornerRadius(WidgetCorner)
            .padding(WidgetPadding)
            .clickable(openApp),
    ) {
        when (state) {
            TodayWidgetState.SignedOut, TodayWidgetState.NoGroup -> Message(R.string.widget_open_app)
            TodayWidgetState.Unavailable -> Message(R.string.widget_unavailable)
            is TodayWidgetState.Content -> TodayLayout(state, hourOfDay, launcher, openApp)
        }
    }
}

@Composable
private fun TodayLayout(state: TodayWidgetState.Content, hourOfDay: Int, launcher: WidgetLauncher, openApp: Action) {
    val size = LocalSize.current
    when {
        size.height >= LargeWidgetSize.height && size.width >= MediumWidgetSize.width -> LargeLayout(state, launcher, openApp)
        size.width >= MediumWidgetSize.width -> MediumLayout(state, launcher, openApp)
        else -> SmallLayout(state, hourOfDay, launcher, openApp)
    }
}

/** 2x2: the date and the meal that comes next. */
@Composable
private fun SmallLayout(state: TodayWidgetState.Content, hourOfDay: Int, launcher: WidgetLauncher, openApp: Action) {
    val next = state.nextMeal(hourOfDay)
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(state)
        Spacer(GlanceModifier.height(Gap))
        MealBlock(next?.first ?: MealType.Lunch, next?.second, launcher, openApp, GlanceModifier.fillMaxWidth())
    }
}

/** 4x2: the lunch and the dinner on the left, the tasks on the right. */
@Composable
private fun MediumLayout(state: TodayWidgetState.Content, launcher: WidgetLauncher, openApp: Action) {
    Row(modifier = GlanceModifier.fillMaxSize()) {
        Column(modifier = GlanceModifier.defaultWeight().fillMaxSize()) {
            Header(state)
            Spacer(GlanceModifier.height(Gap))
            MealBlock(MealType.Lunch, state.lunch, launcher, openApp, GlanceModifier.fillMaxWidth())
            Spacer(GlanceModifier.height(Gap))
            MealBlock(MealType.Dinner, state.dinner, launcher, openApp, GlanceModifier.fillMaxWidth())
        }
        Spacer(GlanceModifier.width(WidgetPadding))
        Tasks(state, modifier = GlanceModifier.defaultWeight().fillMaxSize())
    }
}

/** 4x4: the lunch and the dinner side by side, and every task under them. */
@Composable
private fun LargeLayout(state: TodayWidgetState.Content, launcher: WidgetLauncher, openApp: Action) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(state)
        Spacer(GlanceModifier.height(Gap))
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            MealBlock(MealType.Lunch, state.lunch, launcher, openApp, GlanceModifier.defaultWeight())
            Spacer(GlanceModifier.width(Gap))
            MealBlock(MealType.Dinner, state.dinner, launcher, openApp, GlanceModifier.defaultWeight())
        }
        Spacer(GlanceModifier.height(Gap))
        Tasks(state, modifier = GlanceModifier.defaultWeight().fillMaxWidth())
    }
}

@Composable
private fun Header(state: TodayWidgetState.Content) {
    val context = LocalContext.current
    Text(
        text = context.getString(R.string.widget_today_header, formatWidgetDate(state.date, context.resources.configuration.locales[0])),
        style = TextStyle(color = GlanceTheme.colors.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold),
        maxLines = 1,
    )
}

/**
 * A slot of the day: its name and what is planned. Tapping a recipe meal opens the recipe; anything else opens the app. An empty
 * slot says so.
 */
@Composable
private fun MealBlock(type: MealType, meal: PlannedMeal?, launcher: WidgetLauncher, openApp: Action, modifier: GlanceModifier) {
    val context = LocalContext.current
    val onClick =
        (meal as? PlannedMeal.FromRecipe)?.let { fromRecipe -> actionStartActivity(launcher.openRecipeIntent(fromRecipe.recipe.recipeId)) }
            ?: openApp
    Column(
        modifier = modifier
            .background(GlanceTheme.colors.tertiaryContainer)
            .cornerRadius(BlockCorner)
            .padding(BlockPadding)
            .clickable(onClick),
    ) {
        Text(
            text = context.getString(type.labelRes()),
            style = TextStyle(color = GlanceTheme.colors.onTertiaryContainer, fontSize = 11.sp, fontWeight = FontWeight.Medium),
            maxLines = 1,
        )
        Text(
            text = meal?.title ?: context.getString(R.string.widget_nothing_planned),
            style = TextStyle(color = GlanceTheme.colors.onTertiaryContainer, fontSize = 14.sp, fontWeight = FontWeight.Bold),
            maxLines = 2,
        )
    }
}

/** The tasks of the day with a box each, which is ticked or unticked right from the widget. */
@Composable
private fun Tasks(state: TodayWidgetState.Content, modifier: GlanceModifier) {
    val context = LocalContext.current
    if (state.tasks.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = context.getString(R.string.widget_no_tasks),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 13.sp),
            )
        }
    } else {
        LazyColumn(modifier = modifier) {
            items(state.tasks, itemId = { task -> task.id.hashCode().toLong() }) { task -> TaskRow(state, task) }
        }
    }
}

@Composable
private fun TaskRow(state: TodayWidgetState.Content, task: DayTask) {
    CheckBox(
        checked = task.done,
        onCheckedChange = actionRunCallback<ToggleTaskAction>(
            actionParametersOf(TaskDateKey to state.date.toString(), TaskIdKey to task.id, TaskDoneKey to !task.done),
        ),
        text = task.text,
        style = TextStyle(
            color = if (task.done) GlanceTheme.colors.onSurfaceVariant else GlanceTheme.colors.onSurface,
            fontSize = 14.sp,
        ),
        colors = CheckboxDefaults.colors(
            checkedColor = GlanceTheme.colors.primary,
            uncheckedColor = GlanceTheme.colors.onSurfaceVariant,
        ),
        maxLines = 2,
    )
}

@Composable
private fun Message(@StringRes textRes: Int) {
    val context: Context = LocalContext.current
    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = context.getString(textRes),
            style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium),
        )
    }
}

private fun MealType.labelRes(): Int = when (this) {
    MealType.Lunch -> R.string.widget_lunch
    MealType.Dinner -> R.string.widget_dinner
}
