package com.example.kassaku.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.kassaku.utils.ThemeMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalIsDark = staticCompositionLocalOf { false }

private val LightColors = lightColorScheme(
    primary = ToscaPrimary,              // Tosca sebagai warna utama
    primaryContainer = ToscaContainer,   // Tosca muda untuk container (navbar indicator)
    onPrimaryContainer = ToscaPrimary,   // Text on primary container
    secondary = SoftRed,                 // Merah untuk pengeluaran
    tertiary = GoldAccent,
    background = LightBackground,
    surface = WhiteSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,           // Text putih pada merah
    onTertiary = LightOnSurface,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant, // For Card/Chip backgrounds
    onSurfaceVariant = LightOnSurfaceVariant, // Text on cards/chips
    error = SoftRed,                     // Using SoftRed for errors
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = ToscaPrimary,              // Emerald tetap di dark mode
    primaryContainer = Color(0xFF064E3B), // Emerald gelap untuk container di dark mode
    onPrimaryContainer = ToscaPrimary,   // Text on primary container
    secondary = SoftRed,                 // Rose untuk pengeluaran
    tertiary = GoldAccent,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,           // Text putih pada merah
    onTertiary = DarkOnSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant, // For Card/Chip backgrounds in dark mode
    onSurfaceVariant = DarkOnSurfaceVariant, // Text on cards/chips in dark mode
    error = SoftRed,
    onError = Color.White
)

@Composable
fun KasSakuTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    CompositionLocalProvider(LocalIsDark provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
