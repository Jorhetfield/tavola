package com.tavola.screenshottests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

/** Paleta completa: cualquier cambio de color en el tema aparece como diferencia en esta captura. */
@PreviewTest
@TavolaPreviews
@Composable
fun PaletteScreenshot() {
    TavolaTheme {
        val scheme = MaterialTheme.colorScheme
        val extra = TavolaTheme.colors
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.lg),
        ) {
            Swatches(
                "primary" to scheme.primary,
                "primaryCont" to scheme.primaryContainer,
                "secondary" to scheme.secondary,
                "secondaryCont" to scheme.secondaryContainer,
                "tertiary" to scheme.tertiary,
                "tertiaryCont" to scheme.tertiaryContainer,
                "error" to scheme.error,
                "background" to scheme.background,
                "surface" to scheme.surface,
                "surfLowest" to scheme.surfaceContainerLowest,
                "surfLow" to scheme.surfaceContainerLow,
                "surfContainer" to scheme.surfaceContainer,
                "surfHigh" to scheme.surfaceContainerHigh,
                "surfHighest" to scheme.surfaceContainerHighest,
                "outline" to scheme.outline,
                "outlineVar" to scheme.outlineVariant,
            )
            Swatches(
                "dayCard" to extra.dayCard,
                "dayActive" to extra.dayCardActive,
                "dayMuted" to extra.dayCardMuted,
                "mealSlot" to extra.mealSlot,
                "lunch" to extra.lunchAccent,
                "dinner" to extra.dinnerAccent,
                "taskChip" to extra.taskChip,
                "taskDone" to extra.taskChipDone,
            )
            TypographySample()
        }
    }
}

@Composable
private fun Swatches(vararg items: Pair<String, Color>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
    ) {
        items.forEach { (name, color) ->
            Column(modifier = Modifier.width(SwatchWidth)) {
                Box(
                    modifier = Modifier
                        .size(width = SwatchWidth, height = SwatchHeight)
                        .background(color, RoundedCornerShape(TavolaTheme.spacing.md)),
                )
                Text(text = name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun TypographySample() {
    Column {
        Text("Display Small", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface)
        Text("Headline Medium", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Title Large", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Text("Title Medium", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Body Large", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Text("Label Large", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

private val SwatchWidth = 76.dp
private val SwatchHeight = 44.dp
