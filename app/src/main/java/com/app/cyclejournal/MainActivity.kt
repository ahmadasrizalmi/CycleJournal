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
        super.onCreate(savedInstanceState)

        // FLAG_SECURE disabled to allow screenshot capture and preview

        // Initialize Monetization & Privacy-compliant Ads
        billingManager.initialize()
        adMobManager.initialize()
        adMobManager.loadRewardedVideo()

        biometricAuthHelper = BiometricAuthHelper(this)

        // If PIN is not set yet (first-time install), start unlocked
        if (!pinManager.isPinSet()) {
            isAppUnlocked.value = true
        }

        // ProcessLifecycleOwner 30-second background lock guard
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                lastBackgroundTimestamp = System.currentTimeMillis()
            }

            override fun onStart(owner: LifecycleOwner) {
                val elapsed = System.currentTimeMillis() - lastBackgroundTimestamp
                if (elapsed > 30_000L && pinManager.isPinSet()) {
                    isAppUnlocked.value = false
                }
            }
        })

        setContent {
            CycleJournalTheme {
                if (!isAppUnlocked.value && pinManager.isPinSet()) {
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
                    val isProUser by billingManager.isProUser.collectAsState()

                    val latestCycle by cycleViewModel.latestCycleFlow.collectAsState()
                    val fertilePrediction by cycleViewModel.fertilePredictionFlow.collectAsState()
                    val cycleStats by cycleViewModel.cycleStatsFlow.collectAsState()
                    val periodDates by cycleViewModel.periodDatesFlow.collectAsState()
                    val allLogs by cycleViewModel.allLogsFlow.collectAsState()
                    val completedCycles by cycleViewModel.completedCyclesFlow.collectAsState()
                    val anomalies by cycleViewModel.anomaliesFlow.collectAsState()
                    val downloadedReport by cycleViewModel.downloadedReport.collectAsState()
                    CycleJournalApp(
                        onSharePdf = {
                            cycleViewModel.exportAndSharePdfReport(this@MainActivity)
                        },
                        onExportCsv = {
                            settingsViewModel.exportAndDownloadCsv(this@MainActivity) { saveResult ->
                                cycleViewModel.setDownloadedReport(saveResult)
                            }
                        },
                        onBuyPro = {
                            billingManager.launchPurchaseFlow(this@MainActivity)
                        },
                        onBackupCloud = { pin ->
                            settingsViewModel.performManualBackup(pin)
                        },
                        onRestoreCloud = { pin ->
                            settingsViewModel.performManualRestore(pin)
                        },
                        onNukeData = {
                            settingsViewModel.wipeAllUserData {
                                Process.killProcess(Process.myPid())
                            }
                        },
                        isProUserActive = isProUser,
                        anonymousRecoveryKey = pinManager.getOrCreateAnonymousUserId(),
                        onSaveDailyLog = { cycleViewModel.saveDailyLog(it) },
                        onSavePin = { pinManager.savePin(it) },
                        latestCycle = latestCycle,
                        fertilePrediction = fertilePrediction,
                        cycleStats = cycleStats,
                        periodDates = periodDates,
                        allLogs = allLogs,
                        completedCycles = completedCycles,
                        anomalies = anomalies,
                        downloadedReport = downloadedReport,
                        onDismissDownloadDialog = { cycleViewModel.clearDownloadedReport() },
                        isPinSet = pinManager.isPinSet()
                    )
                }
            }
        }
    }
}
