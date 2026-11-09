package com.tavola.feature.shopping

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.field.TavolaTextField
import com.tavola.core.designsystem.sheet.TavolaModalBottomSheet
import com.tavola.core.designsystem.sheet.rememberSheetCloser
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.ShoppingCategory

private val CategoryBadgeSize = 40.dp
private val CategoryBadgeIconSize = 22.dp
private const val EXPANDED_CHEVRON_DEGREES = 180f

/**
 * The bottom sheet that adds items to the list. It stays open after adding, so several items can be typed one after another; it is
 * closed by swiping it away, tapping outside, or "Done". Closing drops whatever was half typed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShoppingAddSheet(draft: AddItemDraft, actions: ShoppingActions) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val close = rememberSheetCloser(sheetState, onClosed = actions.onAddSheetDismiss)
    // The keyboard waits for the sheet to be fully open: asking for it while the sheet is still sliding in makes the keyboard come
    // up first and cover the sheet.
    val sheetIsOpen = sheetState.currentValue == SheetValue.Expanded
    TavolaModalBottomSheet(onDismissRequest = actions.onAddSheetDismiss, sheetState = sheetState) {
        ShoppingAddSheetContent(draft, actions, onDone = close, readyForInput = sheetIsOpen)
    }
}

/**
 * Stateless content of the sheet: the name, an optional quantity, the category and the button. The category is already guessed
 * from the name; tapping it opens a grid of tiles to pick another one in one tap. Once it is [readyForInput] (the sheet is open) the
 * name takes the focus, so the keyboard comes up on it, and again after each item is added.
 */
@Composable
fun ShoppingAddSheetContent(
    draft: AddItemDraft,
    actions: ShoppingActions,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    readyForInput: Boolean = false,
    startWithCategoriesOpen: Boolean = false,
) {
    val spacing = TavolaTheme.spacing
    val focusRequester = remember { FocusRequester() }
    var categoriesOpen by rememberSaveable { mutableStateOf(startWithCategoriesOpen) }

    LaunchedEffect(readyForInput, draft.addedName) { if (readyForInput) focusRequester.requestFocus() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = spacing.lg)
            .padding(bottom = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Text(text = stringResource(R.string.shopping_add_sheet_title), style = MaterialTheme.typography.headlineSmall)
        TavolaTextField(
            value = draft.name,
            onValueChange = actions.onDraftNameChange,
            label = stringResource(R.string.shopping_add_name_label),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            onImeAction = { if (draft.canAdd) actions.onAddClick() },
        )
        TavolaTextField(
            value = draft.quantity,
            onValueChange = actions.onDraftQuantityChange,
            label = stringResource(R.string.shopping_add_quantity_label),
            modifier = Modifier.fillMaxWidth(),
            imeAction = ImeAction.Done,
            onImeAction = { if (draft.canAdd) actions.onAddClick() },
        )
        CategorySelector(draft.category, draft.isSuggested, categoriesOpen, onClick = { categoriesOpen = !categoriesOpen })
        AnimatedVisibility(
            visible = categoriesOpen,
            enter =
                expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) + fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()) + fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
        ) {
            ShoppingCategoryGrid(
                selected = draft.category,
                onPick = { category ->
                    actions.onDraftCategoryPick(category)
                    categoriesOpen = false
                },
            )
        }
        AddedNotice(draft.addedName)
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onDone) { Text(stringResource(R.string.shopping_add_done)) }
            AddButton(enabled = draft.canAdd, onClick = actions.onAddClick, modifier = Modifier.weight(1f))
        }
    }
}

/** The category that will be used, with a tag when it was guessed from the name; tapping it opens or closes the grid of categories. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CategorySelector(category: ShoppingCategory, isSuggested: Boolean, open: Boolean, onClick: () -> Unit) {
    val spacing = TavolaTheme.spacing
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val chevron by animateFloatAsState(if (open) EXPANDED_CHEVRON_DEGREES else 0f, motion.fastSpatialSpec(), label = "categoryChevron")

    Surface(shape = MaterialTheme.shapes.large, color = scheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(
                    onClickLabel = stringResource(R.string.shopping_add_category_change),
                    role = Role.DropdownList,
                    onClick = onClick,
                )
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            AnimatedContent(
                targetState = category,
                modifier = Modifier.weight(1f),
                transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
                label = "categorySelectorCategory",
            ) { shown ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                    Box(
                        modifier = Modifier
                            .size(CategoryBadgeSize)
                            .clip(CircleShape)
                            .background(scheme.tertiaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            shown.icon,
                            contentDescription = null,
                            tint = scheme.onTertiaryContainer,
                            modifier = Modifier.size(CategoryBadgeIconSize),
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.shopping_add_category_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                        )
                        Text(text = stringResource(shown.labelRes), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            AnimatedVisibility(
                visible = isSuggested,
                enter = fadeIn(motion.defaultEffectsSpec()),
                exit = fadeOut(motion.fastEffectsSpec()),
            ) {
                Text(
                    text = stringResource(R.string.shopping_add_suggested),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onPrimaryContainer,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(scheme.primaryContainer)
                        .padding(horizontal = spacing.sm, vertical = spacing.xs),
                )
            }
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                modifier = Modifier.graphicsLayer { rotationZ = chevron },
            )
        }
    }
}

/** "Added: Milk", shown under the fields once an item is added and gone as soon as something new is typed. */
@Composable
private fun AddedNotice(addedName: String?) {
    val motion = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = addedName,
        modifier = Modifier.animateContentSize(motion.defaultSpatialSpec()),
        transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
        label = "shoppingAddedNotice",
    ) { name ->
        if (name != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm)) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                Text(
                    text = stringResource(R.string.shopping_add_added, name),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AddButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, enabled = enabled, shapes = ButtonDefaults.shapes(), modifier = modifier) {
        Icon(Icons.Rounded.Add, contentDescription = null)
        Text(text = stringResource(R.string.shopping_add), modifier = Modifier.padding(start = TavolaTheme.spacing.sm))
    }
}
