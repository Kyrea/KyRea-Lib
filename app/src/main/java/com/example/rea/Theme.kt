package com.example.rea

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────
// SABİT RENKLER
// ─────────────────────────────────────────────
val CoverPalette = listOf(
    Color(0xFF5C3D2E), Color(0xFF2D4A3E), Color(0xFF2A3550),
    Color(0xFF4A2840), Color(0xFF2A3D4A), Color(0xFF3A4A28),
    Color(0xFF3D2A4A), Color(0xFF4A3A28)
)

val AmberCore   = Color(0xFFBF7A30)
val AmberLight2 = Color(0xFFD4954A)
val GreenDone   = Color(0xFF3A7A5C)

// ─────────────────────────────────────────────
// TEMA PALETİ
// ─────────────────────────────────────────────
data class ReaColorPalette(
    val ink:        Color,
    val ink2:       Color,
    val ink3:       Color,
    val bg:         Color,
    val bg2:        Color,
    val bg3:        Color,
    val cardBg:     Color,
    val headerBg:   Color,
    val headerText: Color,
    val headerSub:  Color,
    val amberLight: Color,
    val amber:      Color,
    val isDark:     Boolean
)

val LightPalette = ReaColorPalette(
    ink        = Color(0xFF2E241C),
    ink2       = Color(0xFF5A4A3C),
    ink3       = Color(0xFF9A8878),
    bg         = Color(0xFFF4EEE6),
    bg2        = Color(0xFFEDE4D8),
    bg3        = Color(0xFFD9CCBC),
    cardBg     = Color(0xFFFAF6F0),
    headerBg   = Color(0xFF3D2E22),
    headerText = Color(0xFFF5EDE2),
    headerSub  = Color(0xFFB8A090),
    amberLight = Color(0xFFFAEDD8),
    amber      = AmberCore,
    isDark     = false
)

val DarkPalette = ReaColorPalette(
    ink        = Color(0xFFEDE3D8),
    ink2       = Color(0xFFC4B5A5),
    ink3       = Color(0xFF7A6E64),
    bg         = Color(0xFF1C1612),
    bg2        = Color(0xFF252018),
    bg3        = Color(0xFF3A3028),
    cardBg     = Color(0xFF2A231A),
    headerBg   = Color(0xFF130F0C),
    headerText = Color(0xFFEDE3D8),
    headerSub  = Color(0xFF7A6E64),
    amberLight = Color(0xFF2E1E0A),
    amber      = AmberLight2,
    isDark     = true
)

// ─────────────────────────────────────────────
// COMPOSITION LOCAL
// ─────────────────────────────────────────────
val LocalReaColors = staticCompositionLocalOf { LightPalette }

// ─────────────────────────────────────────────
// MATERIAL COLOR SCHEME
// ─────────────────────────────────────────────
private fun lightScheme() = lightColorScheme(
    primary            = AmberCore,
    onPrimary          = Color(0xFF2E241C),
    primaryContainer   = Color(0xFFFAEDD8),
    onPrimaryContainer = Color(0xFF2E241C),
    background         = Color(0xFFF4EEE6),
    onBackground       = Color(0xFF2E241C),
    surface            = Color(0xFFFAF6F0),
    onSurface          = Color(0xFF2E241C),
    surfaceVariant     = Color(0xFFEDE4D8),
    onSurfaceVariant   = Color(0xFF9A8878),
    outline            = Color(0xFFD9CCBC),
    inverseSurface     = Color(0xFF3D2E22),
    inverseOnSurface   = Color(0xFFF5EDE2),
)

private fun darkScheme() = darkColorScheme(
    primary            = AmberLight2,
    onPrimary          = Color(0xFF1C1612),
    primaryContainer   = Color(0xFF2E1E0A),
    onPrimaryContainer = Color(0xFFEDE3D8),
    background         = Color(0xFF1C1612),
    onBackground       = Color(0xFFEDE3D8),
    surface            = Color(0xFF2A231A),
    onSurface          = Color(0xFFEDE3D8),
    surfaceVariant     = Color(0xFF252018),
    onSurfaceVariant   = Color(0xFF7A6E64),
    outline            = Color(0xFF3A3028),
    inverseSurface     = Color(0xFFEDE3D8),
    inverseOnSurface   = Color(0xFF1C1612),
)

// ─────────────────────────────────────────────
// TEMA FONKSİYONU
// ─────────────────────────────────────────────
@Composable
fun ReaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content:   @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    val scheme  = if (darkTheme) darkScheme() else lightScheme()

    CompositionLocalProvider(LocalReaColors provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            typography  = Typography(),
            content     = content
        )
    }
}