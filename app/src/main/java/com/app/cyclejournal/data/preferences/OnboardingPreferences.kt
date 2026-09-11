package com.app.cyclejournal.data.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages first-run onboarding completion state and baseline tracking metadata.
 */
class OnboardingPreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_IS_ONBOARDING_COMPLETED = "is_onboarding_completed"
        private const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
        private const val KEY_IS_PROMIL_MODE = "is_promil_mode"
        private const val KEY_APP_LANGUAGE = "app_language"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_IS_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean = true) {
        prefs.edit().putBoolean(KEY_IS_ONBOARDING_COMPLETED, completed).apply()
    }

    fun getLastSyncTimestamp(): Long {
        return prefs.getLong(KEY_LAST_SYNC_TIMESTAMP, 0L)
    }

    fun setLastSyncTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC_TIMESTAMP, timestamp).apply()
    }

    fun isPromilMode(): Boolean {
        return prefs.getBoolean(KEY_IS_PROMIL_MODE, false)
    }

    fun setPromilMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_PROMIL_MODE, enabled).apply()
    }

    fun getAppLanguage(): String {
        return prefs.getString(KEY_APP_LANGUAGE, "system") ?: "system"
    }

    fun setAppLanguage(language: String) {
        prefs.edit().putString(KEY_APP_LANGUAGE, language).apply()
    }
    fun clear() {
        prefs.edit().clear().commit()
    }
}
