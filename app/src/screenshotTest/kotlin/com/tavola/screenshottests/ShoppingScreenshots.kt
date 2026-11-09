package com.tavola.screenshottests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.SetMeal
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.tavola.core.designsystem.field.TavolaQuickAddField
import com.tavola.core.designsystem.preview.TavolaAdaptivePreviews
import com.tavola.core.designsystem.preview.TavolaFontScalePreviews
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.preview.TavolaScreenPreviews
import com.tavola.core.designsystem.shopping.ShoppingRowModel
import com.tavola.core.designsystem.shopping.TavolaShoppingCategoryHeader
import com.tavola.core.designsystem.shopping.TavolaShoppingRow
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingItem
import com.tavola.feature.shopping.ShoppingActions
import com.tavola.feature.shopping.ShoppingContent
import com.tavola.feature.shopping.ShoppingSection
import com.tavola.feature.shopping.ShoppingUiState

private val shoppingSections = listOf(
    ShoppingSection(
        ShoppingCategory.FruitAndVegetables,
        listOf(
            ShoppingItem("tomatoes", "Tomates", ShoppingCategory.FruitAndVegetables, "1 kg"),
            ShoppingItem("peppers", "Pimientos rojos", ShoppingCategory.FruitAndVegetables, checked = true),
        ),
    ),
    ShoppingSection(ShoppingCategory.MeatAndFish, listOf(ShoppingItem("hake", "Merluza", ShoppingCategory.MeatAndFish, "500 g"))),
    ShoppingSection(ShoppingCategory.DairyAndEggs, listOf(ShoppingItem("feta", "Queso feta", ShoppingCategory.DairyAndEggs))),
    ShoppingSection(
        ShoppingCategory.Pantry,
        listOf(
            ShoppingItem("oil", "Aceite de oliva virgen extra", ShoppingCategory.Pantry, "1 l"),
            ShoppingItem("rice", "Arroz bomba", ShoppingCategory.Pantry, checked = true),
        ),
    ),
)

@Composable
private fun Shopping(state: ShoppingUiState) {
    TavolaTheme { ShoppingContent(state, ShoppingActions()) }
}

/** The shopping list in phone (light and dark), foldable and tablet. */
@PreviewTest
@TavolaAdaptivePreviews
@Composable
fun ShoppingScreenshot() = Shopping(ShoppingUiState.Success(shoppingSections, draftCategory = ShoppingCategory.Pantry))

@PreviewTest
@TavolaScreenPreviews
@Composable
fun ShoppingEmptyScreenshot() = Shopping(ShoppingUiState.Success(emptyList(), draftName = "Leche"))

@PreviewTest
@TavolaScreenPreviews
@Composable
fun ShoppingAllDoneScreenshot() = Shopping(
    ShoppingUiState.Success(listOf(ShoppingSection(ShoppingCategory.Pantry, listOf(ShoppingItem("rice", "Arroz bomba", checked = true))))),
)

@PreviewTest
@TavolaScreenPreviews
@Composable
fun ShoppingErrorScreenshot() = Shopping(ShoppingUiState.Error)

/** Large text, where names and quantities have to wrap instead of being cut. */
@PreviewTest
@TavolaFontScalePreviews
@Composable
fun ShoppingFontScaleScreenshot() = Shopping(ShoppingUiState.Success(shoppingSections))

@PreviewTest
@TavolaPreviews
@Composable
fun ShoppingComponentsScreenshot() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        ) {
            TavolaQuickAddField(value = "", onValueChange = {
            }, placeholder = "Añadir a la lista", addContentDescription = "Añadir", onAdd = {})
            TavolaQuickAddField(value = "Leche", onValueChange = {
            }, placeholder = "Añadir a la lista", addContentDescription = "Añadir", onAdd = {})
            TavolaShoppingCategoryHeader("Frutas y verduras", Icons.Rounded.Eco, count = 4)
            TavolaShoppingRow(ShoppingRowModel("1", "Tomates", "1 kg"), onCheckedChange = {})
            TavolaShoppingRow(ShoppingRowModel("2", "Aceite de oliva virgen extra", checked = true), onCheckedChange = {})
            TavolaShoppingCategoryHeader("Carne y pescado", Icons.Rounded.SetMeal, count = 1)
            TavolaShoppingRow(
                ShoppingRowModel(
                    "3",
                    "Queso feta, aceitunas kalamata y orégano para la ensalada griega",
                    "200 g",
                ),
                onCheckedChange = {
                },
            )
        }
    }
}
