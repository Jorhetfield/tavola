package com.tavola.feature.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tavola.feature.showcase.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CarouselSection(modifier: Modifier = Modifier) {
    val recipes = listOf(
        R.string.showcase_recipe_paella,
        R.string.showcase_recipe_moussaka,
        R.string.showcase_recipe_risotto,
        R.string.showcase_recipe_tzatziki,
        R.string.showcase_recipe_gazpacho,
        R.string.showcase_recipe_tiramisu,
    )
    val containers = with(MaterialTheme.colorScheme) {
        listOf(primaryContainer, secondaryContainer, tertiaryContainer)
    }
    val onContainers = with(MaterialTheme.colorScheme) {
        listOf(onPrimaryContainer, onSecondaryContainer, onTertiaryContainer)
    }

    ShowcaseSection(titleRes = R.string.showcase_carousel_title, modifier = modifier) {
        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState { recipes.size },
            preferredItemWidth = 186.dp,
            itemSpacing = ShowcaseDimens.SectionGap,
            modifier = Modifier.height(205.dp),
        ) { index ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .maskClip(MaterialTheme.shapes.extraLarge)
                    .background(containers[index % containers.size]),
                contentAlignment = Alignment.BottomStart,
            ) {
                Text(
                    text = stringResource(recipes[index]),
                    style = MaterialTheme.typography.titleMedium,
                    color = onContainers[index % onContainers.size],
                    maxLines = 2,
                    modifier = Modifier.padding(ShowcaseDimens.Gutter),
                )
            }
        }
    }
}
