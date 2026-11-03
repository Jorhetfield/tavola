package com.tavola.core.designsystem.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

private const val BADGE_SIZE_STEPS = 2

/** A big round badge with an icon, the mark of a welcome or entry screen. It is decorative: the text next to it says what it is. */
@Composable
fun TavolaIconBadge(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(TavolaTheme.spacing.xxxl * BADGE_SIZE_STEPS)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(TavolaTheme.spacing.xxxl),
        )
    }
}

@TavolaPreviews
@Composable
private fun TavolaIconBadgePreview() {
    TavolaTheme { TavolaIconBadge(Icons.Rounded.RestaurantMenu) }
}
