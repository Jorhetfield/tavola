package com.tavola.screenshottests

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.tavola.core.designsystem.preview.TavolaAdaptivePreviews
import com.tavola.core.designsystem.preview.TavolaFontScalePreviews
import com.tavola.core.designsystem.preview.TavolaScreenPreviews
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.feature.groups.SetupGroupActions
import com.tavola.feature.groups.SetupGroupContent
import com.tavola.feature.groups.SetupGroupError
import com.tavola.feature.groups.SetupGroupUiState
import com.tavola.feature.groups.SetupStep

@PreviewTest
@TavolaAdaptivePreviews
@Composable
fun SetupGroupChooseScreenshot() {
    TavolaTheme { SetupGroupContent(SetupGroupUiState(), SetupGroupActions()) }
}

@PreviewTest
@TavolaScreenPreviews
@Composable
fun SetupGroupCreateScreenshot() {
    TavolaTheme { SetupGroupContent(SetupGroupUiState(step = SetupStep.Create, name = "Casa de Marta"), SetupGroupActions()) }
}

/** The group is being created: the button waits and the back arrow is disabled. */
@PreviewTest
@TavolaScreenPreviews
@Composable
fun SetupGroupCreatingScreenshot() {
    TavolaTheme {
        SetupGroupContent(SetupGroupUiState(step = SetupStep.Create, name = "Casa de Marta", isWorking = true), SetupGroupActions())
    }
}

@PreviewTest
@TavolaScreenPreviews
@Composable
fun SetupGroupJoinInvalidCodeScreenshot() {
    TavolaTheme {
        SetupGroupContent(
            SetupGroupUiState(step = SetupStep.Join, code = "K7M2QX4R", error = SetupGroupError.InvalidCode),
            SetupGroupActions(),
        )
    }
}

/** The longest error with large text: nothing may be clipped. */
@PreviewTest
@TavolaFontScalePreviews
@Composable
fun SetupGroupJoinLargeFontScreenshot() {
    TavolaTheme {
        SetupGroupContent(
            SetupGroupUiState(step = SetupStep.Join, code = "K7M2QX", error = SetupGroupError.InvalidCode),
            SetupGroupActions(),
        )
    }
}

@PreviewTest
@TavolaFontScalePreviews
@Composable
fun SetupGroupChooseLargeFontScreenshot() {
    TavolaTheme { SetupGroupContent(SetupGroupUiState(), SetupGroupActions()) }
}
