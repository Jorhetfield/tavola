package com.tavola.feature.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/** Dimensiones temporales del showcase hasta que exista `TavolaSpacing` (Fase 1). */
internal object ShowcaseDimens {
    val Gutter = 16.dp
    val ItemGap = 12.dp
    val SectionGap = 8.dp
}

@Composable
internal fun ShowcaseSection(titleRes: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ShowcaseDimens.Gutter),
        verticalArrangement = Arrangement.spacedBy(ShowcaseDimens.SectionGap),
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        content()
    }
}
