package com.app.cyclejournal.ui
import com.app.cyclejournal.export.pdf.PdfShareHelper

import androidx.compose.animation.*
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import com.app.cyclejournal.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import com.app.cyclejournal.domain.model.AnomalyAlert
import com.app.cyclejournal.domain.model.CycleStats
import com.app.cyclejournal.domain.model.FertilePrediction
import com.app.cyclejournal.ui.theme.*
import com.app.cyclejournal.ui.components.BottomNavBar
import com.app.cyclejournal.ui.components.BrandBand
import com.app.cyclejournal.ui.components.CycleDetailSheetV4
import com.app.cyclejournal.ui.components.LogSheetV4
import com.app.cyclejournal.ui.screens.AnalysisV4Screen
import com.app.cyclejournal.ui.screens.CalendarV4Screen
import com.app.cyclejournal.ui.screens.HomeV4Screen
import com.app.cyclejournal.ui.screens.SettingsV4Screen
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class AppScreen {
    DASHBOARD, CALENDAR, REPORT, SETTINGS
}

/** The pill shows the language actually in use - never an "AUTO" placeholder. */
private fun activeLanguageCode(appLanguage: String): String = when (appLanguage) {
    "id" -> "ID"
    "en" -> "EN"
    else -> when (Locale.getDefault().language.lowercase()) {
        // Java reports Indonesian as the legacy "in" code on older devices.
        "in", "id" -> "ID"
        else -> "EN"
    }
}

private fun LocalDate.monthName(): String =
    format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()))

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun CycleJournalApp(
    onSharePdf: (() -> Unit)? = null,
    onExportCsv: (() -> Unit)? = null,
    onBuyPro: (() -> Unit)? = null,
    onBackupLocal: ((Boolean, String?) -> Unit)? = null,
    onRestoreLocal: (() -> Unit)? = null,
    onNukeData: ((String) -> Boolean)? = null,
    isProUserActive: Boolean = false,
    anonymousRecoveryKey: String = "px-7f9a2b1c4e0d",
    onSaveDailyLog: (DailyLogEntity) -> Unit = {},
    latestCycle: CycleEntity? = null,
    fertilePrediction: FertilePrediction? = null,
    cycleStats: CycleStats? = null,
    periodDates: Set<LocalDate> = emptySet(),
    allLogs: List<DailyLogEntity> = emptyList(),
    completedCycles: List<CycleEntity> = emptyList(),
    anomalies: List<AnomalyAlert> = emptyList(),
    isPinSet: Boolean = false,
    onSavePin: (String) -> Unit = {},
    isPromilModeInitial: Boolean = false,
    onTogglePromilMode: (Boolean) -> Unit = {},
    downloadedReport: PdfShareHelper.SaveResult? = null,
    onDismissDownloadDialog: () -> Unit = {},
    appLanguage: String = "system",
    onLanguageChanged: (String) -> Unit = {},
    appTextScale: Float = 1f,
    onTextScaleChanged: (Float) -> Unit = {},
    isFingerprintUnlockEnabled: Boolean = false,
    onFingerprintUnlockChanged: (Boolean) -> Unit = {},
    isPeriodReminderEnabled: Boolean = true,
    onPeriodReminderChanged: (Boolean) -> Unit = {},
    isBbtReminderEnabled: Boolean = true,
    onBbtReminderChanged: (Boolean) -> Unit = {},
    appVersionName: String = "",
    isDarkModeInitial: Boolean = false,
    onDarkModeChanged: (Boolean) -> Unit = {}
) {
    val resources = LocalContext.current.resources
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var isDarkMode by remember(isDarkModeInitial) { mutableStateOf(isDarkModeInitial) }
    var isDiscreetMode by remember { mutableStateOf(false) }
    var isPinModalOpen by remember { mutableStateOf(false) }
    var isLogModalOpen by remember { mutableStateOf(false) }
    var detailCycle by remember { mutableStateOf<CycleEntity?>(null) }
    var logModalDate by remember { mutableStateOf(LocalDate.now()) }
    var isProLicenseActive by remember(isProUserActive) { mutableStateOf(isProUserActive) }
    var isPinConfigured by remember(isPinSet) { mutableStateOf(isPinSet) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var isPromilMode by remember(isPromilModeInitial) { mutableStateOf(isPromilModeInitial) }

    val today = remember { LocalDate.now() }
    var selectedCalendarDate by remember { mutableStateOf(today) }

    val showToast: (String) -> Unit = { message ->
        toastMessage = message
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2500)
            toastMessage = null
        }
    }

    // The v4 surfaces carry their own palette, so the page behind them is always `canvas-soft`.
    val backgroundColor = CanvasSoft

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            // Exposes testTag values as resource ids so the screenshot flows can select screens
            // and controls by identifier instead of locale-specific text.
            .semantics { testTagsAsResourceId = true }
    ) {
        Scaffold(
            containerColor = backgroundColor,
            topBar = {
                BrandBand(
                    languageCode = activeLanguageCode(appLanguage),
                    isDarkMode = isDarkMode,
                    onThemeToggle = {
                        isDarkMode = !isDarkMode
                        onDarkModeChanged(isDarkMode)
                        showToast(if (isDarkMode) resources.getString(R.string.app_mode_dark_active) else resources.getString(R.string.app_mode_light_active))
                    },
                    onDiscreetToggle = {
                        isDiscreetMode = !isDiscreetMode
                        showToast(if (isDiscreetMode) resources.getString(R.string.app_mode_discreet_active) else resources.getString(R.string.app_mode_standard_active))
                    },
                    onLanguageClick = {
                        val next = if (activeLanguageCode(appLanguage) == "ID") "en" else "id"
                        onLanguageChanged(next)
                    }
                )
            },
            bottomBar = {
                BottomNavBar(
                    selectedIndex = currentScreen.ordinal,
                    labels = listOf(
                        stringResource(R.string.v4_nav_home),
                        stringResource(R.string.v4_nav_calendar),
                        stringResource(R.string.v4_nav_analysis),
                        stringResource(R.string.v4_nav_settings)
                    ),
                    onSelect = { index -> currentScreen = AppScreen.entries[index] },
                    onFabClick = {
                        logModalDate = if (currentScreen == AppScreen.CALENDAR) selectedCalendarDate else today
                        isLogModalOpen = true
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("screen_" + currentScreen.name.lowercase())
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> HomeV4Screen(
                        isDiscreet = isDiscreetMode,
                        isPromilMode = isPromilMode,
                        periodDates = periodDates,
                        latestCycle = latestCycle,
                        fertilePrediction = fertilePrediction,
                        cycleStats = cycleStats,
                        todayLog = allLogs.find { it.date == today },
                        today = today,
                        onOpenLog = { date ->
                            logModalDate = date
                            isLogModalOpen = true
                        },
                        onMarkPeriodEnded = {
                            // Closing a period is recorded the way the engine reads it: the day's
                            // flow goes back to NONE, so the aggregator ends the cycle here.
                            val existing = allLogs.find { it.date == today }
                            onSaveDailyLog(
                                existing?.copy(flow = FlowIntensity.NONE)
                                    ?: DailyLogEntity(date = today, flow = FlowIntensity.NONE)
                            )
                            showToast(resources.getString(R.string.v4_home_period_ended_toast))
                        },
                        onOpenCalendar = { currentScreen = AppScreen.CALENDAR }
                    )
                    AppScreen.CALENDAR -> CalendarV4Screen(
                        isDiscreet = isDiscreetMode,
                        periodDates = periodDates,
                        fertilePrediction = fertilePrediction,
                        allLogs = allLogs,
                        latestCycle = latestCycle,
                        completedCycles = completedCycles,
                        cycleStats = cycleStats,
                        selectedCalendarDate = selectedCalendarDate,
                        onSelectDate = { selectedCalendarDate = it },
                        onOpenLog = { date ->
                            logModalDate = date
                            isLogModalOpen = true
                        },
                        onToast = showToast
                    )
                    AppScreen.REPORT -> AnalysisV4Screen(
                        isPro = isProLicenseActive,
                        cycleStats = cycleStats,
                        completedCycles = completedCycles,
                        anomalies = anomalies,
                        allLogs = allLogs,
                        latestCycle = latestCycle,
                        fertilePrediction = fertilePrediction,
                        onSharePdf = {
                            onSharePdf?.invoke() ?: run {
                                showToast(resources.getString(R.string.report_export_preparing_summary))
                            }
                        },
                        onExportCsv = {
                            onExportCsv?.invoke() ?: showToast(resources.getString(R.string.app_export_exporting_raw_csv))
                        },
                        onBuyPro = {
                            onBuyPro?.invoke() ?: run {
                                isProLicenseActive = true
                                showToast(resources.getString(R.string.app_toast_pro_activated))
                            }
                        },
                        onNavigateToCalendar = { date ->
                            selectedCalendarDate = date
                            currentScreen = AppScreen.CALENDAR
                            showToast(resources.getString(R.string.app_toast_opening_month_in_calendar, date.monthName()))
                        },
                        onCycleClick = { cycle -> detailCycle = cycle },
                        onToast = showToast
                    )
                    AppScreen.SETTINGS -> SettingsV4Screen(
                        isDarkMode = isDarkMode,
                        isDiscreet = isDiscreetMode,
                        isPro = isProLicenseActive,
                        isPromilMode = isPromilMode,
                        onTogglePromilMode = {
                            isPromilMode = it
                            onTogglePromilMode(it)
                        },
                        appLanguage = appLanguage,
                        onLanguageChanged = onLanguageChanged,
                        appTextScale = appTextScale,
                        onTextScaleChanged = onTextScaleChanged,
                        isPinConfigured = isPinConfigured,
                        anonymousRecoveryKey = anonymousRecoveryKey,
                        onToggleDiscreet = { isDiscreetMode = !isDiscreetMode },
                        onToggleDark = {
                            isDarkMode = !isDarkMode
                            onDarkModeChanged(isDarkMode)
                        },
                        isFingerprintUnlockEnabled = isFingerprintUnlockEnabled,
                        onFingerprintUnlockChanged = onFingerprintUnlockChanged,
                        isPeriodReminderEnabled = isPeriodReminderEnabled,
                        onPeriodReminderChanged = onPeriodReminderChanged,
                        isBbtReminderEnabled = isBbtReminderEnabled,
                        onBbtReminderChanged = onBbtReminderChanged,
                        appVersionName = appVersionName,
                        onOpenPin = { isPinModalOpen = true },
                        onBuyPro = {
                            onBuyPro?.invoke() ?: run {
                                isProLicenseActive = true
                                showToast(resources.getString(R.string.app_toast_upgrade_success))
                            }
                        },
                        onCopyRecoveryKey = {
                            showToast(resources.getString(R.string.app_toast_recovery_key_copied, anonymousRecoveryKey))
                        },
                        onBackupLocal = onBackupLocal,
                        onRestoreLocal = onRestoreLocal,
                        onNukeData = { pin -> onNukeData?.invoke(pin) ?: false },
                        onToast = showToast
                    )
                }
            }
        }

        // Floating Toast Snack
        toastMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp)
                    .shadow(16.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate900.copy(alpha = 0.92f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = msg,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // PIN Setup Modal Dialog
        if (isPinModalOpen) {
            PinSetupDialog(
                isDarkMode = isDarkMode,
                onDismiss = { isPinModalOpen = false },
                onSave = { enteredPin ->
                    onSavePin(enteredPin)
                    isPinConfigured = true
                    isPinModalOpen = false
                    showToast(resources.getString(R.string.app_toast_pin_enabled))
                }
            )
        }

        // Cycle detail, opened from a row of the Analysis timeline
        detailCycle?.let { cycle ->
            CycleDetailSheetV4(
                cycle = cycle,
                allLogs = allLogs,
                onDismiss = { detailCycle = null },
                onOpenCalendar = { date ->
                    detailCycle = null
                    selectedCalendarDate = date
                    currentScreen = AppScreen.CALENDAR
                }
            )
        }

        // Daily Log Bottom Sheet (pre-filled with real existing log if available)
        if (isLogModalOpen) {
            val existingLog = allLogs.find { it.date == logModalDate }
            LogSheetV4(
                targetDate = logModalDate,
                initialLog = existingLog,
                onDismiss = { isLogModalOpen = false },
                onSave = { logEntity ->
                    onSaveDailyLog(logEntity)
                    isLogModalOpen = false
                    val isBleed = logEntity.flow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY)
                    showToast(if (isBleed) resources.getString(R.string.app_toast_period_log_saved) else resources.getString(R.string.app_toast_daily_log_saved))
                }
            )
        }
        if (downloadedReport != null) {
            val res = downloadedReport
            val context = LocalContext.current
            val isCsv = res.fileName.endsWith(".csv", ignoreCase = true)
            val isBackup = res.fileName.endsWith(".cjbackup", ignoreCase = true)
            val fileTypeTitle = when {
                isBackup -> stringResource(R.string.app_backup_file_label)
                isCsv -> stringResource(R.string.app_csv_data_label)
                else -> stringResource(R.string.app_cycle_summary_pdf_label)
            }
            val openButtonLabel = when {
                isBackup -> stringResource(R.string.app_share_to_drive_chat)
                isCsv -> stringResource(R.string.app_open_csv)
                else -> stringResource(R.string.app_open_pdf)
            }

            AlertDialog(
                onDismissRequest = onDismissDownloadDialog,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.app_toast_download_complete), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.app_download_saved_message, fileTypeTitle),
                            fontSize = 14.sp,
                            color = if (isDarkMode) Slate400 else Slate600
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDarkMode) DarkBackground else Slate100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Download/CycleJournal/${res.fileName}",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = Coral600,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isBackup) {
                                com.app.cyclejournal.export.csv.CsvExportHelper.shareCsv(context, res, resources.getString(R.string.app_backup_file_name))
                            } else if (isCsv) {
                                com.app.cyclejournal.export.csv.CsvExportHelper.openCsv(context, res)
                            } else {
                                PdfShareHelper.openPdf(context, res)
                            }
                            onDismissDownloadDialog()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Coral500),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(openButtonLabel, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                if (isCsv) {
                                    com.app.cyclejournal.export.csv.CsvExportHelper.shareCsv(context, res)
                                } else {
                                    PdfShareHelper.sharePdf(context, res.localFile)
                                }
                                onDismissDownloadDialog()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.app_share))
                        }
                        TextButton(onClick = onDismissDownloadDialog) {
                            Text(stringResource(R.string.app_close))
                        }
                    }
                }
            )
        }
    }
}

// 4. SPOG MEDICAL REPORT SCREEN: Wired to Room Database completedCycles and FIGO calculations

@Composable
fun PinSetupDialog(
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val textPrimary = if (isDarkMode) Color.White else Slate900

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = cardBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFFFF1F2)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Coral600)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(stringResource(R.string.pin_setup_title), fontSize = 15.sp, fontWeight = FontWeight.Black, color = textPrimary)
                Text(stringResource(R.string.pin_setup_subtitle), fontSize = 13.sp, color = Slate400)

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (i in 0..3) {
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(if (i < pin.length) Coral500 else Slate200)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                val digits = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "DEL")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0..3) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (col in 0..2) {
                                val item = digits[row * 3 + col]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (item.isNotEmpty()) (if (isDarkMode) DarkBackground else Slate100) else Color.Transparent)
                                        .clickable(enabled = item.isNotEmpty()) {
                                            if (item == "DEL") {
                                                if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                            } else if (pin.length < 4) {
                                                pin += item
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item == "DEL") {
                                        Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = null, tint = textPrimary, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text(item, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onSave(pin) },
                    enabled = pin.length == 4,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral500)
                ) {
                    Text(stringResource(R.string.pin_setup_save), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
