package com.tavola.core.designsystem.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

/** Semana mostrada. [label] llega ya formateado ("19 – 25 oct"); [isCurrentWeek] oculta el botón "Hoy". */
@Immutable
data class WeekSelectorModel(val label: String, val isCurrentWeek: Boolean)

/** Navegación entre semanas: flecha anterior, rango de la semana, botón "Hoy" (solo fuera de la actual) y flecha siguiente. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaWeekSelector(
    model: WeekSelectorModel,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = TavolaTheme.spacing
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        FilledTonalIconButton(onClick = onPreviousClick, shapes = IconButtonDefaults.shapes()) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = stringResource(R.string.week_selector_previous))
        }
        AnimatedContent(
            targetState = model.label,
            modifier = Modifier.weight(1f),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "weekLabel",
        ) { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        AnimatedVisibility(
            visible = !model.isCurrentWeek,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            FilledTonalButton(
                onClick = onTodayClick,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.padding(end = spacing.xs),
            ) {
                Text(stringResource(R.string.week_selector_today))
            }
        }
        FilledTonalIconButton(onClick = onNextClick, shapes = IconButtonDefaults.shapes()) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = stringResource(R.string.week_selector_next))
        }
    }
}

@TavolaPreviews
@Composable
private fun TavolaWeekSelectorPreview() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.lg),
        ) {
            TavolaWeekSelector(WeekSelectorModel("19 – 25 oct", isCurrentWeek = true), {}, {}, {})
            TavolaWeekSelector(WeekSelectorModel("26 oct – 1 nov", isCurrentWeek = false), {}, {}, {})
        }
    }
}
