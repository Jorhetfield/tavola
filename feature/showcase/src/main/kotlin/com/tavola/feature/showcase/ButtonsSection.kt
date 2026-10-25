package com.tavola.feature.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tavola.feature.showcase.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ButtonsSection(modifier: Modifier = Modifier) {
    ShowcaseSection(titleRes = R.string.showcase_buttons_title, modifier = modifier) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(ShowcaseDimens.ItemGap),
            verticalArrangement = Arrangement.spacedBy(ShowcaseDimens.ItemGap),
        ) {
            Button(onClick = {}, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(R.string.showcase_button_filled))
            }
            FilledTonalButton(onClick = {}, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(R.string.showcase_button_tonal))
            }
            OutlinedButton(onClick = {}, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(R.string.showcase_button_outlined))
            }
            ElevatedButton(onClick = {}, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(R.string.showcase_button_elevated))
            }
        }
        Button(
            onClick = {},
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.heightIn(min = ButtonDefaults.MediumContainerHeight),
            contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.MediumIconSize))
            Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(ButtonDefaults.MediumContainerHeight)))
            Text(text = stringResource(R.string.showcase_button_big), style = MaterialTheme.typography.titleMedium)
        }
        MealToggleGroup()
        SplitButtonDemo()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MealToggleGroup() {
    val options = listOf(
        R.string.showcase_group_breakfast,
        R.string.showcase_group_dinner,
        R.string.showcase_group_snack,
    )
    var selected by remember { mutableIntStateOf(0) }
    Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        options.forEachIndexed { index, label ->
            ToggleButton(
                checked = selected == index,
                onCheckedChange = { selected = index },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Text(stringResource(label))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SplitButtonDemo() {
    var checked by remember { mutableStateOf(false) }
    SplitButtonLayout(
        leadingButton = {
            SplitButtonDefaults.LeadingButton(onClick = {}) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize))
                Text(stringResource(R.string.showcase_split_main))
            }
        },
        trailingButton = {
            SplitButtonDefaults.TrailingButton(checked = checked, onCheckedChange = { checked = it }) {
                Icon(
                    imageVector = if (checked) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.showcase_split_toggle),
                    modifier = Modifier.size(SplitButtonDefaults.TrailingIconSize),
                )
            }
        },
    )
}
