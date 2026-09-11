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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.widget.Toast
import com.app.cyclejournal.domain.manager.BackupFileInspection
import com.app.cyclejournal.domain.manager.LocalRestoreOutcome
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
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

                    var pendingInspectedBackup by remember { mutableStateOf<BackupFileInspection?>(null) }
                    var isRestorePinInputOpen by remember { mutableStateOf(false) }
                    var restorePinInput by remember { mutableStateOf("") }

                    val backupFilePicker = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument()
                    ) { uri ->
                        if (uri != null) {
                            settingsViewModel.restoreFromBackupFile(
                                context = this@MainActivity,
                                 fileUri = uri,
                                onNeedsPin = { inspection ->
                                    pendingInspectedBackup = inspection
                                    restorePinInput = ""
                                    isRestorePinInputOpen = true
                                },
                                onComplete = { outcome ->
                                    when (outcome) {
                                        is LocalRestoreOutcome.Success -> {
                                            Toast.makeText(this@MainActivity, "Berhasil memulihkan ${outcome.logsRestored} catatan harian & ${outcome.cyclesRestored} siklus!", Toast.LENGTH_LONG).show()
                                        }
                                        is LocalRestoreOutcome.InvalidPin -> {
                                            Toast.makeText(this@MainActivity, "PIN salah. Gagal mendekripsi berkas cadangan.", Toast.LENGTH_LONG).show()
                                        }
                                        is LocalRestoreOutcome.InvalidFileFormat -> {
                                            Toast.makeText(this@MainActivity, "Format berkas tidak valid atau bukan cadangan CycleJournal.", Toast.LENGTH_LONG).show()
                                        }
                                        is LocalRestoreOutcome.Error -> {
                                            Toast.makeText(this@MainActivity, outcome.message, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            )
                        }
                    }

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
                        onBackupLocal = { isEncrypted, pin ->
                            settingsViewModel.createLocalBackup(this@MainActivity, isEncrypted, pin) { saveResult ->
                                cycleViewModel.setDownloadedReport(saveResult)
                            }
                        },
                        onRestoreLocal = {
                            backupFilePicker.launch(arrayOf("*/*"))
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
                        isPinSet = pinManager.isPinSet(),
                        isPromilModeInitial = prefs.isPromilMode(),
                        onTogglePromilMode = { prefs.setPromilMode(it) }
                    )
                    if (isRestorePinInputOpen && pendingInspectedBackup != null) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { isRestorePinInputOpen = false },
                            title = {
                                androidx.compose.material3.Text("Masukkan PIN Cadangan", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            },
                            text = {
                                androidx.compose.foundation.layout.Column {
                                    androidx.compose.material3.Text("Berkas cadangan ini dienkripsi dengan PIN. Masukkan 4 digit angka pembuka:")
                                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                                    androidx.compose.material3.OutlinedTextField(
                                        value = restorePinInput,
                                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) restorePinInput = it },
                                        placeholder = { androidx.compose.material3.Text("4 Digit Angka") },
                                        singleLine = true
                                    )
                                }
                            },
                            confirmButton = {
                                androidx.compose.material3.TextButton(
                                    enabled = restorePinInput.length == 4,
                                    onClick = {
                                        val pin = restorePinInput
                                        val inspection = pendingInspectedBackup!!
                                        isRestorePinInputOpen = false
                                        settingsViewModel.restoreInspectedBackup(inspection, pin) { outcome ->
                                            when (outcome) {
                                                is LocalRestoreOutcome.Success -> {
                                                    Toast.makeText(this@MainActivity, "Berhasil memulihkan ${outcome.logsRestored} catatan harian & ${outcome.cyclesRestored} siklus!", Toast.LENGTH_LONG).show()
                                                }
                                                is LocalRestoreOutcome.InvalidPin -> {
                                                    Toast.makeText(this@MainActivity, "PIN salah. Gagal mendekripsi berkas cadangan.", Toast.LENGTH_LONG).show()
                                                }
                                                is LocalRestoreOutcome.Error -> {
                                                    Toast.makeText(this@MainActivity, outcome.message, Toast.LENGTH_LONG).show()
                                                }
                                                else -> {}
                                            }
                                        }
                                    }
                                ) {
                                    androidx.compose.material3.Text("Buka & Pulihkan", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                androidx.compose.material3.TextButton(onClick = { isRestorePinInputOpen = false }) {
                                    androidx.compose.material3.Text("Batal")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
