package net.ivpn.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorPalette = darkColors(
    primary = colorPrimary,
    secondary = colorPrimary
)

private val LightColorPalette = lightColors(
    primary = colorPrimary,
    secondary = colorPrimary
)

@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors =
        if (darkTheme) DarkColorPalette
        else LightColorPalette

    val customColors =
        if (darkTheme) CustomDarkColorPalette
        else CustomLightColorPalette

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
