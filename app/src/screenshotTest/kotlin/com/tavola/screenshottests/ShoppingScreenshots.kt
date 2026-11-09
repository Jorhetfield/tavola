package com.tavola.screenshottests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.SetMeal
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.tavola.core.designsystem.choice.TavolaChoiceTile
import com.tavola.core.designsystem.preview.TavolaAdaptivePreviews
import com.tavola.core.designsystem.preview.TavolaFontScalePreviews
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.preview.TavolaScreenPreviews
import com.tavola.core.designsystem.shopping.ShoppingRowModel
import com.tavola.core.designsystem.shopping.TavolaShoppingCategoryHeader
import com.tavola.core.designsystem.shopping.TavolaShoppingRow
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.ShoppingCandidate
import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingItem
import com.tavola.feature.shopping.AddItemDraft
import com.tavola.feature.shopping.AddToShoppingActions
import com.tavola.feature.shopping.AddToShoppingContent
import com.tavola.feature.shopping.AddToShoppingRow
import com.tavola.feature.shopping.AddToShoppingUiState
import com.tavola.feature.shopping.ShoppingActions
import com.tavola.feature.shopping.ShoppingAddSheetContent
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
    TavolaTheme { ShoppingContent(state, ShoppingActions(), addSheetOpen = false) }
}

/** The shopping list in phone (light and dark), foldable and tablet. */
@PreviewTest
@TavolaAdaptivePreviews
@Composable
fun ShoppingScreenshot() = Shopping(ShoppingUiState.Success(shoppingSections))

@PreviewTest
@TavolaScreenPreviews
@Composable
fun ShoppingEmptyScreenshot() = Shopping(ShoppingUiState.Success(emptyList()))

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

/** The sheet content on a surface with the shape of the sheet (the real sheet is a window and is not captured). */
@Composable
private fun SheetSurface(sheetHeight: Dp = 720.dp, content: @Composable () -> Unit) {
    TavolaTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.extraLarge.copy(bottomStart = CornerSize(0f), bottomEnd = CornerSize(0f)),
        ) {
            content()
        }
    }
}

@Composable
private fun AddSheet(draft: AddItemDraft, categoriesOpen: Boolean = false, sheetHeight: Dp = 720.dp) {
    SheetSurface(sheetHeight) {
        ShoppingAddSheetContent(draft, ShoppingActions(), onDone = {}, startWithCategoriesOpen = categoriesOpen)
    }
}

/** The sheet with a name whose category is guessed. */
@PreviewTest
@TavolaPreviews
@Composable
fun ShoppingAddSheetScreenshot() = AddSheet(AddItemDraft(name = "Leche", quantity = "2 l"))

/** The sheet with the grid of categories open and one picked by hand. */
@PreviewTest
@TavolaPreviews
@Composable
fun ShoppingAddSheetCategoriesScreenshot() =
    AddSheet(AddItemDraft(name = "Detergente", pickedCategory = ShoppingCategory.Household), categoriesOpen = true)

/** The sheet right after adding an item, ready for the next one. */
@PreviewTest
@TavolaPreviews
@Composable
fun ShoppingAddSheetAddedScreenshot() = AddSheet(AddItemDraft(addedName = "Leche"))

@PreviewTest
@TavolaFontScalePreviews
@Composable
fun ShoppingAddSheetFontScaleScreenshot() = AddSheet(AddItemDraft(name = "Leche", quantity = "2 l"))

@PreviewTest
@TavolaPreviews
@Composable
fun ShoppingComponentsScreenshot() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm)) {
                TavolaChoiceTile("Frutas y verduras", Icons.Rounded.Eco, selected = true, onClick = {}, modifier = Modifier.weight(1f))
                TavolaChoiceTile("Carne y pescado", Icons.Rounded.SetMeal, selected = false, onClick = {}, modifier = Modifier.weight(1f))
                TavolaChoiceTile("Despensa", Icons.Rounded.Inventory2, selected = false, onClick = {}, modifier = Modifier.weight(1f))
            }
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

/** The grid of categories at large text, where it goes down to two columns so that no name is cut. */
@PreviewTest
@TavolaFontScalePreviews
@Composable
fun ShoppingAddSheetCategoriesFontScaleScreenshot() =
    AddSheet(AddItemDraft(name = "Detergente", pickedCategory = ShoppingCategory.Household), categoriesOpen = true, sheetHeight = 1600.dp)

private val offered = listOf(
    ShoppingCandidate("Arroz bomba", "400 g"),
    ShoppingCandidate("Caldo de pollo", "1,2 l"),
    ShoppingCandidate("Pollo troceado", "1 kg"),
    ShoppingCandidate("Azafrán", "1 pizca"),
    ShoppingCandidate("Aceite de oliva", "4 cdas"),
    ShoppingCandidate("Sal"),
)

private fun pickRows(vararg unpicked: Int, onList: Set<Int> = setOf(4)): AddToShoppingUiState.Success = AddToShoppingUiState.Success(
    offered.mapIndexed { index, candidate ->
        AddToShoppingRow(index, candidate, onList = index in onList, selected = index !in onList && index !in unpicked.toSet())
    },
)

@Composable
private fun AddToShopping(state: AddToShoppingUiState) {
    SheetSurface { AddToShoppingContent(state, AddToShoppingActions()) }
}

/** The sheet that adds the ingredients of a recipe: everything picked but what is already on the list. */
@PreviewTest
@TavolaPreviews
@Composable
fun AddToShoppingScreenshot() = AddToShopping(pickRows())

/** Some ingredients unpicked, as when the person already has them at home. */
@PreviewTest
@TavolaPreviews
@Composable
fun AddToShoppingUnpickedScreenshot() = AddToShopping(pickRows(1, 5))

@PreviewTest
@TavolaFontScalePreviews
@Composable
fun AddToShoppingFontScaleScreenshot() = AddToShopping(pickRows(1))

@PreviewTest
@TavolaPreviews
@Composable
fun AddToShoppingErrorScreenshot() = AddToShopping(AddToShoppingUiState.Error)
