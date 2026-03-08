package com.example.agripredict.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Thème sombre — couleurs naturelles adaptées au mode nuit.
 */
private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryLight,
    secondary = BrownSecondaryLight,
    tertiary = OrangeAccent,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = ErrorRed,
    onPrimary = OnPrimaryDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark
)

/**
 * Thème clair — couleurs agricoles principales.
 */
private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = BrownSecondary,
    tertiary = OrangeAccent,
    background = BackgroundLight,
    surface = SurfaceLight,
    error = ErrorRed,
    onPrimary = OnPrimaryLight,
    onSecondary = OnSecondaryLight,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight
)

/**
 * Thème principal de AgriPredict.
 *
 * Utilise Material 3 (Material You) avec des couleurs agricoles.
 * Sur Android 12+, les couleurs dynamiques du système sont utilisées.
 */
@Composable
fun AgriPredictTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Désactivé pour garder le thème agricole
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}