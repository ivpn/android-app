package net.ivpn.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import net.ivpn.core.common.nightmode.OledModeController

private val DarkColorPalette = darkColors(
    primary = colorPrimary,
    secondary = colorPrimary
)

private val OledColorPalette = darkColors(
    primary = colorPrimary,
    secondary = colorPrimary,
    background = oledBackground,
    surface = oledSurface
)

private val LightColorPalette = lightColors(
    primary = colorPrimary,
    secondary = colorPrimary
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    oledTheme: Boolean = OledModeController.isOledModeEnabled(),
    content: @Composable() () -> Unit
) {
    val colors = when {
        oledTheme -> OledColorPalette
        darkTheme -> DarkColorPalette
        else -> LightColorPalette
    }

    val customColors = when {
        oledTheme -> CustomOledColorPalette
        darkTheme -> CustomDarkColorPalette
        else -> CustomLightColorPalette
    }

    CompositionLocalProvider(
        LocalColors provides customColors
    ) {
        MaterialTheme(
            colors = colors,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}
