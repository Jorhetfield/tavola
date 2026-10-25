package com.tavola.core.designsystem.loading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

/**
 * Indicador de carga expresivo (la forma que se transforma). [contained] lo dibuja sobre una píldora de
 * color; [label] añade un texto debajo. Pensado para el estado `Loading` de los `UiState`.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaLoadingIndicator(modifier: Modifier = Modifier, label: String? = null, contained: Boolean = true) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md),
    ) {
        if (contained) {
            ContainedLoadingIndicator(
                containerColor = colors.primaryContainer,
                indicatorColor = colors.onPrimaryContainer,
            )
        } else {
            LoadingIndicator(color = colors.primary)
        }
        label?.let { labelText ->
            Text(text = labelText, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
    }
}

@TavolaPreviews
@Composable
private fun TavolaLoadingIndicatorPreview() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.lg),
        ) {
            TavolaLoadingIndicator(label = "Preparando tu semana…")
            TavolaLoadingIndicator(contained = false)
        }
    }
}
