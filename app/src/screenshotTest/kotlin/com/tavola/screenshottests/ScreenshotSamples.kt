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

/** Datos fijos para las capturas: si cambian, cambian las imágenes de referencia. */
internal object ScreenshotSamples {
    val days = listOf(
        DayCardModel(
            id = "mon",
            weekday = "Lunes",
            dayNumber = "19",
            state = DayState.Past,
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

    val navItems = listOf(
        TavolaNavItem("calendar", R.string.nav_calendar, Icons.Outlined.CalendarMonth, Icons.Rounded.CalendarMonth),
        TavolaNavItem("shopping", R.string.nav_shopping, Icons.Outlined.ShoppingCart, Icons.Rounded.ShoppingCart),
    )
}
