package com.tavola.feature.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

/** Debug tools: fill the app with sample data to develop against, or wipe it to see the empty states. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SampleDataSection(state: SampleDataUiState, onLoadClick: () -> Unit, onClearClick: () -> Unit, modifier: Modifier = Modifier) {
    ShowcaseSection(titleRes = R.string.showcase_sample_data_title, modifier = modifier) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(ShowcaseDimens.ItemGap),
            verticalArrangement = Arrangement.spacedBy(ShowcaseDimens.ItemGap),
        ) {
            FilledTonalButton(onClick = onLoadClick, enabled = !state.isWorking, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(R.string.showcase_sample_data_load))
            }
            OutlinedButton(onClick = onClearClick, enabled = !state.isWorking, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(R.string.showcase_sample_data_clear))
            }
        }
        state.lastResult?.let { result ->
            Text(
                text = stringResource(result.messageRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun SampleDataResult.messageRes(): Int = when (this) {
    SampleDataResult.Loaded -> R.string.showcase_sample_data_loaded
    SampleDataResult.Cleared -> R.string.showcase_sample_data_cleared
    SampleDataResult.Failed -> R.string.showcase_sample_data_failed
}

@TavolaPreviews
@Composable
private fun SampleDataSectionPreview() {
    TavolaTheme {
        SampleDataSection(
            state = SampleDataUiState(lastResult = SampleDataResult.Loaded),
            onLoadClick = {},
            onClearClick = {},
        )
    }
}
