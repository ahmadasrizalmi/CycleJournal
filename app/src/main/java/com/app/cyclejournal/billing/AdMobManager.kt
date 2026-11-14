package com.app.cyclejournal.billing

import android.app.Activity
import android.content.Context
import androidx.core.os.bundleOf
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AdMob Non-Personalized Mediation Manager.
 *
 * Privacy Guarantees:
 * 1. Strictly passes `npa: "1"` (Non-Personalized Ads) in every AdRequest bundle to disable GAID and behavioral profiling.
 * 2. Hard Kill-Switch: If user is Pro (`entitlementManager.isProUserFlow.value == true`), completely bypasses
 *    and disables all ad requests, loading, and callbacks.
 */
@Singleton
class AdMobManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val entitlementManager: UserEntitlementManager
) {

    companion object {
        const val ENABLE_ADS = true

        // Production AdMob Unit IDs
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3127867508262261/6457931160"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3127867508262261/4905176343"
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-3127867508262261/5163041693"

        // Interstitial cooldown: 4 hours between full-screen ads on log save
        private const val INTERSTITIAL_COOLDOWN_MS = 4 * 60 * 60 * 1000L
        private const val PREFS_NAME = "admob_cooldown_prefs"
        private const val KEY_LAST_INTERSTITIAL_TIME = "last_interstitial_timestamp"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    fun isProUser(): Boolean = entitlementManager.isProUserFlow.value

    fun initialize() {
        if (entitlementManager.isProUserFlow.value || !ENABLE_ADS) return
        MobileAds.initialize(context) { }
    }

    /**
     * Builds privacy-compliant non-personalized ad request.
     */
    fun buildNpaAdRequest(): AdRequest {
        return AdRequest.Builder()
            .addNetworkExtrasBundle(
                AdMobAdapter::class.java,
                bundleOf("npa" to "1") // Enforces Non-Personalized Ads under GDPR / Health Policy
            )
            .build()
    }

    // =========================================================================
    // 1. REWARDED VIDEO ADS (Clinical PDF Export)
    // =========================================================================

    fun loadRewardedVideo(onLoaded: (() -> Unit)? = null, onFailed: (() -> Unit)? = null) {
        if (!ENABLE_ADS || entitlementManager.isProUserFlow.value || isRewardedLoading) return

        isRewardedLoading = true
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            buildNpaAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    onFailed?.invoke()
                }
            }
        )
    }

    fun showRewardedVideo(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onDismissedOrFailed: () -> Unit = {}
    ) {
        // Graceful ad bypass: if ads are disabled or user is Pro, unlock export immediately
        if (!ENABLE_ADS || entitlementManager.isProUserFlow.value) {
            onRewardEarned()
            return
        }

        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedVideo()
                    onDismissedOrFailed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewardedVideo()
                    onRewardEarned()
                }
            }

            ad.show(activity) { _ ->
                onRewardEarned()
            }
        } else {
            // If ad fails to load or offline, never block the patient from their medical data
            loadRewardedVideo()
            onRewardEarned()
        }
    }

    // =========================================================================
    // 2. INTERSTITIAL ADS (Save Daily Log with 4-hour Cooldown)
    // =========================================================================

    fun loadInterstitial(onLoaded: (() -> Unit)? = null, onFailed: (() -> Unit)? = null) {
        if (!ENABLE_ADS || entitlementManager.isProUserFlow.value || isInterstitialLoading || interstitialAd != null) return

        isInterstitialLoading = true
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            buildNpaAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    onFailed?.invoke()
                }
            }
        )
    }

    fun showInterstitial(
        activity: Activity,
        onFinished: () -> Unit = {}
    ) {
        if (!ENABLE_ADS || entitlementManager.isProUserFlow.value) {
            onFinished()
            return
        }

        val now = System.currentTimeMillis()
        val lastShown = prefs.getLong(KEY_LAST_INTERSTITIAL_TIME, 0L)
        if (now - lastShown < INTERSTITIAL_COOLDOWN_MS) {
            // Still in cooldown period; do not annoy the user
            onFinished()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    prefs.edit().putLong(KEY_LAST_INTERSTITIAL_TIME, System.currentTimeMillis()).apply()
                    loadInterstitial()
                    onFinished()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitial()
                    onFinished()
                }
            }
            ad.show(activity)
        } else {
            loadInterstitial()
            onFinished()
        }
    }
}
