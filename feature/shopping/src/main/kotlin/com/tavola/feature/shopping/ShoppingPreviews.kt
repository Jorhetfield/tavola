package com.tavola.feature.shopping

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tavola.core.designsystem.preview.TavolaPreviews
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
    TavolaTheme { ShoppingContent(ShoppingUiState.Success(previewItems.groupedByCategory()), ShoppingActions(), addSheetOpen = false) }
}

@TavolaScreenPreviews
@Composable
private fun ShoppingContentEmptyPreview() {
    TavolaTheme { ShoppingContent(ShoppingUiState.Success(emptyList()), ShoppingActions(), addSheetOpen = false) }
}

@TavolaPreviews
@Composable
private fun ShoppingAddSheetPreview() {
    TavolaTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.extraLarge.copy(bottomStart = CornerSize(0f), bottomEnd = CornerSize(0f)),
        ) {
            ShoppingAddSheetContent(AddItemDraft(name = "Leche", quantity = "2 l"), ShoppingActions(), onDone = {})
        }
    }
}
