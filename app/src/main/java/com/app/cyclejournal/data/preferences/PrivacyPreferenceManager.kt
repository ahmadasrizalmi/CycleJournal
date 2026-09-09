package com.app.cyclejournal.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages UI privacy preferences:
 * 1. Discreet Shield Mode: Sanitizes clinical terminology (e.g. "Menstruasi" -> "Fase 01")
 *    so curious onlookers cannot read intimate reproductive health labels.
 * 2. OLED Dark Mode: Independent dark/light appearance toggle.
 */
class PrivacyPreferenceManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "privacy_ui_prefs"
        private const val KEY_DISCREET_MODE = "is_discreet_mode_enabled"
        private const val KEY_DARK_MODE = "is_oled_dark_mode_enabled"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isDiscreetModeEnabled = MutableStateFlow(prefs.getBoolean(KEY_DISCREET_MODE, false))
    val isDiscreetModeEnabled: StateFlow<Boolean> = _isDiscreetModeEnabled

    private val _isDarkModeEnabled = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    val isDarkModeEnabled: StateFlow<Boolean> = _isDarkModeEnabled

    fun setDiscreetMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DISCREET_MODE, enabled).apply()
        _isDiscreetModeEnabled.value = enabled
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _isDarkModeEnabled.value = enabled
    }

    /**
     * Sanitizes clinical phase labels when Discreet Shield Mode is active.
     */
    fun sanitizePhaseLabel(rawPhase: String): String {
        if (!_isDiscreetModeEnabled.value) return rawPhase

        return when {
            rawPhase.contains("Menstruasi", ignoreCase = true) || rawPhase.contains("Period", ignoreCase = true) -> "Fase 01"
            rawPhase.contains("Folikuler", ignoreCase = true) || rawPhase.contains("Follicular", ignoreCase = true) -> "Fase 02"
            rawPhase.contains("Subur", ignoreCase = true) || rawPhase.contains("Fertile", ignoreCase = true) || rawPhase.contains("Ovulasi", ignoreCase = true) -> "Fase Tengah"
            rawPhase.contains("Luteal", ignoreCase = true) -> "Fase 03"
            else -> "Fase Aktif"
        }
    }

    /**
     * Sanitizes flow and mucus descriptions when Discreet Shield Mode is active.
     */
    fun sanitizeBiomarkerLabel(label: String): String {
        if (!_isDiscreetModeEnabled.value) return label
        return "Tercatat"
    }
}
