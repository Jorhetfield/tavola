package com.tavola.feature.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tavola.core.designsystem.choice.TavolaChoiceTile
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.ShoppingCategory

private const val COLUMNS = 3

/** Every category as a tile, [COLUMNS] to a row, with the [selected] one marked: one tap picks a category. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ShoppingCategoryGrid(selected: ShoppingCategory, onPick: (ShoppingCategory) -> Unit, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        maxItemsInEachRow = COLUMNS,
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
