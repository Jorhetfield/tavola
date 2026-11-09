package com.tavola.feature.shopping

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.BakeryDining
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.EggAlt
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.SetMeal
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.ui.graphics.vector.ImageVector
import com.tavola.domain.model.ShoppingCategory

/** The icon of a category, in its header and in the chip that picks it. */
internal val ShoppingCategory.icon: ImageVector
    get() = when (this) {
        ShoppingCategory.FruitAndVegetables -> Icons.Rounded.Eco
        ShoppingCategory.MeatAndFish -> Icons.Rounded.SetMeal
        ShoppingCategory.DairyAndEggs -> Icons.Rounded.EggAlt
        ShoppingCategory.Pantry -> Icons.Rounded.Inventory2
        ShoppingCategory.Bakery -> Icons.Rounded.BakeryDining
        ShoppingCategory.Frozen -> Icons.Rounded.AcUnit
        ShoppingCategory.Drinks -> Icons.Rounded.LocalDrink
        ShoppingCategory.Household -> Icons.Rounded.CleaningServices
        ShoppingCategory.Other -> Icons.Rounded.ShoppingBasket
    }

/** The name of a category as the person reads it. */
@get:StringRes
internal val ShoppingCategory.labelRes: Int
    get() = when (this) {
        ShoppingCategory.FruitAndVegetables -> R.string.shopping_category_fruit_and_vegetables
        ShoppingCategory.MeatAndFish -> R.string.shopping_category_meat_and_fish
        ShoppingCategory.DairyAndEggs -> R.string.shopping_category_dairy_and_eggs
        ShoppingCategory.Pantry -> R.string.shopping_category_pantry
        ShoppingCategory.Bakery -> R.string.shopping_category_bakery
        ShoppingCategory.Frozen -> R.string.shopping_category_frozen
        ShoppingCategory.Drinks -> R.string.shopping_category_drinks
        ShoppingCategory.Household -> R.string.shopping_category_household
        ShoppingCategory.Other -> R.string.shopping_category_other
    }
