package com.tavola.core.designsystem.snackbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.R
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

private val CountdownSize = 32.dp
private val CountdownStrokeWidth = 3.dp
private val CountdownWavelength = 12.dp
private const val SHORT_MILLIS = 4_000L
private const val LONG_MILLIS = 10_000L
private const val TRACK_ALPHA = 0.24f

/** Host that draws every snackbar of the app as a [TavolaSnackbar]. Use it as the `snackbarHost` of `TavolaScaffold`. */
@Composable
fun TavolaSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data -> TavolaSnackbar(data) }
}

/**
 * A snackbar with a countdown: a wavy ring that empties as the time runs out, so the user can see how long Undo is
 * still available. A snackbar that never times out ([SnackbarDuration.Indefinite]) shows no ring.
 */
@Composable
fun TavolaSnackbar(data: SnackbarData, modifier: Modifier = Modifier) {
    val visuals = data.visuals
    val durationMillis = visuals.duration.millisOrNull()
    val remaining = remember(data) { Animatable(1f) }
    LaunchedEffect(data) {
        // The countdown mirrors the host's own timeout in real time, so it is a linear tween and not a motion-scheme spring.
        if (durationMillis != null) remaining.animateTo(0f, tween(durationMillis.toInt(), easing = LinearEasing))
    }
    TavolaSnackbarContent(
        message = visuals.message,
        actionLabel = visuals.actionLabel,
        remaining = if (durationMillis != null) ({ remaining.value }) else null,
        onActionClick = data::performAction,
        onDismissClick = if (visuals.withDismissAction) data::dismiss else null,
        modifier = modifier,
    )
}

/** Stateless snackbar: [remaining] is the fraction of the time left (1 to 0), or `null` for no countdown. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaSnackbarContent(
    message: String,
    actionLabel: String?,
    remaining: (() -> Float)?,
    onActionClick: () -> Unit,
    onDismissClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val countdownStroke = with(LocalDensity.current) { Stroke(width = CountdownStrokeWidth.toPx(), cap = StrokeCap.Round) }
    val scheme = MaterialTheme.colorScheme
    Snackbar(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        action = actionLabel?.let { label ->
            { TextButton(onClick = onActionClick) { Text(text = label, color = scheme.inversePrimary) } }
        },
        dismissAction = onDismissClick?.let { dismiss ->
            {
                IconButton(onClick = dismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.snackbar_dismiss))
                }
            }
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md)) {
            if (remaining != null) {
                CircularWavyProgressIndicator(
                    progress = remaining,
                    modifier = Modifier.size(CountdownSize),
                    stroke = countdownStroke,
                    trackStroke = countdownStroke,
                    wavelength = CountdownWavelength,
                    color = scheme.inversePrimary,
                    trackColor = scheme.inverseOnSurface.copy(alpha = TRACK_ALPHA),
                )
            }
            Text(text = message)
        }
    }
}

/** How long the host keeps a snackbar of this duration, or `null` if it stays until it is dismissed. */
private fun SnackbarDuration.millisOrNull(): Long? = when (this) {
    SnackbarDuration.Short -> SHORT_MILLIS
    SnackbarDuration.Long -> LONG_MILLIS
    SnackbarDuration.Indefinite -> null
}

@TavolaPreviews
@Composable
private fun TavolaSnackbarPreview() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.lg),
        ) {
            TavolaSnackbarContent(
                message = "Receta eliminada",
                actionLabel = "Deshacer",
                remaining = { 0.6f },
                onActionClick = {},
                onDismissClick = {},
            )
            TavolaSnackbarContent(
                message = "No se ha podido guardar la receta",
                actionLabel = null,
                remaining = { 0.25f },
                onActionClick = {},
                onDismissClick = null,
            )
        }
    }
}
