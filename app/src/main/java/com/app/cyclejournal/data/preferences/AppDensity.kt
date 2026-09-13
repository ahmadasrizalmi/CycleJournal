package com.app.cyclejournal.data.preferences

import android.content.Context
import android.content.res.Configuration

/**
 * Applies the in-app text size by scaling the context density, the same lever Android's own
 * "Display size" uses.
 *
 * Density (not the font scale) is deliberate: every v4 dimension is a fixed dp box (40/52/96dp
 * rows), so growing the font alone would overflow those boxes. Scaling density keeps text and its
 * container in the same ratio - and, unlike a Compose `LocalDensity` override, it also reaches the
 * separate windows Compose opens for bottom sheets and dialogs.
 */
object AppDensity {

    /** Returns a context whose density carries [scale] (1.0 keeps the device setting). */
    fun wrap(base: Context, scale: Float = OnboardingPreferences(base).getAppTextScale()): Context {
        if (scale == 1f) return base
        val baseDensity = base.resources.configuration.densityDpi
        val config = Configuration(base.resources.configuration).apply {
            densityDpi = (baseDensity * scale).toInt().coerceAtLeast(72)
        }
        return base.createConfigurationContext(config)
    }
}
