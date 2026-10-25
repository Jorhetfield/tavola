package com.tavola.feature.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tavola.feature.showcase.R

@Composable
internal fun ChipsSection(modifier: Modifier = Modifier) {
    val labels = listOf(
        R.string.showcase_chip_vegetarian,
        R.string.showcase_chip_quick,
        R.string.showcase_chip_greek,
        R.string.showcase_chip_italian,
    )
    var selected by remember { mutableStateOf(setOf(R.string.showcase_chip_greek)) }
    var reminder by remember { mutableStateOf(true) }

    ShowcaseSection(titleRes = R.string.showcase_chips_title, modifier = modifier) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(ShowcaseDimens.SectionGap)) {
            labels.forEach { label ->
                val isSelected = label in selected
                FilterChip(
                    selected = isSelected,
                    onClick = { selected = if (isSelected) selected - label else selected + label },
                    label = { Text(stringResource(label)) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(ShowcaseDimens.ItemGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Switch(
                checked = reminder,
                onCheckedChange = { isOn -> reminder = isOn },
                thumbContent = if (reminder) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
            )
            Text(stringResource(R.string.showcase_switch_label))
        }
    }
}
