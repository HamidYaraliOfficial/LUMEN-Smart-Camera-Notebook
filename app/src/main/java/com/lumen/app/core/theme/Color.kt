package com.lumen.app.core.theme

import androidx.compose.ui.graphics.Color

/**
 * LUMEN ships five selectable themes:
 *  - WINDOWS11 : the default theme, inspired by Windows 11 Fluent (Mica-like surfaces, accent blue)
 *  - LIGHT     : plain Material 3 light theme
 *  - DARK      : plain Material 3 dark theme (AMOLED friendly)
 *  - RED       : accent-red variant (light base)
 *  - BLUE      : accent-blue variant (light base)
 */
enum class LumenThemeOption(val id: String) {
    WINDOWS11("windows11"),
    LIGHT("light"),
    DARK("dark"),
    RED("red"),
    BLUE("blue");

    companion object {
        fun fromId(id: String?): LumenThemeOption = entries.firstOrNull { it.id == id } ?: WINDOWS11
    }
}

// ---------- Windows 11 (default) ----------
val Win11AccentBlue = Color(0xFF0078D4)
val Win11AccentBlueDark = Color(0xFF60CDFF)
val Win11Surface = Color(0xFFF3F3F3)
val Win11SurfaceVariant = Color(0xFFE9E9E9)
val Win11Background = Color(0xFFFAFAFA)
val Win11OnSurface = Color(0xFF1B1B1B)
val Win11Outline = Color(0xFFC7C7C7)

// ---------- Plain Light ----------
val LightPrimary = Color(0xFF3D5AFE)
val LightSurface = Color(0xFFFFFFFF)
val LightBackground = Color(0xFFF7F7F9)
val LightOnSurface = Color(0xFF1A1A1A)

// ---------- Plain Dark (AMOLED-friendly) ----------
val DarkPrimary = Color(0xFF8AB4FF)
val DarkSurface = Color(0xFF000000)
val DarkSurfaceVariant = Color(0xFF121212)
val DarkBackground = Color(0xFF000000)
val DarkOnSurface = Color(0xFFEAEAEA)

// ---------- Red accent ----------
val RedPrimary = Color(0xFFC62828)
val RedPrimaryDark = Color(0xFFFF6E6E)
val RedSurface = Color(0xFFFFFBFA)
val RedBackground = Color(0xFFFFF3F1)

// ---------- Blue accent ----------
val BluePrimary = Color(0xFF1565C0)
val BluePrimaryDark = Color(0xFF9EC9FF)
val BlueSurface = Color(0xFFFAFCFF)
val BlueBackground = Color(0xFFF0F6FF)

// ---------- Shared semantic colors ----------
val ConfidenceHigh = Color(0xFF2E7D32)
val ConfidenceMedium = Color(0xFFF9A825)
val ConfidenceLow = Color(0xFFC62828)
