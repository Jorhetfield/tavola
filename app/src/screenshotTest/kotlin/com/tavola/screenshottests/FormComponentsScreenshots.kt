package com.tavola.screenshottests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.tavola.core.designsystem.field.TavolaSearchField
import com.tavola.core.designsystem.field.TavolaTextField
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.recipe.RecipeRowModel
import com.tavola.core.designsystem.recipe.TavolaRecipeRow
import com.tavola.core.designsystem.theme.TavolaTheme

@PreviewTest
@TavolaPreviews
@Composable
fun FieldsScreenshot() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md),
        ) {
            TavolaSearchField(value = "", onValueChange = {
            }, placeholder = "Buscar receta o ingrediente", clearContentDescription = "Borrar")
            TavolaSearchField(value = "paella", onValueChange = {}, placeholder = "Buscar receta", clearContentDescription = "Borrar")
            TavolaTextField(value = "", onValueChange = {}, label = "¿Qué vais a comer?")
            TavolaTextField(value = "Sobras de ayer", onValueChange = {}, label = "¿Qué vais a comer?")
        }
    }
}

@PreviewTest
@TavolaPreviews
@Composable
fun RecipeRowsScreenshot() {
    TavolaTheme {
        Column(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        ) {
            TavolaRecipeRow(RecipeRowModel("1", "Paella valenciana", "50 min · Español", selected = true), onClick = {})
            TavolaRecipeRow(RecipeRowModel("2", "Moussaka", "70 min · Griego"), onClick = {})
            TavolaRecipeRow(RecipeRowModel("3", "Ensalada de tomate, feta y aceitunas kalamata con orégano"), onClick = {})
        }
    }
}
