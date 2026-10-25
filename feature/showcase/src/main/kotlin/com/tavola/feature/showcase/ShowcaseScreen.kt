package com.tavola.feature.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.tavola.core.designsystem.R as DesignSystemR
import com.tavola.core.designsystem.navigation.TavolaFloatingNavBar
import com.tavola.core.designsystem.navigation.TavolaNavItem
import com.tavola.core.designsystem.preview.TavolaScreenPreviews
import com.tavola.core.designsystem.scaffold.TavolaScaffold
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.core.designsystem.topbar.TavolaTopBar
import com.tavola.core.designsystem.topbar.TavolaTopBarAction
import com.tavola.feature.showcase.R

private val NavItems = listOf(
    TavolaNavItem("calendar", DesignSystemR.string.nav_calendar, Icons.Outlined.CalendarMonth, Icons.Rounded.CalendarMonth),
    TavolaNavItem("recipes", DesignSystemR.string.nav_recipes, Icons.Outlined.MenuBook, Icons.Rounded.MenuBook),
    TavolaNavItem("shopping", DesignSystemR.string.nav_shopping, Icons.Outlined.ShoppingCart, Icons.Rounded.ShoppingCart),
)

/** Pantalla temporal para ver el tema y los componentes de Material 3 Expressive. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShowcaseScreen(modifier: Modifier = Modifier) {
    val topBarScroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var selectedKey by remember { mutableStateOf(NavItems.first().key) }

    TavolaScaffold(
        modifier = modifier.nestedScroll(topBarScroll.nestedScrollConnection),
        topBar = {
            TavolaTopBar(
                title = stringResource(R.string.showcase_title),
                subtitle = stringResource(R.string.showcase_subtitle),
                actions = listOf(
                    TavolaTopBarAction(Icons.Rounded.Settings, DesignSystemR.string.topbar_settings) {},
                ),
                scrollBehavior = topBarScroll,
            )
        },
        floatingBar = {
            TavolaFloatingNavBar(
                items = NavItems,
                selectedKey = selectedKey,
                onItemClick = { selectedKey = it },
            )
        },
    ) { contentPadding ->
        ShowcaseContent(contentPadding)
    }
}

@Composable
private fun ShowcaseContent(contentPadding: PaddingValues) {
    val spacing = TavolaTheme.spacing
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(spacing.xl),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + spacing.lg,
            bottom = contentPadding.calculateBottomPadding(),
        ),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { CalendarSection() }
        item { ButtonsSection() }
        item { ChipsSection() }
        item { IndicatorsSection() }
        item { ShapesSection() }
        item { CarouselSection() }
    }
}

@TavolaScreenPreviews
@Composable
private fun ShowcaseScreenPreview() {
    TavolaTheme { ShowcaseScreen() }
}
