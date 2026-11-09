package com.tavola.feature.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import com.tavola.core.designsystem.choice.TavolaChoiceTile
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.ShoppingCategory

private const val COLUMNS = 3
private const val COLUMNS_WITH_LARGE_TEXT = 2
private const val LARGE_TEXT_SCALE = 1.3f

/**
 * Every category as a tile with the [selected] one marked: one tap picks a category. It is [COLUMNS] to a row, or fewer when the
 * text is large, so the names are never cut. The tiles are one group of choices for screen readers.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ShoppingCategoryGrid(selected: ShoppingCategory, onPick: (ShoppingCategory) -> Unit, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    val columns = if (LocalDensity.current.fontScale > LARGE_TEXT_SCALE) COLUMNS_WITH_LARGE_TEXT else COLUMNS
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        maxItemsInEachRow = columns,
    ) {
        ShoppingCategory.entries.forEach { category ->
            TavolaChoiceTile(
                label = stringResource(category.labelRes),
                icon = category.icon,
                selected = category == selected,
                onClick = { onPick(category) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
