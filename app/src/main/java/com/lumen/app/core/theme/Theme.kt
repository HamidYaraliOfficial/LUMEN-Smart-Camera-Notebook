package com.lumen.app.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Win11ColorScheme = lightColorScheme(
    primary = Win11AccentBlue,
    onPrimary = Color.White,
    secondary = Win11AccentBlueDark,
    background = Win11Background,
    surface = Win11Surface,
    surfaceVariant = Win11SurfaceVariant,
    onSurface = Win11OnSurface,
    onBackground = Win11OnSurface,
    outline = Win11Outline,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onBackground = LightOnSurface,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.Black,
    background = DarkBackground,
    surface = DarkSurfaceVariant,
    onSurface = DarkOnSurface,
    onBackground = DarkOnSurface,
)

private val RedColorScheme = lightColorScheme(
    primary = RedPrimary,
    onPrimary = Color.White,
    background = RedBackground,
    surface = RedSurface,
    onSurface = Color(0xFF321313),
    onBackground = Color(0xFF321313),
)

private val BlueColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    background = BlueBackground,
    surface = BlueSurface,
    onSurface = Color(0xFF0D1B2A),
    onBackground = Color(0xFF0D1B2A),
)

/** Rounded, Fluent/Windows-11-like corner radii used across cards, sheets and buttons. */
object LumenShapes {
    const val CardRadius = 12
    const val SheetRadius = 16
    const val ChipRadius = 8
}

@Composable
fun LumenTheme(
    themeOption: LumenThemeOption = LumenThemeOption.WINDOWS11,
    useSystemDarkVariant: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeOption) {
        LumenThemeOption.WINDOWS11 -> if (useSystemDarkVariant) DarkColorScheme else Win11ColorScheme
        LumenThemeOption.LIGHT -> LightColorScheme
        LumenThemeOption.DARK -> DarkColorScheme
        LumenThemeOption.RED -> RedColorScheme
        LumenThemeOption.BLUE -> BlueColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LumenTypography,
        content = content
    )
}
