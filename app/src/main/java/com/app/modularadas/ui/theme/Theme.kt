package com.app.modularadas.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    onPrimary = BackgroundDark,
    primaryContainer = Teal40,
    onPrimaryContainer = BackgroundDark,
    secondary = Slate80,
    onSecondary = BackgroundDark,
    secondaryContainer = Slate40,
    onSecondaryContainer = BackgroundDark,
    tertiary = Amber80,
    onTertiary = BackgroundDark,
    tertiaryContainer = Amber40,
    onTertiaryContainer = BackgroundDark,
    background = BackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Slate80,
    outline = OutlineDark,
    outlineVariant = OutlineDark,
    error = Color(0xFFFF8A80),
    onError = BackgroundDark,
    errorContainer = Color(0xFF9F3A35),
    onErrorContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    onPrimary = Color.White,
    primaryContainer = Teal80,
    onPrimaryContainer = BackgroundDark,
    secondary = Slate40,
    onSecondary = Color.White,
    secondaryContainer = Slate80,
    onSecondaryContainer = BackgroundDark,
    tertiary = Amber40,
    onTertiary = Color.White,
    tertiaryContainer = Amber80,
    onTertiaryContainer = BackgroundDark,
    background = BackgroundLight,
    onBackground = BackgroundDark,
    surface = SurfaceLight,
    onSurface = BackgroundDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Slate40,
    outline = OutlineLight,
    outlineVariant = SurfaceVariantLight,
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

@Composable
fun ModularADASTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}