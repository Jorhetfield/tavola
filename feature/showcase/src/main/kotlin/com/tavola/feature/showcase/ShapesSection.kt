package com.tavola.feature.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tavola.feature.showcase.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ShapesSection(modifier: Modifier = Modifier) {
    val shapes = listOf(
        MaterialShapes.Cookie9Sided,
        MaterialShapes.Clover4Leaf,
        MaterialShapes.Flower,
        MaterialShapes.Sunny,
        MaterialShapes.SoftBurst,
        MaterialShapes.Heart,
        MaterialShapes.Gem,
        MaterialShapes.Puffy,
    )
    val colors = with(MaterialTheme.colorScheme) {
        listOf(primary, secondary, tertiary, primaryContainer, secondaryContainer, tertiaryContainer)
    }

    ShowcaseSection(titleRes = R.string.showcase_shapes_title, modifier = modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(ShowcaseDimens.ItemGap),
            contentPadding = PaddingValues(end = ShowcaseDimens.Gutter),
        ) {
            items(shapes.size) { index ->
                Box(
                    Modifier
                        .size(88.dp)
                        .clip(shapes[index].toShape())
                        .background(colors[index % colors.size]),
                )
            }
        }
    }
}
