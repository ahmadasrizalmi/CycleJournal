package com.app.cyclejournal.billing

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Controller for monetization entitlement, managing Lifetime Pro purchase verification
 * and hard ad-suppression kill-switch.
 */
class UserEntitlementManager(context: Context) {

    companion object {
        const val SKU_LIFETIME_PRO = "lifetime_pro_access"
        private const val PREFS_NAME = "billing_entitlement_prefs"
        private const val KEY_IS_PRO_USER = "is_pro_user"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isProUserFlow = MutableStateFlow(prefs.getBoolean(KEY_IS_PRO_USER, false))
    val isProUserFlow: StateFlow<Boolean> = _isProUserFlow

    fun setProUser(isPro: Boolean) {
        prefs.edit().putBoolean(KEY_IS_PRO_USER, isPro).apply()
        _isProUserFlow.value = isPro
    }

    fun shouldShowBannerAd(): Boolean = !isProUserFlow.value

    fun canExportPdfDirectly(): Boolean = isProUserFlow.value
}
