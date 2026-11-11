package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.settings.AppThemeMode

data class TradingThemeColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val card: Color,
    val border: Color,
    val hover: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val gold: Color,
    val bullishGreen: Color,
    val bearishRed: Color,
    val bullishGreenBg: Color,
    val bearishRedBg: Color,
    val isDark: Boolean
)

val DarkTradingColors = TradingThemeColors(
    background = NavyBackground,
    surface = NavySurface,
    surfaceVariant = NavySurfaceVariant,
    card = NavyCard,
    border = NavyBorder,
    hover = NavyHover,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    gold = NexusGold,
    bullishGreen = BullishGreen,
    bearishRed = BearishRed,
    bullishGreenBg = BullishGreenBg,
    bearishRedBg = BearishRedBg,
    isDark = true
)

val LightTradingColors = TradingThemeColors(
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9),
    card = Color(0xFFFFFFFF),
    border = Color(0xFFCBD5E1),
    hover = Color(0xFFE2E8F0),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF334155),
    textMuted = Color(0xFF64748B),
    gold = Color(0xFFB88214),
    bullishGreen = Color(0xFF00A352),
    bearishRed = Color(0xFFDC2626),
    bullishGreenBg = Color(0x1F00A352),
    bearishRedBg = Color(0x1FDC2626),
    isDark = false
)

val LocalTradingColors = staticCompositionLocalOf { DarkTradingColors }

object NexusTheme {
    val colors: TradingThemeColors
        @Composable
        get() = LocalTradingColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = NexusGold,
    onPrimary = NavyBackground,
    primaryContainer = NexusGoldDark,
    onPrimaryContainer = TextPrimary,
    secondary = NeutralCyan,
    onSecondary = NavyBackground,
    secondaryContainer = NavySurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = BullishGreen,
    onTertiary = NavyBackground,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NavyBorder,
    outlineVariant = NavyHover,
    error = BearishRed,
    onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB88214),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF00A352),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun NexusTradingTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemDark
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val tradingColors = if (isDark) DarkTradingColors else LightTradingColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(LocalTradingColors provides tradingColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
