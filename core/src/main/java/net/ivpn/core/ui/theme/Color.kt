package net.ivpn.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val colorPrimary = Color(0xFF398FE6)

@Immutable
data class CustomColors(
    val textFieldLabel: Color = Color.Unspecified
)

val LocalColors = staticCompositionLocalOf {
    CustomColors()
}

val OnLightCustomColors = CustomColors(
    textFieldLabel = Color(0xFF2A394B)
)

val OnDarkCustomColors = CustomColors(
    textFieldLabel = Color(0xFFFFFFFF)
)
