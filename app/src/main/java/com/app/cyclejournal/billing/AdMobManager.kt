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
        const val ENABLE_ADS = false
        // Official Google AdMob Test Rewarded Ad Unit ID
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false

    fun initialize() {
        if (entitlementManager.isProUserFlow.value) return
        MobileAds.initialize(context) { }
    }

    /**
     * Builds privacy-compliant non-personalized ad request.
     */
    private fun buildNpaAdRequest(): AdRequest {
        return AdRequest.Builder()
            .addNetworkExtrasBundle(
                AdMobAdapter::class.java,
                bundleOf("npa" to "1") // Enforces Non-Personalized Ads under GDPR / Health Policy
            )
            .build()
    }

    /**
     * Pre-loads rewarded video ad for Free tier users exporting clinical reports.
     */
    fun loadRewardedVideo(onLoaded: (() -> Unit)? = null, onFailed: (() -> Unit)? = null) {
        if (!ENABLE_ADS || entitlementManager.isProUserFlow.value || isAdLoading) return

        isAdLoading = true
        RewardedAd.load(
            context,
            TEST_REWARDED_AD_UNIT_ID,
            buildNpaAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isAdLoading = false
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isAdLoading = false
                    onFailed?.invoke()
                }
            }
        )
    }

    /**
     * Shows the rewarded video ad to unlock a medical PDF report export.
     * If user is Pro or ad is unavailable, invokes [onRewardEarned] directly.
     */
    fun showRewardedVideo(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onDismissedOrFailed: () -> Unit
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
                    // Preload next ad
                    loadRewardedVideo()
                    onDismissedOrFailed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    // On display failure, gracefully allow the user to export
                    onRewardEarned()
                }
            }

            ad.show(activity) { _ ->
                onRewardEarned()
            }
        } else {
            // If ad fails to load or offline, never block the patient from their medical data
            onRewardEarned()
        }
    }
}
