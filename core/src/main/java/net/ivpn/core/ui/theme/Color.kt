package net.ivpn.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val colorPrimary = Color(0xFF398FE6)
val oledBackground = Color(0xFF000000)
val oledSurface = Color(0xFF000000)
val oledCard = Color(0xFF0A0A0A)

@Immutable
data class CustomColors(
    val textFieldLabel: Color = Color.Unspecified,
    val textFieldBackground: Color = Color.Unspecified,
    val textFieldText: Color = Color.Unspecified,
    val textFieldPlaceholder: Color = Color.Unspecified,
    val secondaryLabel: Color = Color.Unspecified,
    val background: Color = Color.Unspecified,
    val surface: Color = Color.Unspecified
)

val LocalColors = staticCompositionLocalOf {
    CustomColors()
}

val CustomLightColorPalette = CustomColors(
    textFieldLabel = Color(0xA02A394B),
    textFieldBackground = Color(0x54D3DFE6),
    textFieldText = Color(0xFF2A394B),
    textFieldPlaceholder = Color(0x802A394B),
    secondaryLabel = Color(0x80000000),
    background = Color(0xFFF5F9FC),
    surface = Color(0xFFFFFFFF)
)

val CustomDarkColorPalette = CustomColors(
    textFieldLabel = Color(0xFFFFFFFF),
    textFieldBackground = Color(0xFF1C1C1E),
    textFieldText = Color(0xFFFFFFFF),
    textFieldPlaceholder = Color(0x80FFFFFF),
    secondaryLabel = Color(0x80FFFFFF),
    background = Color(0xFF121212),
    surface = Color(0xFF202020)
)

val CustomOledColorPalette = CustomColors(
    textFieldLabel = Color(0xFFFFFFFF),
    textFieldBackground = Color(0xFF0A0A0A),
    textFieldText = Color(0xFFFFFFFF),
    textFieldPlaceholder = Color(0x80FFFFFF),
    secondaryLabel = Color(0x80FFFFFF),
    background = oledBackground,
    surface = oledSurface
)
