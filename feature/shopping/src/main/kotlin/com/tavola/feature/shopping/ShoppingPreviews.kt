package com.tavola.feature.shopping

import androidx.compose.runtime.Composable
import com.tavola.core.designsystem.preview.TavolaScreenPreviews
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingItem

private val previewItems = listOf(
    ShoppingItem("tomatoes", "Tomates", ShoppingCategory.FruitAndVegetables, "1 kg", createdAt = 1),
    ShoppingItem("peppers", "Pimientos rojos", ShoppingCategory.FruitAndVegetables, checked = true, createdAt = 2),
    ShoppingItem("hake", "Merluza", ShoppingCategory.MeatAndFish, "500 g", createdAt = 3),
    ShoppingItem("feta", "Queso feta", ShoppingCategory.DairyAndEggs, createdAt = 4),
    ShoppingItem("oil", "Aceite de oliva virgen extra", ShoppingCategory.Pantry, "1 l", createdAt = 5),
)

@TavolaScreenPreviews
@Composable
private fun ShoppingContentPreview() {
    TavolaTheme { ShoppingContent(ShoppingUiState.Success(previewItems.groupedByCategory()), ShoppingActions()) }
}

@TavolaScreenPreviews
@Composable
private fun ShoppingContentEmptyPreview() {
    TavolaTheme { ShoppingContent(ShoppingUiState.Success(emptyList(), draftName = "Leche"), ShoppingActions()) }
}
