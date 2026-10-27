package com.tavola.feature.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.tavola.core.designsystem.recipe.RecipeRowModel
import com.tavola.core.designsystem.recipe.TavolaRecipeRow
import com.tavola.core.designsystem.theme.TavolaTheme
import kotlinx.coroutines.delay

/** How long a swiped row may stay out of view waiting for its recipe to leave the list before it slides back. */
private const val REMOVAL_GRACE_MILLIS = 1_500L

/**
 * A recipe row that is deleted by swiping it to the start. The same action is offered to accessibility services, which
 * cannot swipe. The row normally leaves the list as soon as the recipe is deleted; if it is still there after
 * [REMOVAL_GRACE_MILLIS] (the delete failed) it slides back.
 *
 * The swipe state is saved per list key, so when Undo brings the recipe back the new row would otherwise be restored
 * as "already swiped" (a red background with the bin and nothing else). A row that comes up swiped without the user
 * having swiped it is put back in place straight away.
 */
@Composable
internal fun SwipeToRemoveRecipeRow(row: RecipeRowModel, onClick: () -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    val removeLabel = stringResource(R.string.recipe_list_remove)
    val swipedByUser = remember { booleanArrayOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            val swipedToRemove = value == SwipeToDismissBoxValue.EndToStart
            if (swipedToRemove) {
                swipedByUser[0] = true
                onRemove()
            }
            swipedToRemove
        },
    )
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            if (swipedByUser[0]) {
                delay(REMOVAL_GRACE_MILLIS)
                dismissState.reset()
            } else {
                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }
        }
    }
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.semantics {
            customActions = listOf(
                CustomAccessibilityAction(removeLabel) {
                    onRemove()
                    true
                },
            )
        },
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.large)
                    .padding(horizontal = TavolaTheme.spacing.xl),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        TavolaRecipeRow(model = row, onClick = onClick)
    }
}
