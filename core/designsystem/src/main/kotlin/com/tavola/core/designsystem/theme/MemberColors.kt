package com.tavola.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** The color of a person of a group, with the color of what is drawn on it (an initial). */
@Immutable
data class TavolaMemberColor(val container: Color, val content: Color)

/** The eight colors a member can have, from the Mediterranean palette. A member's color index wraps around them. */
internal val TavolaLightMemberColors = listOf(
    TavolaMemberColor(Azafran, AzafranOscuro),
    TavolaMemberColor(Egeo, CrudoLowest),
    TavolaMemberColor(Albahaca, CrudoLowest),
    TavolaMemberColor(Terracota, CrudoLowest),
    TavolaMemberColor(Limon, AzafranOscuro),
    TavolaMemberColor(Mediterraneo, NocheEgeo),
    TavolaMemberColor(OlivaClaro, OlivaTexto),
    TavolaMemberColor(Arcilla, AzafranOscuro),
)

internal val TavolaDarkMemberColors = listOf(
    TavolaMemberColor(AzafranNoche, AzafranOscuro),
    TavolaMemberColor(MediterraneoNoche, NocheProfunda),
    TavolaMemberColor(Albahaca, TextoNoche),
    TavolaMemberColor(TerracotaNoche, TerracotaNocheTexto),
    TavolaMemberColor(Limon, AzafranOscuro),
    TavolaMemberColor(NocheMaximo, TextoNoche),
    TavolaMemberColor(OlivaNoche, OlivaNocheTexto),
    TavolaMemberColor(Arcilla, AzafranOscuro),
)
