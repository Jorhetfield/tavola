package com.tavola.screenshottests

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ShoppingCart
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.calendar.DayCardModel
import com.tavola.core.designsystem.calendar.DayState
import com.tavola.core.designsystem.calendar.MealSlotModel
import com.tavola.core.designsystem.calendar.MealType
import com.tavola.core.designsystem.calendar.TaskChipModel
import com.tavola.core.designsystem.navigation.TavolaNavItem

/** Fixed data for the screenshots: if it changes, the reference images change. */
internal object ScreenshotSamples {
    val days = listOf(
        DayCardModel(
            id = "mon",
            weekday = "Lunes",
            dayNumber = "19",
            state = DayState.Past,
            monthShort = "oct",
            lunch = MealSlotModel(MealType.Lunch, "Gazpacho", "15 min · Español"),
            dinner = MealSlotModel(MealType.Dinner, "Tortilla de patatas", "30 min · Español"),
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
            tasks = listOf(TaskChipModel("t3", "Preparar la masa")),
        ),
        DayCardModel(id = "thu", weekday = "Jueves", dayNumber = "22", state = DayState.Upcoming),
    )

    /** Past days as they start: collapsed. One has tasks left to do, one has nothing planned, one has a long name. */
    val collapsedDays = listOf(
        DayCardModel(
            id = "mon",
            weekday = "Lunes",
            dayNumber = "19",
            state = DayState.Past,
            monthShort = "oct",
            expanded = false,
            lunch = MealSlotModel(MealType.Lunch, "Gazpacho", "15 min · Español"),
            dinner = MealSlotModel(MealType.Dinner, "Tortilla de patatas", "30 min · Español"),
            tasks = listOf(TaskChipModel("a", "Comprar huevos"), TaskChipModel("b", "Sacar la basura", done = true)),
        ),
        DayCardModel(
            id = "sun",
            weekday = "Domingo",
            dayNumber = "18",
            state = DayState.Past,
            monthShort = "oct",
            expanded = false,
            lunch = MealSlotModel(MealType.Lunch),
            dinner = MealSlotModel(MealType.Dinner, "Comemos fuera"),
        ),
        DayCardModel(
            id = "sat",
            weekday = "Sábado",
            dayNumber = "17",
            state = DayState.Past,
            monthShort = "oct",
            expanded = false,
            lunch = MealSlotModel(MealType.Lunch, "Ensalada de tomate, feta y aceitunas kalamata con orégano"),
            dinner = MealSlotModel(MealType.Dinner, "Pizza margarita", "45 min · Italiano"),
            tasks = listOf(
                TaskChipModel("c", "Comprar harina"),
                TaskChipModel("d", "Preparar la masa"),
                TaskChipModel("e", "Sacar el vino"),
            ),
        ),
    )

    val navItems = listOf(
        TavolaNavItem("calendar", R.string.nav_calendar, Icons.Outlined.CalendarMonth, Icons.Rounded.CalendarMonth),
        TavolaNavItem("shopping", R.string.nav_shopping, Icons.Outlined.ShoppingCart, Icons.Rounded.ShoppingCart),
    )
}
