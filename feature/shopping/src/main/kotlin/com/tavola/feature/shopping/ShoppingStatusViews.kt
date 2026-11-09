package com.tavola.feature.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.tavola.core.designsystem.badge.TavolaIconBadge
import com.tavola.core.designsystem.motion.popOnAppear
import com.tavola.core.designsystem.theme.TavolaTheme

/** Puts [content] in the middle of the space the scaffold leaves free (loading and error states). */
@Composable
internal fun CenteredBox(contentPadding: PaddingValues, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** What is shown below the add field while the list has no items: a basket that pops in, and a line that says what to do. */
@Composable
internal fun ShoppingEmpty(modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        TavolaIconBadge(Icons.Rounded.ShoppingBasket, modifier = Modifier.popOnAppear(delayMillis = 0))
        Text(
            text = stringResource(R.string.shopping_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.shopping_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** "The list could not be loaded" with a button to try again. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ShoppingError(onRetryClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.lg),
    ) {
        Text(
            text = stringResource(R.string.shopping_error),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetryClick, shapes = ButtonDefaults.shapes()) { Text(stringResource(R.string.shopping_retry)) }
    }
}
