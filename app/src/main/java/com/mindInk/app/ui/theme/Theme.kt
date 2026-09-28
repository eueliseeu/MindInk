package com.mindInk.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity

private val MindInkDarkColorScheme = darkColorScheme(
    primary = MindInkRed,
    onPrimary = MindInkWhite,
    primaryContainer = MindInkRedDark,
    onPrimaryContainer = MindInkWhite,
    secondary = MindInkSurfaceVariant,
    onSecondary = MindInkWhite,
    background = MindInkBlack,
    onBackground = MindInkWhite,
    surface = MindInkSurface,
    onSurface = MindInkWhite,
    surfaceVariant = MindInkSurfaceVariant,
    onSurfaceVariant = MindInkMuted,
    outline = MindInkBorder,
    error = MindInkError,
    onError = MindInkWhite
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = MindInkDarkColorScheme,
        typography = Typography,
        content = content
    )
}