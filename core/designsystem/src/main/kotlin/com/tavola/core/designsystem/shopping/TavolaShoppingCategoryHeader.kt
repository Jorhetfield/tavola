package com.tavola.core.designsystem.shopping

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.SetMeal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

private val BadgeSize = 32.dp
private val BadgeIconSize = 18.dp

/** The title of a group of items of the shopping list: a round badge with the [icon] of the category, its [label] and how many items [count] it has. */
@Composable
fun TavolaShoppingCategoryHeader(label: String, icon: ImageVector, count: Int, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .padding(horizontal = spacing.xs, vertical = spacing.xs)
            .semantics(mergeDescendants = true) { heading() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(BadgeSize)
                .clip(CircleShape)
                .background(scheme.tertiaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = scheme.onTertiaryContainer, modifier = Modifier.size(BadgeIconSize))
        }
        Text(text = label, style = MaterialTheme.typography.titleMedium, color = scheme.onSurface, modifier = Modifier.weight(1f))
        Text(text = count.toString(), style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
    }
}

@TavolaPreviews
@Composable
private fun TavolaShoppingCategoryHeaderPreview() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        ) {
            TavolaShoppingCategoryHeader("Frutas y verduras", Icons.Rounded.Eco, count = 4)
            TavolaShoppingCategoryHeader("Carne y pescado", Icons.Rounded.SetMeal, count = 1)
        }
    }
}
