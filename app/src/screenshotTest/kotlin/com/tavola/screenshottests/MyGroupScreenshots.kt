package com.tavola.screenshottests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.tavola.core.designsystem.dialog.TavolaConfirmDialog
import com.tavola.core.designsystem.member.TavolaMemberAvatar
import com.tavola.core.designsystem.preview.TavolaAdaptivePreviews
import com.tavola.core.designsystem.preview.TavolaFontScalePreviews
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.preview.TavolaScreenPreviews
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.Group
import com.tavola.domain.model.GroupMember
import com.tavola.feature.groups.MemberItem
import com.tavola.feature.groups.MyGroupActions
import com.tavola.feature.groups.MyGroupContent
import com.tavola.feature.groups.MyGroupDialog
import com.tavola.feature.groups.MyGroupUiState

private val sampleGroup = Group("g1", "Casa de Marta", listOf("marta", "luis", "ana", "ivan", "sofia", "pablo"), "K7M2QX4R")

private val sampleMembers = listOf(
    MemberItem(GroupMember("marta", "Marta Ruiz", null, colorIndex = 0), isMe = true),
    MemberItem(GroupMember("luis", "Luis Gómez", null, colorIndex = 1), isMe = false),
    MemberItem(GroupMember("ana", "Ana", null, colorIndex = 2), isMe = false),
    MemberItem(GroupMember("ivan", "Iván", null, colorIndex = 3), isMe = false),
    MemberItem(GroupMember("sofia", "Sofía Martín", null, colorIndex = 4), isMe = false),
    MemberItem(GroupMember("pablo", null, null, colorIndex = 5), isMe = false),
)

private fun sampleState(dialog: MyGroupDialog? = null) = MyGroupUiState.Success(sampleGroup, sampleMembers, dialog = dialog)

@Composable
private fun SampleMyGroup(state: MyGroupUiState) {
    TavolaTheme { MyGroupContent(state, MyGroupActions(), onBack = {}, onCopyCode = {}, onShareCode = {}) }
}

@PreviewTest
@TavolaAdaptivePreviews
@Composable
fun MyGroupScreenshot() {
    SampleMyGroup(sampleState())
}

@PreviewTest
@TavolaFontScalePreviews
@Composable
fun MyGroupLargeFontScreenshot() {
    SampleMyGroup(sampleState())
}

@PreviewTest
@TavolaScreenPreviews
@Composable
fun MyGroupLeaveDialogScreenshot() {
    SampleMyGroup(sampleState(dialog = MyGroupDialog.Leave))
}

@PreviewTest
@TavolaScreenPreviews
@Composable
fun MyGroupRenewDialogScreenshot() {
    SampleMyGroup(sampleState(dialog = MyGroupDialog.RenewCode))
}

/** The eight colors of the member palette, one avatar each (the last one has no name and shows a question mark). */
@PreviewTest
@TavolaPreviews
@Composable
fun MemberAvatarsScreenshot() {
    TavolaTheme {
        FlowRow(
            modifier = Modifier.padding(TavolaTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        ) {
            listOf("Marta", "Luis", "Ana", "Iván", "Sofía", "Pablo", "Rosa", null).forEachIndexed { index, name ->
                TavolaMemberAvatar(name = name, colorIndex = index)
            }
        }
    }
}

@PreviewTest
@TavolaPreviews
@Composable
fun ConfirmDialogScreenshot() {
    TavolaTheme {
        TavolaConfirmDialog(
            title = "¿Salir del grupo?",
            message = "Dejarás de ver el calendario y las recetas del grupo.",
            confirmText = "Salir",
            dismissText = "Cancelar",
            onConfirm = {},
            onDismiss = {},
            isDestructive = true,
        )
    }
}
