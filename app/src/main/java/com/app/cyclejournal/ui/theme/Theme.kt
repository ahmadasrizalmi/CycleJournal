package com.app.cyclejournal.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4E6),
    onPrimaryContainer = Color(0xFFBE123C),
    secondary = PrimaryCoral,
    onSecondary = Color.White,
    background = BackgroundWhite,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = AlertBorder,
    onError = Color.White,
    errorContainer = AlertBg,
    onErrorContainer = AlertText
)

@Composable
fun CycleJournalTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    val palette = LocalV4Palette.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = palette.brandStart.toArgb()
                window.navigationBarColor = palette.paper.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                // The v4 header band is a brand gradient, so the status bar runs light-on-dark.
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = !palette.isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
