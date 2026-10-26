package com.tavola.screenshottests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.calendar.TavolaDayCard
import com.tavola.core.designsystem.navigation.TavolaFloatingNavBar
import com.tavola.core.designsystem.preview.TavolaAdaptivePreviews
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.scaffold.TavolaScaffold
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.core.designsystem.topbar.TavolaTopBar
import com.tavola.core.designsystem.topbar.TavolaTopBarAction

@PreviewTest
@TavolaPreviews
@Composable
fun TopBarScreenshot() {
    TavolaTheme {
        TavolaTopBar(
            title = "Semana 43",
            subtitle = "19 – 25 de octubre",
            navigationAction = TavolaTopBarAction(Icons.AutoMirrored.Rounded.ArrowBack, R.string.topbar_back) {},
            actions = listOf(TavolaTopBarAction(Icons.Rounded.Settings, R.string.topbar_settings) {}),
        )
    }
}

@PreviewTest
@TavolaPreviews
@Composable
fun FloatingNavBarScreenshot() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.lg),
        ) {
            TavolaFloatingNavBar(items = ScreenshotSamples.navItems, selectedKey = "calendar", onItemClick = {})
            TavolaFloatingNavBar(items = ScreenshotSamples.navItems, selectedKey = "shopping", onItemClick = {})
        }
    }
}

/** Pantalla completa en móvil (light y dark), plegable y tablet: insets, barra flotante y ancho del contenido. */
@PreviewTest
@TavolaAdaptivePreviews
@Composable
fun CalendarScaffoldScreenshot() {
    TavolaTheme {
        TavolaScaffold(
            topBar = { TavolaTopBar(title = "Semana 43", subtitle = "19 – 25 de octubre") },
            floatingBar = {
                TavolaFloatingNavBar(items = ScreenshotSamples.navItems, selectedKey = "calendar", onItemClick = {})
            },
        ) { contentPadding ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.lg),
                contentPadding = PaddingValues(
                    start = TavolaTheme.spacing.lg,
                    end = TavolaTheme.spacing.lg,
                    top = contentPadding.calculateTopPadding() + TavolaTheme.spacing.lg,
                    bottom = contentPadding.calculateBottomPadding(),
                ),
            ) {
                items(ScreenshotSamples.days, key = { day -> day.id }) { day ->
                    TavolaDayCard(day, onMealClick = {}, onTaskCheckedChange = { _, _ -> }, onTaskRemoveClick = {}, onAddTaskClick = {})
                }
            }
        }
    }
}
