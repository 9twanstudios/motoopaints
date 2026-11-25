package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    DAYLIGHT("Daylight"),
    DARK_BOOTH("Dark Booth")
}

private val DaylightColorScheme = lightColorScheme(
    primary = SignalOrangeLight,
    onPrimary = OnSignalOrangeLight,
    primaryContainer = SignalOrangeContainerLight,
    onPrimaryContainer = OnSignalOrangeContainerLight,
    secondary = WorkshopSlateLight,
    onSecondary = OnWorkshopSlateLight,
    secondaryContainer = WorkshopSlateContainerLight,
    onSecondaryContainer = OnWorkshopSlateContainerLight,
    tertiary = BrassAccentLight,
    tertiaryContainer = BrassContainerLight,
    onTertiaryContainer = OnBrassContainerLight,
    background = DaylightBackground,
    onBackground = DaylightOnBackground,
    surface = DaylightSurface,
    onSurface = DaylightOnSurface,
    surfaceVariant = DaylightSurfaceVariant,
    onSurfaceVariant = DaylightOnSurfaceVariant,
    outline = DaylightOutline,
    error = AlertCrimson
)

private val DarkBoothColorScheme = darkColorScheme(
    primary = SignalOrangeDark,
    onPrimary = OnSignalOrangeDark,
    primaryContainer = SignalOrangeContainerDark,
    onPrimaryContainer = OnSignalOrangeContainerDark,
    secondary = WorkshopSlateDark,
    onSecondary = OnWorkshopSlateDark,
    secondaryContainer = WorkshopSlateContainerDark,
    onSecondaryContainer = OnWorkshopSlateContainerDark,
    tertiary = BrassAccentDark,
    tertiaryContainer = BrassContainerDark,
    onTertiaryContainer = OnBrassContainerDark,
    background = BoothBackground,
    onBackground = BoothOnBackground,
    surface = BoothSurface,
    onSurface = BoothOnSurface,
    surfaceVariant = BoothSurfaceVariant,
    onSurfaceVariant = BoothOnSurfaceVariant,
    outline = BoothOutline,
    error = AlertCrimsonDark
)

val MotoopaintShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MotoopaintTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DAYLIGHT -> false
        ThemeMode.DARK_BOOTH -> true
    }

    val colorScheme = if (isDark) DarkBoothColorScheme else DaylightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MotoopaintTypography,
        shapes = MotoopaintShapes,
        content = content
    )
}
