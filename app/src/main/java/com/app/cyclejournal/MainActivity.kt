package com.app.cyclejournal

import android.os.Bundle
import android.os.Process
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.app.cyclejournal.billing.AdMobManager
import com.app.cyclejournal.billing.BillingManager
import com.app.cyclejournal.data.preferences.OnboardingPreferences
import com.app.cyclejournal.security.BiometricAuthHelper
import com.app.cyclejournal.security.SecurityPinManager
import com.app.cyclejournal.ui.CycleJournalApp
import com.app.cyclejournal.ui.home.CycleViewModel
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.domain.model.CycleStats
import com.app.cyclejournal.domain.model.FertilePrediction
import java.time.LocalDate
import com.app.cyclejournal.ui.security.PinLockScreen
import com.app.cyclejournal.ui.settings.SettingsViewModel
import com.app.cyclejournal.ui.theme.CycleJournalTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var pinManager: SecurityPinManager

    @Inject
    lateinit var prefs: OnboardingPreferences

    @Inject
    lateinit var billingManager: BillingManager

    @Inject
    lateinit var adMobManager: AdMobManager

    private val cycleViewModel: CycleViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private var lastBackgroundTimestamp = 0L
    private val isAppUnlocked = androidx.compose.runtime.mutableStateOf(false)
    private lateinit var biometricAuthHelper: BiometricAuthHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Android 12+ Splash Screen
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Enforce medical privacy: block screenshots and hide preview in Recents
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // Initialize Monetization & Privacy-compliant Ads
        billingManager.initialize()
        adMobManager.initialize()
        adMobManager.loadRewardedVideo()

        biometricAuthHelper = BiometricAuthHelper(this)

        // If PIN is not set yet (first-time install), start unlocked
        if (!pinManager.isPinSet() || !prefs.isOnboardingCompleted()) {
            isAppUnlocked.value = true
        }

        // ProcessLifecycleOwner 30-second background lock guard
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                lastBackgroundTimestamp = System.currentTimeMillis()
            }

            override fun onStart(owner: LifecycleOwner) {
                val elapsed = System.currentTimeMillis() - lastBackgroundTimestamp
                if (elapsed > 30_000L && pinManager.isPinSet() && prefs.isOnboardingCompleted()) {
                    isAppUnlocked.value = false
                }
            }
        })

        setContent {
            CycleJournalTheme {
                if (!isAppUnlocked.value && pinManager.isPinSet() && prefs.isOnboardingCompleted()) {
                    PinLockScreen(
                        onPinEntered = { enteredPin ->
                            val valid = pinManager.verifyPin(enteredPin)
                            if (valid) {
                                isAppUnlocked.value = true
                            }
                            valid
                        },
                        onBiometricRequested = {
                            if (biometricAuthHelper.canAuthenticate()) {
                                biometricAuthHelper.promptBiometric(
                                    onSuccess = { isAppUnlocked.value = true },
                                    onErrorOrFallback = {}
                                )
                            }
                        }
                    )
                } else {
                    val isProUser = billingManager.isProUser.value

                    val latestCycle by cycleViewModel.latestCycleFlow.collectAsState()
                    val fertilePrediction by cycleViewModel.fertilePredictionFlow.collectAsState()
                    val cycleStats by cycleViewModel.cycleStatsFlow.collectAsState()
                    val periodDates by cycleViewModel.periodDatesFlow.collectAsState()

                    CycleJournalApp(
                        onSharePdf = {
                            // Free-tier rewarded ad gate / Pro direct export
                            adMobManager.showRewardedVideo(
                                activity = this@MainActivity,
                                onRewardEarned = {
                                    cycleViewModel.exportAndSharePdfReport(this@MainActivity)
                                },
                                onDismissedOrFailed = {}
                            )
                        },
                        onExportCsv = {
                            settingsViewModel.exportAndShareCsv(this@MainActivity)
                        },
                        onBuyPro = {
                            billingManager.launchPurchaseFlow(this@MainActivity)
                        },
                        onBackupCloud = {
                            settingsViewModel.performManualBackup("0000")
                        },
                        onRestoreCloud = {
                            settingsViewModel.performManualRestore("0000")
                        },
                        onNukeData = {
                            settingsViewModel.wipeAllUserData {
                                Process.killProcess(Process.myPid())
                            }
                        },
                        isProUserActive = isProUser,
                        anonymousRecoveryKey = pinManager.getOrCreateAnonymousUserId(),
                        onSaveDailyLog = { cycleViewModel.saveDailyLog(it) },
                        latestCycle = latestCycle,
                        fertilePrediction = fertilePrediction,
                        cycleStats = cycleStats,
                        periodDates = periodDates
                    )
                }
            }
        }
    }
}
