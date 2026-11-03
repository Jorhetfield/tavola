package com.tavola.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Roles semánticos propios de Tavola que Material 3 no cubre. */
@Immutable
data class TavolaColors(
    val dayCard: Color,
    val onDayCard: Color,
    val dayCardActive: Color,
    val onDayCardActive: Color,
    val dayCardMuted: Color,
    val onDayCardMuted: Color,
    val mealSlot: Color,
    val onMealSlot: Color,
    val lunchAccent: Color,
    val dinnerAccent: Color,
    val taskChip: Color,
    val onTaskChip: Color,
    val taskChipDone: Color,
    val onTaskChipDone: Color,
    val memberColors: List<TavolaMemberColor>,
)

internal val TavolaLightExtraColors = TavolaColors(
    dayCard = MediterraneoSuave,
    onDayCard = NocheEgeo,
    dayCardActive = Azafran,
    onDayCardActive = AzafranOscuro,
    dayCardMuted = Color(0xFFEFEAE0),
    onDayCardMuted = Color(0xFF6B7480),
    mealSlot = OlivaSuave,
    onMealSlot = Color(0xFF24401A),
    lunchAccent = Limon,
    dinnerAccent = Egeo,
    taskChip = AzafranSuave,
    onTaskChip = Color(0xFF5A3000),
    taskChipDone = Color(0xFFEAE3D6),
    onTaskChipDone = Color(0xFF8A8576),
    memberColors = TavolaLightMemberColors,
)

internal val TavolaDarkExtraColors = TavolaColors(
    dayCard = NocheCard,
    onDayCard = Color(0xFFE6EEF6),
    dayCardActive = AzafranNoche,
    onDayCardActive = AzafranOscuro,
    dayCardMuted = Color(0xFF142433),
    onDayCardMuted = Color(0xFF7F93A8),
    mealSlot = OlivaNoche,
    onMealSlot = OlivaNocheTexto,
    lunchAccent = Limon,
    dinnerAccent = MediterraneoNoche,
    taskChip = TerracotaNoche,
    onTaskChip = TerracotaNocheTexto,
    taskChipDone = Color(0xFF1F3347),
    onTaskChipDone = Color(0xFF7F93A8),
    memberColors = TavolaDarkMemberColors,
)

internal val LocalTavolaColors = staticCompositionLocalOf { TavolaLightExtraColors }
