package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArenaXColorScheme = darkColorScheme(
    primary = ElectricVioletLight,
    onPrimary = Color.White,
    primaryContainer = ElectricVioletDark,
    onPrimaryContainer = Color.White,
    secondary = CyberCyanLight,
    onSecondary = Color.Black,
    secondaryContainer = CyberCyanDark,
    onSecondaryContainer = Color.White,
    tertiary = EsportsGoldLight,
    onTertiary = Color.Black,
    tertiaryContainer = EsportsGold,
    onTertiaryContainer = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    error = CrimsonRed,
    onError = Color.White
)

@Composable
fun ArenaXTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArenaXColorScheme,
        typography = Typography,
        content = content
    )
}
