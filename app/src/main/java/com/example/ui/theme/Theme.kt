package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ChampionsLeagueCyan,
    onPrimary = Color(0xFF002233),
    primaryContainer = ChampionsLeagueDark,
    onPrimaryContainer = Color(0xFFC7F1FF),
    secondary = EuropaLeagueAmber,
    onSecondary = Color(0xFF3B2000),
    secondaryContainer = EuropaLeagueDark,
    onSecondaryContainer = Color(0xFFFFDDB0),
    tertiary = StarGold,
    background = UefaDeepNavy,
    onBackground = TextPrimary,
    surface = UefaSurfaceNavy,
    onSurface = TextPrimary,
    surfaceVariant = UefaCardNavy,
    onSurfaceVariant = TextSecondary,
    outline = UefaCardBorder
)

private val LightColorScheme = darkColorScheme(
    // We intentionally keep the iconic deep UEFA night aesthetic as default for high contrast
    primary = ChampionsLeagueCyan,
    onPrimary = Color(0xFF002233),
    primaryContainer = ChampionsLeagueDark,
    onPrimaryContainer = Color(0xFFC7F1FF),
    secondary = EuropaLeagueAmber,
    onSecondary = Color(0xFF3B2000),
    secondaryContainer = EuropaLeagueDark,
    onSecondaryContainer = Color(0xFFFFDDB0),
    tertiary = StarGold,
    background = UefaDeepNavy,
    onBackground = TextPrimary,
    surface = UefaSurfaceNavy,
    onSurface = TextPrimary,
    surfaceVariant = UefaCardNavy,
    onSurfaceVariant = TextSecondary,
    outline = UefaCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
