package com.tavola.core.designsystem.topbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

/**
 * Barra superior grande y colapsable. Titular en serif, subtítulo opcional y acciones como
 * botones tonales redondeados. Pasa un [scrollBehavior] (`exitUntilCollapsedScrollBehavior`) para colapsarla.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationAction: TavolaTopBarAction? = null,
    actions: List<TavolaTopBarAction> = emptyList(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val spacing = TavolaTheme.spacing
    LargeFlexibleTopAppBar(
        title = { Text(text = title, style = MaterialTheme.typography.headlineLarge) },
        modifier = modifier,
        subtitle = subtitle?.let { text ->
            { AnimatedSubtitle(text) }
        },
        navigationIcon = {
            if (navigationAction != null) {
                TopBarIconButton(navigationAction, Modifier.padding(start = spacing.xs))
            }
        },
        actions = {
            Row {
                actions.forEach { action -> TopBarIconButton(action, Modifier.padding(end = spacing.sm)) }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            subtitleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        scrollBehavior = scrollBehavior,
    )
}

/** The subtitle changes with a vertical slide (the old text leaves upwards, the new one comes from below). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AnimatedSubtitle(text: String) {
    val motion = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            (
                slideInVertically(motion.defaultSpatialSpec<IntOffset>()) { height ->
                    height
                } + fadeIn(motion.defaultEffectsSpec())
                ) togetherWith
                (slideOutVertically(motion.fastSpatialSpec<IntOffset>()) { height -> -height } + fadeOut(motion.fastEffectsSpec()))
        },
        label = "topBarSubtitle",
    ) { subtitle ->
        Text(text = subtitle, style = MaterialTheme.typography.titleSmall)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopBarIconButton(action: TavolaTopBarAction, modifier: Modifier = Modifier) {
    FilledTonalIconButton(
        onClick = action.onClick,
        modifier = modifier,
        shapes = IconButtonDefaults.shapes(),
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = stringResource(action.contentDescriptionRes),
        )
    }
}

@TavolaPreviews
@Composable
private fun TavolaTopBarPreview() {
    TavolaTheme {
        TavolaTopBar(
            title = "Semana 43",
            subtitle = "20 – 26 de octubre",
            navigationAction = TavolaTopBarAction(
                Icons.AutoMirrored.Rounded.ArrowBack,
                R.string.topbar_back,
            ) {},
            actions = listOf(
                TavolaTopBarAction(Icons.Rounded.CalendarMonth, R.string.topbar_today) {},
                TavolaTopBarAction(Icons.Rounded.Settings, R.string.topbar_settings) {},
            ),
        )
    }
}
