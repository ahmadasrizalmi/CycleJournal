package com.app.cyclejournal.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Material components the v4 design does not draw itself - dialogs, ripples, text selection - read
 * their colours from here, so the scheme has to follow the v4 palette. Pinning it to the light
 * scheme made every dialog a white card on a near-black app.
 */
private fun v4ColorScheme(p: V4Palette): ColorScheme {
    val base = if (p.isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = p.brandEnd,
        onPrimary = p.onBrand,
        primaryContainer = p.brandTint,
        onPrimaryContainer = p.ink,
        secondary = p.brandStart,
        onSecondary = p.onBrand,
        background = p.canvasSoft,
        onBackground = p.ink,
        surface = p.paper,
        onSurface = p.ink,
        surfaceVariant = p.canvasSoft,
        onSurfaceVariant = p.ink2,
        surfaceContainerHigh = p.paper,
        surfaceContainerHighest = p.canvasSoft,
        outline = p.fieldLine,
        outlineVariant = p.line,
        error = p.alertBrown,
        onError = p.onBrand,
        errorContainer = p.alertTint,
        onErrorContainer = p.alertBrown
    )
}

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

    val colorScheme = remember(palette) { v4ColorScheme(palette) }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        // Material's own contentColorFor() does not know the surfaceContainer roles, so an
        // AlertDialog title - a Text with no explicit colour - inherited plain black and vanished
        // on the dark dialog. Every surface here is a v4 composable, so ink is the right default
        // for anything that does not pick its own colour.
        CompositionLocalProvider(
            LocalContentColor provides palette.ink,
            content = content
        )
    }
}
