package net.ivpn.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val colorPrimary = Color(0xFF398FE6)

@Immutable
data class CustomColors(
    val textFieldLabel: Color = Color.Unspecified,
    val textFieldBackground: Color = Color.Unspecified,
    val textFieldText: Color = Color.Unspecified,
    val textFieldPlaceholder: Color = Color.Unspecified,
    val secondaryLabel: Color = Color.Unspecified
)

val LocalColors = staticCompositionLocalOf {
    CustomColors()
}

val CustomLightColorPalette = CustomColors(
    textFieldLabel = Color(0xA02A394B),
    textFieldBackground = Color(0x54D3DFE6),
    textFieldText = Color(0xFF2A394B),
    textFieldPlaceholder = Color(0x802A394B),
    secondaryLabel = Color(0x80000000)
)

val CustomDarkColorPalette = CustomColors(
    textFieldLabel = Color(0xFFFFFFFF),
    textFieldBackground = Color(0xFF1C1C1E),
    textFieldText = Color(0xFFFFFFFF),
    textFieldPlaceholder = Color(0x80FFFFFF),
    secondaryLabel = Color(0x80FFFFFF)
)
