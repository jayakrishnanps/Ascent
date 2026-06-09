package com.yourapp.productivity.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant
)

private val GreenColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = GreenOnPrimaryContainer,
    secondary = GreenSecondary,
    onSecondary = GreenOnSecondary,
    secondaryContainer = GreenSecondaryContainer,
    onSecondaryContainer = GreenOnSecondaryContainer,
    tertiary = GreenTertiary,
    onTertiary = GreenOnTertiary,
    tertiaryContainer = GreenTertiaryContainer,
    onTertiaryContainer = GreenOnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant
)

private val RedColorScheme = darkColorScheme(
    primary = RedPrimary,
    onPrimary = RedOnPrimary,
    primaryContainer = RedPrimaryContainer,
    onPrimaryContainer = RedOnPrimaryContainer,
    secondary = RedSecondary,
    onSecondary = RedOnSecondary,
    secondaryContainer = RedSecondaryContainer,
    onSecondaryContainer = RedOnSecondaryContainer,
    tertiary = RedTertiary,
    onTertiary = RedOnTertiary,
    tertiaryContainer = RedTertiaryContainer,
    onTertiaryContainer = RedOnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant
)

private val YellowColorScheme = darkColorScheme(
    primary = YellowPrimary,
    onPrimary = YellowOnPrimary,
    primaryContainer = YellowPrimaryContainer,
    onPrimaryContainer = YellowOnPrimaryContainer,
    secondary = YellowSecondary,
    onSecondary = YellowOnSecondary,
    secondaryContainer = YellowSecondaryContainer,
    onSecondaryContainer = YellowOnSecondaryContainer,
    tertiary = YellowTertiary,
    onTertiary = YellowOnTertiary,
    tertiaryContainer = YellowTertiaryContainer,
    onTertiaryContainer = YellowOnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant
)

private val BlueColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = BlueOnPrimary,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = BlueOnPrimaryContainer,
    secondary = BlueSecondary,
    onSecondary = BlueOnSecondary,
    secondaryContainer = BlueSecondaryContainer,
    onSecondaryContainer = BlueOnSecondaryContainer,
    tertiary = BlueTertiary,
    onTertiary = BlueOnTertiary,
    tertiaryContainer = BlueTertiaryContainer,
    onTertiaryContainer = BlueOnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant
)

@Composable
fun AscentTheme(
    themeMode: String = "DARK", // Defaulting to DARK aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode.uppercase()) {
        "GREEN" -> GreenColorScheme
        "RED" -> RedColorScheme
        "YELLOW" -> YellowColorScheme
        "BLUE" -> BlueColorScheme
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

