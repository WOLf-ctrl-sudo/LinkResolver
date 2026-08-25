package com.linkresolver.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF3D5AFE),
    secondary = androidx.compose.ui.graphics.Color(0xFF00BFA5)
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF8C9EFF),
    secondary = androidx.compose.ui.graphics.Color(0xFF64FFDA)
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun LinkResolverTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        useDark -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
