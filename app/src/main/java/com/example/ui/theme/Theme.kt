package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CobaltLight,
    onPrimary = Color.White,
    primaryContainer = CobaltPrimary,
    onPrimaryContainer = CobaltContainer,
    secondary = Color(0xFFFB923C),
    onSecondary = Color(0xFF431407),
    secondaryContainer = Color(0xFF7C2D12),
    onSecondaryContainer = CoralContainer,
    tertiary = BeginnerEmerald,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMuted,
    outline = DarkOutline,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

private val LightColorScheme = lightColorScheme(
    primary = CobaltPrimary,
    onPrimary = Color.White,
    primaryContainer = CobaltContainer,
    onPrimaryContainer = OnCobaltContainer,
    secondary = CoralAccent,
    onSecondary = Color.White,
    secondaryContainer = CoralContainer,
    onSecondaryContainer = OnCoralContainer,
    tertiary = BeginnerEmerald,
    onTertiary = Color.White,
    background = IvoryBackground,
    onBackground = SlateInk,
    surface = IvorySurface,
    onSurface = SlateInk,
    surfaceVariant = IvorySurfaceVariant,
    onSurfaceVariant = SlateMuted,
    outline = OutlineWarm,
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun WordChainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WordChainTypography,
        content = content
    )
}
