package com.app.cyclejournal.data.preferences

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Resolves the app-wide language preference into a locale-configured [Context] for code that runs
 * outside the Compose tree (application startup, broadcast receivers, view models).
 *
 * The "id" locale is intentionally built from the legacy constructor: Android's resource compiler
 * stores the Indonesian qualifier in both modern (`values-id`) and legacy (`values-in`) form, and
 * older platforms only match the latter.
 */
object AppLocale {

    /** Returns a context whose resources resolve in the stored app language, or [base] for "system". */
    fun wrap(base: Context, language: String = OnboardingPreferences(base).getAppLanguage()): Context {
        val locale = localeFor(language) ?: return base
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        return base.createConfigurationContext(config)
    }

    /** Maps a stored preference value ("system", "id", "en") to a [Locale], or null for system default. */
    fun localeFor(language: String): Locale? = when (language) {
        "id" -> Locale("id", "ID")
        "en" -> Locale.ENGLISH
        else -> null
    }
}
