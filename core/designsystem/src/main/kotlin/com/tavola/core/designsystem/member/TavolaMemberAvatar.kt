package com.tavola.core.designsystem.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

/**
 * A round avatar for a person of a group: the first letter of their name on their color. [colorIndex] is the position in the
 * member palette and wraps around it. It is decorative (the name is next to it), so it says nothing to screen readers.
 */
@Composable
fun TavolaMemberAvatar(name: String?, colorIndex: Int, modifier: Modifier = Modifier) {
    val colors = TavolaTheme.colors.memberColors
    val color = colors[colorIndex.mod(colors.size)]
    Box(
        modifier = modifier
            .size(TavolaTheme.spacing.xxxl)
            .clip(CircleShape)
            .background(color.container),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name?.trim()?.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            color = color.content,
        )
    }
}

@TavolaPreviews
@Composable
private fun TavolaMemberAvatarPreview() {
    TavolaTheme {
        FlowRow(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        ) {
            listOf("Marta", "Luis", "Ana", "Iván", "Sofía", "Pablo", "Rosa", "Tomás", null).forEachIndexed { index, name ->
                TavolaMemberAvatar(name = name, colorIndex = index)
            }
        }
    }
}
