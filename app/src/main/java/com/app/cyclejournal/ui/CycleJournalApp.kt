package com.app.cyclejournal.ui
import com.app.cyclejournal.export.pdf.PdfShareHelper
import com.app.cyclejournal.ui.home.CycleFilter
import com.app.cyclejournal.ui.report.CycleHistoryRow

import androidx.compose.animation.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import android.content.Context
import android.content.res.Resources
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.app.cyclejournal.R
import com.app.cyclejournal.BuildConfig
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.app.cyclejournal.data.local.entity.CervicalMucusType
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import com.app.cyclejournal.domain.model.AnomalyAlert
import com.app.cyclejournal.domain.model.CycleStats
import com.app.cyclejournal.domain.model.FertilePrediction
import com.app.cyclejournal.ui.theme.*
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class AppScreen {
    DASHBOARD, CALENDAR, REPORT, SETTINGS
}

enum class CyclePhase {
    MENSTRUATION, FOLLICULAR, FERTILE, OVULATION, LUTEAL
}

private enum class NukeStage { Closed, Confirm, Wiping }

/** Typed proof of intent; the localized hint (nuke_confirm_hint) mirrors one of these words. */
private val NUKE_KEYWORDS = listOf("HAPUS", "DELETE")

data class DayStripItem(
    val dayOfMonth: Int,
    val dayName: String,
    val title: String,
    val bbt: String,
    val pain: String,
    val mucus: String,
    val phase: CyclePhase,
    val dotColor: Color,
    val date: LocalDate,
    val log: DailyLogEntity? = null
)

private fun LocalDate.monthName(): String =
    format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()))

private fun LocalDate.monthAbbr(): String =
    format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))

private fun mucusLabelFor(type: CervicalMucusType, resources: Resources): String = when (type) {
    CervicalMucusType.NONE -> resources.getString(R.string.mucus_none)
    CervicalMucusType.DRY -> resources.getString(R.string.mucus_dry)
    CervicalMucusType.STICKY -> resources.getString(R.string.mucus_sticky)
    CervicalMucusType.CREAMY -> resources.getString(R.string.mucus_creamy)
    CervicalMucusType.WATERY -> resources.getString(R.string.mucus_watery)
    CervicalMucusType.EGG_WHITE -> resources.getString(R.string.mucus_egg_white)
}

@Composable
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
    onTextScaleChanged: (Float) -> Unit = {}
) {
    val resources = LocalContext.current.resources
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var isDarkMode by remember { mutableStateOf(false) }
    var isDiscreetMode by remember { mutableStateOf(false) }
    var isPinModalOpen by remember { mutableStateOf(false) }
    var isLogModalOpen by remember { mutableStateOf(false) }
    var logModalDate by remember { mutableStateOf(LocalDate.now()) }
    var isProLicenseActive by remember(isProUserActive) { mutableStateOf(isProUserActive) }
    var isPinConfigured by remember(isPinSet) { mutableStateOf(isPinSet) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var isPromilMode by remember(isPromilModeInitial) { mutableStateOf(isPromilModeInitial) }

    val today = remember { LocalDate.now() }
    var selectedCalendarDate by remember { mutableStateOf(today) }
    // Dynamically build week days from real backend data (Monday to Sunday around today)
    val weekDays = remember(today, allLogs, periodDates, fertilePrediction, latestCycle) {
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        (0..6).map { offset ->
            val date = monday.plusDays(offset.toLong())
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val log = allLogs.find { it.date == date }
            val bbt = log?.basalBodyTempCelsius?.let { String.format(Locale.US, "%.2f °C", it) } ?: "-- °C"
            val pain = log?.let {
                if (it.painVasScore > 0) "VAS ${it.painVasScore}" else resources.getString(R.string.app_pain_free)
            } ?: resources.getString(R.string.app_pain_free)
            val mucus = log?.cervicalMucus?.let { mucusLabelFor(it, resources) } ?: resources.getString(R.string.mucus_dry)

            val periodDays = (latestCycle?.periodDurationDays ?: 5).toLong().coerceAtLeast(1L)
            val isHaid = date in periodDates || (latestCycle != null && !date.isBefore(latestCycle.startDate) && date.isBefore(latestCycle.startDate.plusDays(periodDays)))
            val isOvulation = fertilePrediction != null && date == fertilePrediction.predictedOvulationDate
            val isFertile = fertilePrediction != null && !date.isBefore(fertilePrediction.fertileWindowStart) && !date.isAfter(fertilePrediction.fertileWindowEnd)

            val (phase, dotColor, title) = when {
                isHaid -> Triple(CyclePhase.MENSTRUATION, Color(0xFFFB7185), resources.getString(R.string.app_phase_menstrual))
                isOvulation -> Triple(CyclePhase.OVULATION, MedicalTeal, resources.getString(R.string.app_phase_ovulation_peak))
                isFertile -> Triple(CyclePhase.FERTILE, Coral500, resources.getString(R.string.app_phase_fertile_window))
                else -> Triple(CyclePhase.FOLLICULAR, Color(0xFFCBD5E1), resources.getString(R.string.app_phase_follicular))
            }

            DayStripItem(
                dayOfMonth = date.dayOfMonth,
                dayName = dayName,
                title = title,
                bbt = bbt,
                pain = pain,
                mucus = mucus,
                phase = phase,
                dotColor = dotColor,
                date = date,
                log = log
            )
        }
    }

    var selectedDay by remember(weekDays) {
        mutableStateOf(weekDays.find { it.date == today } ?: weekDays.first())
    }

    val showToast: (String) -> Unit = { message ->
        toastMessage = message
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2500)
            toastMessage = null
        }
    }

    val backgroundColor = if (isDarkMode) DarkBackground else LightBackground

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Scaffold(
            containerColor = backgroundColor,
            topBar = {
                AppHeader(
                    isDarkMode = isDarkMode,
                    isDiscreetMode = isDiscreetMode,
                    onToggleDiscreet = {
                        isDiscreetMode = !isDiscreetMode
                        showToast(if (isDiscreetMode) resources.getString(R.string.app_mode_discreet_active) else resources.getString(R.string.app_mode_standard_active))
                    },
                    onToggleDarkMode = {
                        isDarkMode = !isDarkMode
                        showToast(if (isDarkMode) resources.getString(R.string.app_mode_dark_active) else resources.getString(R.string.app_mode_light_active))
                    },
                    onOpenSettings = { currentScreen = AppScreen.SETTINGS }
                )
            },
            bottomBar = {
                AppBottomNavigation(
                    currentScreen = currentScreen,
                    isDarkMode = isDarkMode,
                    onSelect = { currentScreen = it },
                    onOpenFab = {
                        logModalDate = if (currentScreen == AppScreen.CALENDAR) selectedCalendarDate else selectedDay.date
                        isLogModalOpen = true
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreenView(
                        isDarkMode = isDarkMode,
                        isDiscreet = isDiscreetMode,
                        weekDays = weekDays,
                        selectedDay = selectedDay,
                        latestCycle = latestCycle,
                        fertilePrediction = fertilePrediction,
                        cycleStats = cycleStats,
                        allLogs = allLogs,
                        isPromilMode = isPromilMode,
                        onSelectDay = {
                            selectedDay = it
                            showToast(resources.getString(R.string.app_toast_showing_date, it.dayOfMonth, it.date.monthName(), it.date.year))
                        },
                        onOpenCalendar = { currentScreen = AppScreen.CALENDAR },
                        onOpenLog = {
                            logModalDate = selectedDay.date
                            isLogModalOpen = true
                        }
                    )
                    AppScreen.CALENDAR -> CalendarScreenView(
                        isDarkMode = isDarkMode,
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
                    AppScreen.REPORT -> SpOgReportScreenView(
                        isDarkMode = isDarkMode,
                        isPro = isProLicenseActive,
                        anonymousRecoveryKey = anonymousRecoveryKey,
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
                        onToast = showToast
                    )
                    AppScreen.SETTINGS -> SettingsScreenView(
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
                        onToggleDark = { isDarkMode = !isDarkMode },
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

        // Daily Log Bottom Sheet (pre-filled with real existing log if available)
        if (isLogModalOpen) {
            val existingLog = allLogs.find { it.date == logModalDate }
            DailyLogBottomSheet(
                isDarkMode = isDarkMode,
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

@Composable
fun AppHeader(
    isDarkMode: Boolean,
    isDiscreetMode: Boolean,
    onToggleDiscreet: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.8f)
    val textPrimary = if (isDarkMode) Color.White else Slate900

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_cyclejournal_logo),
                contentDescription = stringResource(R.string.header_logo_content_desc),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                if (isDiscreetMode) {
                    Text(
                        text = stringResource(R.string.header_discreet_mode_label),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                }
                Text(
                    text = if (isDiscreetMode) stringResource(R.string.header_discreet_app_name) else stringResource(R.string.header_app_name),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textPrimary
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HeaderSquareButton(
                icon = if (isDiscreetMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                tint = if (isDiscreetMode) Coral600 else Slate600,
                cardBg = cardBg,
                borderCol = borderCol,
                onClick = onToggleDiscreet
            )
            HeaderSquareButton(
                icon = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.NightlightRound,
                tint = if (isDarkMode) Color(0xFFFBBF24) else Slate600,
                cardBg = cardBg,
                borderCol = borderCol,
                onClick = onToggleDarkMode
            )
            HeaderSquareButton(
                icon = Icons.Default.Tune,
                tint = Slate600,
                cardBg = cardBg,
                borderCol = borderCol,
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
fun HeaderSquareButton(
    icon: ImageVector,
    tint: Color,
    cardBg: Color,
    borderCol: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderCol),
        modifier = Modifier.size(34.dp),
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        }
    }
}

// 2. DASHBOARD VIEW: Fully wired to backend, clean Hero card, dynamic VAS alert
@Composable
fun DashboardScreenView(
    isDarkMode: Boolean,
    isDiscreet: Boolean,
    weekDays: List<DayStripItem>,
    selectedDay: DayStripItem,
    latestCycle: CycleEntity?,
    fertilePrediction: FertilePrediction?,
    cycleStats: CycleStats?,
    allLogs: List<DailyLogEntity>,
    isPromilMode: Boolean = false,
    onSelectDay: (DayStripItem) -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenLog: () -> Unit
) {
    val resources = LocalContext.current.resources
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorder else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500

    val today = remember { LocalDate.now() }
    val hasActiveCycle = latestCycle != null

    val currentCycleDay = latestCycle?.let {
        (ChronoUnit.DAYS.between(it.startDate, today) + 1L).coerceAtLeast(1L)
    }

    val effectiveAvgCycleDays = cycleStats?.averageLength ?: 28.0
    val stdDev = cycleStats?.standardDeviation
    val isBleedingToday = selectedDay.phase == CyclePhase.MENSTRUATION
    val isFertileToday = selectedDay.phase == CyclePhase.FERTILE || selectedDay.phase == CyclePhase.OVULATION

    val predictedNextPeriod = fertilePrediction?.predictedNextPeriodDate
        ?: (latestCycle?.startDate?.plusDays(effectiveAvgCycleDays.toLong()))

    val daysUntilNextPeriod = predictedNextPeriod?.let {
        ChronoUnit.DAYS.between(today, it).toInt()
    }

    val nextPeriodValueStr = predictedNextPeriod?.let {
        val monthStr = it.monthAbbr()
        "${it.dayOfMonth} $monthStr"
    } ?: "--"

    val nextPeriodSubtext = when {
        !hasActiveCycle -> stringResource(R.string.dash_next_period_log_first)
        daysUntilNextPeriod == null -> stringResource(R.string.dash_next_period_no_prediction)
        daysUntilNextPeriod > 0 -> pluralStringResource(R.plurals.app_days_left, daysUntilNextPeriod, daysUntilNextPeriod)
        daysUntilNextPeriod == 0 -> stringResource(R.string.app_today_exclaim)
        else -> pluralStringResource(R.plurals.app_days_late, -daysUntilNextPeriod, -daysUntilNextPeriod)
    }

    val periodDays = (latestCycle?.periodDurationDays ?: 5).toLong()
    val subtitleDynamic = when {
        isDiscreet -> stringResource(R.string.dash_subtitle_discreet)
        !hasActiveCycle -> stringResource(R.string.dash_subtitle_start_logging)
        isBleedingToday && currentCycleDay != null -> {
            val remainingDays = (periodDays - currentCycleDay + 1).coerceAtLeast(1L)
            stringResource(R.string.dash_period_ending_format, remainingDays)
        }
        selectedDay.phase == CyclePhase.OVULATION -> stringResource(R.string.dash_subtitle_ovulation_today)
        isFertileToday -> stringResource(R.string.dash_subtitle_fertile_peak)
        else -> stringResource(R.string.dash_subtitle_hormones_stable)
    }

    val phaseTitle = when {
        isDiscreet -> if (hasActiveCycle) stringResource(R.string.dash_phase_mid_period) else stringResource(R.string.dash_phase_start_journal)
        !hasActiveCycle -> stringResource(R.string.dash_phase_start_journal_cta)
        isBleedingToday -> stringResource(R.string.dash_phase_menstrual)
        selectedDay.phase == CyclePhase.OVULATION -> stringResource(R.string.app_phase_ovulation_peak)
        isFertileToday -> stringResource(R.string.app_phase_fertile_window)
        else -> stringResource(R.string.app_phase_follicular)
    }

    val conceptionChance = when {
        !hasActiveCycle -> "--"
        selectedDay.phase == CyclePhase.OVULATION -> stringResource(R.string.dash_conception_peak_fertile)
        isFertileToday -> stringResource(R.string.dash_conception_high)
        isBleedingToday -> stringResource(R.string.dash_conception_very_low)
        else -> stringResource(R.string.dash_conception_low)
    }
    val progressRatio = if (currentCycleDay != null && hasActiveCycle) {
        (currentCycleDay.toFloat() / effectiveAvgCycleDays.toFloat()).coerceIn(0.05f, 1f)
    } else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
    ) {
        // 1. HERO CARD: DYNAMIC HORMONAL PHASE (Clean Coral gradient without dark maroon border)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(26.dp))
                    .clip(RoundedCornerShape(26.dp))
                    .background(CoralLinearGradient)
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // TOP ROW: Title & Subtitle on left, Circular Day Progress on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                            Text(
                                text = phaseTitle,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD1D8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = subtitleDynamic,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFFFE4E6)
                                )
                            }
                        }

                        // Circular Progress: Day X
                        Box(
                            modifier = Modifier.size(86.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokePx = 7.dp.toPx()
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.25f),
                                    style = Stroke(width = strokePx)
                                )
                                drawArc(
                                    color = Color.White,
                                    startAngle = -90f,
                                    sweepAngle = (progressRatio * 360f).coerceIn(15f, 360f),
                                    useCenter = false,
                                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (currentCycleDay != null) "$currentCycleDay" else "--",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    lineHeight = 28.sp
                                )
                                Text(
                                    text = stringResource(R.string.dash_day_progress_label),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFFFE4E6)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BOTTOM ROW: FULL-WIDTH Symmetrical Dual-Metric Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 1 (Left): HAID BERIKUTNYA
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 54.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.20f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.dash_hero_next_period_header),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.85f),
                                    letterSpacing = 0.5.sp,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = nextPeriodValueStr,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 2
                                )
                                Text(
                                    text = nextPeriodSubtext,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFE4E6),
                                    maxLines = 2
                                )
                            }
                        }

                        // Card 2 (Right): Dynamic Goal
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 54.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.20f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (isPromilMode) {
                                    val ovDate = fertilePrediction?.predictedOvulationDate
                                    val fertileStart = fertilePrediction?.fertileWindowStart
                                    val daysUntilOvulation = ovDate?.let { ChronoUnit.DAYS.between(today, it).toInt() }

                                    val ovDateStr = ovDate?.let {
                                        val m = it.month.name.lowercase().take(3).replaceFirstChar { c -> c.uppercase() }
                                        "${it.dayOfMonth} $m"
                                    } ?: "--"

                                    val (headerText, valueText, subText, valueColor) = when {
                                        ovDate == null -> Quadruple(
                                            stringResource(R.string.dash_hero_fertile_ovulation_header),
                                            "--",
                                            stringResource(R.string.dash_hero_log_for_prediction),
                                            Color.White
                                        )
                                        // State 3: HARI-H PUNCAK OVULASI
                                        today == ovDate -> Quadruple(
                                            stringResource(R.string.dash_hero_ovulation_peak_today),
                                            stringResource(R.string.dash_hero_best_conceive_time),
                                            stringResource(R.string.dash_hero_max_pregnancy_chance),
                                            Color(0xFFFEF08A)
                                        )
                                        // State 2: JENDELA SUBUR (H-5 s/d H-1 sebelum ovulasi)
                                        fertileStart != null && !today.isBefore(fertileStart) && today.isBefore(ovDate) -> Quadruple(
                                            stringResource(R.string.dash_hero_fertile_window_header),
                                            stringResource(R.string.dash_hero_high_chance),
                                            stringResource(R.string.cal_ovulation_peak_format, ovDateStr, daysUntilOvulation ?: 1),
                                            Color(0xFFA5F3FC)
                                        )
                                        // State 4: PASCA OVULASI (Masa subur lewat)
                                        today.isAfter(ovDate) -> Quadruple(
                                            stringResource(R.string.dash_hero_fertile_window_closed),
                                            stringResource(R.string.dash_hero_low_chance),
                                            stringResource(R.string.dash_hero_awaiting_new_cycle),
                                            Color.White.copy(alpha = 0.9f)
                                        )
                                        // State 1: MENUJU MASA SUBUR (Countdown)
                                        else -> Quadruple(
                                            stringResource(R.string.dash_hero_fertile_ovulation_header),
                                            ovDateStr,
                                            pluralStringResource(R.plurals.app_days_left, daysUntilOvulation ?: 0, daysUntilOvulation ?: 0),
                                            Color(0xFFA5F3FC)
                                        )
                                    }

                                    Text(
                                        text = headerText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 0.5.sp,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = valueText,
                                        fontSize = if (valueText.length > 10) 11.5.sp else 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = valueColor,
                                        maxLines = 2
                                    )
                                    Text(
                                        text = subText,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFFE4E6),
                                        maxLines = 2
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.dash_hero_average_cycle_header),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 0.5.sp,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val avgText = when {
                                        cycleStats != null -> stringResource(R.string.dash_avg_cycle_days_format, cycleStats.averageLength)
                                        hasActiveCycle -> stringResource(R.string.dash_avg_cycle_default_days)
                                        else -> stringResource(R.string.dash_avg_cycle_no_data)
                                    }
                                    Text(
                                        text = avgText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 2
                                    )
                                    val varText = when {
                                        cycleStats?.standardDeviation != null -> stringResource(R.string.dash_avg_cycle_variation_format, cycleStats.standardDeviation)
                                        hasActiveCycle -> stringResource(R.string.dash_avg_cycle_early_estimate)
                                        else -> stringResource(R.string.dash_avg_cycle_no_history)
                                    }
                                    Text(
                                        text = varText,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFFE4E6),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. ERGONOMIC 7-DAY HORIZONTAL WEEK STRIP (Real Backend Calendar Data)
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderCol),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.dash_week_of_format, today.monthName(), today.year),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Coral500))
                        }

                        Text(
                            text = stringResource(R.string.dash_open_full_calendar),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Coral600,
                            modifier = Modifier.clickable { onOpenCalendar() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        weekDays.forEach { item ->
                            val isSelected = item.date == selectedDay.date
                            val isPeak = item.phase == CyclePhase.OVULATION
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when {
                                            isSelected -> Slate900
                                            isPeak -> Color(0xFFECFEFF)
                                            else -> if (isDarkMode) DarkBackground else Slate50
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Coral400 else if (isPeak) Color(0xFFA5F3FC) else Slate200.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onSelectDay(item) }
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = item.dayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color(0xFFFCA5A5) else if (isPeak) MedicalTeal else Slate400
                                )
                                Text(
                                    text = "${item.dayOfMonth}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color.White else if (isPeak) MedicalTeal else textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val log = item.log
                                val hasLogEntry = log != null && (
                                    log.basalBodyTempCelsius != null ||
                                    log.painVasScore > 0 ||
                                    log.cervicalMucus != CervicalMucusType.NONE ||
                                    log.flow != FlowIntensity.NONE ||
                                    !log.notes.isNullOrBlank()
                                )
                                if (hasLogEntry) {
                                    val dotColor = if (item.phase == CyclePhase.FERTILE || item.phase == CyclePhase.OVULATION) MedicalCyan else PrimaryCoral
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. BBT BIPHASIC SPARKLINE TREND CARD (Driven by Real Backend Log Entries)
        item {
            val logsWithBbt = allLogs.filter { it.basalBodyTempCelsius != null }.sortedBy { it.date }.takeLast(7)
            val hasRealBbt = logsWithBbt.isNotEmpty()

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderCol),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFF1F2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Coral600, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.dash_bbt_chart_title), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Text(stringResource(R.string.dash_bbt_chart_subtitle), fontSize = 12.sp, color = textSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (hasRealBbt) Color(0xFFECFDF5) else Slate100,
                            border = BorderStroke(1.dp, if (hasRealBbt) Color(0xFFA7F3D0) else Slate200)
                        ) {
                            Text(
                                text = if (hasRealBbt) stringResource(R.string.dash_bbt_status_normal) else stringResource(R.string.dash_bbt_status_no_data),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasRealBbt) Color(0xFF065F46) else Slate500,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = stringResource(R.string.dash_bbt_coverline),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (hasRealBbt && logsWithBbt.size >= 2) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp)
                        ) {
                            val w = size.width
                            val h = size.height

                            val coverlineY = h * 0.58f
                            drawLine(
                                color = Color(0xFFCBD5E1),
                                start = Offset(0f, coverlineY),
                                end = Offset(w, coverlineY),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )

                            val minT = 36.0
                            val maxT = 37.0
                            val pts = logsWithBbt.mapIndexed { idx, item ->
                                val x = w * (idx.toFloat() / (logsWithBbt.size - 1).coerceAtLeast(1).toFloat())
                                val temp = item.basalBodyTempCelsius ?: 36.4
                                val y = h * (1f - ((temp - minT) / (maxT - minT)).toFloat().coerceIn(0.1f, 0.9f))
                                Offset(x, y)
                            }

                            val curvePath = Path().apply {
                                moveTo(pts.first().x, pts.first().y)
                                for (i in 1 until pts.size) {
                                    val prev = pts[i - 1]
                                    val curr = pts[i]
                                    val midX = (prev.x + curr.x) / 2f
                                    cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                                }
                            }

                            val fillPath = Path().apply {
                                addPath(curvePath)
                                lineTo(pts.last().x, h)
                                lineTo(pts.first().x, h)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Coral600.copy(alpha = 0.35f), Color.Transparent)
                                )
                            )

                            drawPath(
                                path = curvePath,
                                color = Coral600,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            pts.forEachIndexed { idx, pt ->
                                val isSelectedPoint = idx == (pts.size - 1)
                                val dotColor = if (isSelectedPoint) Slate900 else Coral600
                                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                                drawCircle(color = dotColor, radius = 3.5.dp.toPx(), center = pt)
                            }
                        }
                    } else {
                        // Clean empty state when no BBT recorded yet
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.dash_bbt_empty_title),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = stringResource(R.string.dash_bbt_empty_hint),
                                fontSize = 12.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.dash_bbt_phase_follicular_low), fontSize = 12.sp, color = textSecondary)
                        Text(stringResource(R.string.dash_bbt_rise), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Coral600)
                        Text(stringResource(R.string.dash_bbt_phase_luteal_high), fontSize = 12.sp, color = textSecondary)
                    }
                }
            }
        }

        // Prediction Insight Card (FIGO Cycle & Fertility Prediction)
        item {
            CyclePredictionInsightCard(
                fertilePrediction = fertilePrediction,
                latestCycle = latestCycle,
                cycleStats = cycleStats,
                isDarkMode = isDarkMode
            )
        }

        // 4. QUICK DAILY LOG SUMMARY CARD: Driven by real DailyLogEntity for selected date
        item {
            val log = selectedDay.log

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderCol),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Coral500))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.dash_today_note_format, selectedDay.dayOfMonth, selectedDay.date.monthAbbr()),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }

                        Text(
                            text = stringResource(R.string.dash_edit_note),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Coral600,
                            modifier = Modifier.clickable { onOpenLog() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDarkMode) DarkBackground else Slate50,
                            border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(stringResource(R.string.dash_log_basal_temp), fontSize = 12.sp, color = textSecondary)
                                Text(if (log?.basalBodyTempCelsius != null) String.format(Locale.US, "%.2f °C", log.basalBodyTempCelsius) else "-- °C", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, color = textPrimary)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDarkMode) DarkBackground else Slate50,
                            border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(stringResource(R.string.dash_log_cervical_mucus), fontSize = 12.sp, color = textSecondary)
                                Text(if (log != null && log.cervicalMucus != CervicalMucusType.NONE) mucusLabelFor(log.cervicalMucus, resources) else "--", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Real VAS Logic: Green Comfortable if 0 or Bebas Nyeri, Red Alert ONLY if >= 7
                    val vasScore = log?.painVasScore ?: 0
                    val isPainFree = vasScore == 0
                    val isPainAlert = vasScore >= 7

                    val vasContainerColor = when {
                        isPainAlert -> Color(0xFFFFF1F2)
                        isPainFree -> Color(0xFFECFDF5)
                        else -> Color(0xFFFFFBEB)
                    }
                    val vasBorderColor = when {
                        isPainAlert -> Color(0xFFFFE4E6)
                        isPainFree -> Color(0xFFA7F3D0)
                        else -> Color(0xFFFDE68A)
                    }
                    val vasTitleColor = when {
                        isPainAlert -> MedicalRose
                        isPainFree -> Color(0xFF047857)
                        else -> Color(0xFFB45309)
                    }
                    val vasTextColor = when {
                        isPainAlert -> Color(0xFF9F1239)
                        isPainFree -> Color(0xFF065F46)
                        else -> Color(0xFF92400E)
                    }
                    val vasBadgeBg = when {
                        isPainAlert -> MedicalRose
                        isPainFree -> Color(0xFF10B981)
                        else -> Color(0xFFF59E0B)
                    }
                    val vasBadgeText = when {
                        isPainAlert -> stringResource(R.string.dash_vas_severe)
                        isPainFree -> stringResource(R.string.app_pain_free)
                        else -> stringResource(R.string.dash_vas_mild)
                    }
                    val vasDisplayDesc = when {
                        isPainAlert -> stringResource(R.string.dash_vas_pelvic_pain_format, vasScore)
                        isPainFree -> stringResource(R.string.dash_vas_zero_pain_free)
                        else -> stringResource(R.string.dash_vas_mild_pain_format, vasScore)
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = vasContainerColor,
                        border = BorderStroke(1.dp, vasBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    stringResource(R.string.dash_vas_scale_title),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = vasTitleColor
                                )
                                Text(
                                    text = vasDisplayDesc,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = vasTextColor
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = vasBadgeBg
                            ) {
                                Text(
                                    text = vasBadgeText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. CALENDAR VIEW: Connected to real Period Dates & Fertile Prediction from Room DB
@Composable
fun CalendarScreenView(
    isDarkMode: Boolean,
    periodDates: Set<LocalDate>,
    fertilePrediction: FertilePrediction?,
    allLogs: List<DailyLogEntity>,
    latestCycle: CycleEntity?,
    completedCycles: List<CycleEntity>,
    cycleStats: CycleStats? = null,
    selectedCalendarDate: LocalDate = LocalDate.now(),
    onSelectDate: (LocalDate) -> Unit = {},
    onOpenLog: (LocalDate) -> Unit = {},
    onToast: (String) -> Unit
) {
    val resources = LocalContext.current.resources
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorder else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500

    val today = remember { LocalDate.now() }
    var currentYearMonth by remember { mutableStateOf(java.time.YearMonth.from(selectedCalendarDate)) }

    // Multi-Cycle Predictions (FIGO Standard - Projects up to 6 cycles / 6 months ahead)
    val avgCycleLength = (cycleStats?.averageLength ?: 28.0).toLong().coerceIn(21L, 45L)
    val avgPeriodDuration = (cycleStats?.averagePeriodDuration ?: 5.0).toLong().coerceIn(3L, 8L)
    val anchorDate = latestCycle?.startDate ?: periodDates.maxOrNull()

    val predictedPeriodDates = remember(anchorDate, avgCycleLength, avgPeriodDuration) {
        if (anchorDate == null) return@remember emptySet<LocalDate>()
        val set = mutableSetOf<LocalDate>()
        for (k in 1..6) {
            val start = anchorDate.plusDays(k * avgCycleLength)
            for (d in 0 until avgPeriodDuration) {
                set.add(start.plusDays(d))
            }
        }
        set
    }

    val predictedOvulationDates = remember(anchorDate, avgCycleLength) {
        if (anchorDate == null) return@remember emptySet<LocalDate>()
        val set = mutableSetOf<LocalDate>()
        for (k in 1..6) {
            val nextPeriod = anchorDate.plusDays(k * avgCycleLength)
            val ov = nextPeriod.minusDays(14)
            set.add(ov)
        }
        set
    }

    val predictedFertileRanges = remember(predictedOvulationDates) {
        val set = mutableSetOf<LocalDate>()
        predictedOvulationDates.forEach { ov ->
            for (offset in -5..1) {
                set.add(ov.plusDays(offset.toLong()))
            }
        }
        set
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.cal_eyebrow_cycle_ovulation_map), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Coral600)
                    Text(stringResource(R.string.cal_header_title), fontSize = 20.sp, fontWeight = FontWeight.Black, color = textPrimary)
                }

                Surface(
                    onClick = {
                        currentYearMonth = java.time.YearMonth.from(today)
                        onSelectDate(today)
                        onToast(resources.getString(R.string.cal_toast_back_to_today, today.dayOfMonth, today.monthName(), today.year))
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF1F2),
                    border = BorderStroke(1.dp, Color(0xFFFFE4E6))
                ) {
                    Text(stringResource(R.string.cal_today), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Coral600, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }

        // Full Month Calendar Card with Interactive Month Navigation (< and >)
        item {
            val daysInMonth = currentYearMonth.lengthOfMonth()
            val firstDayOfMonth = currentYearMonth.atDay(1).dayOfWeek.value // 1 = Monday ... 7 = Sunday

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderCol),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                currentYearMonth = currentYearMonth.minusMonths(1)
                                val maxDay = currentYearMonth.lengthOfMonth()
                                val targetDay = selectedCalendarDate.dayOfMonth.coerceIn(1, maxDay)
                                onSelectDate(currentYearMonth.atDay(targetDay))
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.cal_prev_month), tint = textPrimary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentYearMonth.year}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textPrimary
                            )
                            Text(
                                text = if (fertilePrediction != null) {
                                    val nextP = fertilePrediction.predictedNextPeriodDate
                                    val ov = fertilePrediction.predictedOvulationDate
                                    stringResource(R.string.cal_period_ovulation_format, nextP.dayOfMonth, nextP.monthAbbr(), ov.dayOfMonth, ov.monthAbbr())
                                } else {
                                    stringResource(R.string.cal_prediction_hint)
                                },
                                fontSize = 12.sp,
                                color = if (fertilePrediction != null) Coral600 else Slate400,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = {
                                currentYearMonth = currentYearMonth.plusMonths(1)
                                val maxDay = currentYearMonth.lengthOfMonth()
                                val targetDay = selectedCalendarDate.dayOfMonth.coerceIn(1, maxDay)
                                onSelectDate(currentYearMonth.atDay(targetDay))
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.cal_next_month), tint = textPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val headers = (1..7).map { DayOfWeek.of(it).getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        headers.forEach { h ->
                            Text(h, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSecondary, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val totalSlots = ((firstDayOfMonth - 1) + daysInMonth)
                    val totalRows = (totalSlots + 6) / 7

                    for (row in 0 until totalRows) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            for (col in 0..6) {
                                val slotIndex = row * 7 + col
                                val dayNum = slotIndex - (firstDayOfMonth - 1) + 1
                                if (dayNum in 1..daysInMonth) {
                                    val cellDate = currentYearMonth.atDay(dayNum)

                                    // Real bleeding vs predicted bleeding vs fertile window
                                    val isActualHaid = cellDate in periodDates
                                    val isPredictedHaid = !isActualHaid && !cellDate.isBefore(today) && (
                                        cellDate in predictedPeriodDates ||
                                        (fertilePrediction != null && cellDate in (0 until avgPeriodDuration).map { fertilePrediction.predictedNextPeriodDate.plusDays(it) })
                                    )
                                    val isPeakOvulation = (fertilePrediction != null && cellDate == fertilePrediction.predictedOvulationDate) || cellDate in predictedOvulationDates
                                    val isFertile = (fertilePrediction != null && !cellDate.isBefore(fertilePrediction.fertileWindowStart) && !cellDate.isAfter(fertilePrediction.fertileWindowEnd)) || cellDate in predictedFertileRanges
                                    val isSelected = cellDate == selectedCalendarDate

                                    val cellBg = when {
                                        isSelected -> Slate900
                                        isActualHaid -> Color(0xFFFFE4E6)
                                        isPredictedHaid -> Color(0xFFFFF1F2)
                                        isPeakOvulation -> MedicalCyan
                                        isFertile -> Color(0xFFCFFAFE)
                                        else -> Color.Transparent
                                    }

                                    val cellBorder = when {
                                        isSelected -> BorderStroke(2.dp, Coral400)
                                        isPredictedHaid -> BorderStroke(1.dp, Color(0xFFFECDD3))
                                        else -> null
                                    }

                                    val textColor = when {
                                        isSelected || isPeakOvulation -> Color.White
                                        isActualHaid -> Color(0xFF9F1239)
                                        isPredictedHaid -> Color(0xFFBE123C)
                                        isFertile -> Color(0xFF0E7490)
                                        else -> textPrimary
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(cellBg)
                                            .then(if (cellBorder != null) Modifier.border(cellBorder, RoundedCornerShape(10.dp)) else Modifier)
                                            .clickable {
                                                if (selectedCalendarDate == cellDate) {
                                                    onOpenLog(cellDate)
                                                } else {
                                                    onSelectDate(cellDate)
                                                    onToast(resources.getString(R.string.cal_selected_day_hint, dayNum, cellDate.monthName()))
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$dayNum",
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected || isPeakOvulation || isActualHaid || isPredictedHaid) FontWeight.Black else FontWeight.SemiBold,
                                                color = textColor
                                            )
                                            if (isPredictedHaid && !isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(3.dp)
                                                        .clip(CircleShape)
                                                        .background(Coral600)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        LegendPill(color = Color(0xFFFFE4E6), label = stringResource(R.string.cal_legend_menstruation), textSecondary)
                        LegendPill(color = Color(0xFFFECDD3), label = stringResource(R.string.cal_legend_predicted_period), textSecondary)
                        LegendPill(color = Color(0xFFCFFAFE), label = stringResource(R.string.cal_legend_fertile_window), textSecondary)
                        LegendPill(color = MedicalCyan, label = stringResource(R.string.app_phase_ovulation_peak), textSecondary)
                    }
                }
            }
        }

        // Inspector Card for Selected Date (with Prediction Details)
        item {
            val log = allLogs.find { it.date == selectedCalendarDate }
            val isActualHaid = selectedCalendarDate in periodDates
            val isPredictedHaid = !isActualHaid && !selectedCalendarDate.isBefore(today) && (
                selectedCalendarDate in predictedPeriodDates ||
                (fertilePrediction != null && selectedCalendarDate in (0 until avgPeriodDuration).map { fertilePrediction.predictedNextPeriodDate.plusDays(it) })
            )
            val isPeakOvulation = (fertilePrediction != null && selectedCalendarDate == fertilePrediction.predictedOvulationDate) || selectedCalendarDate in predictedOvulationDates
            val isFertile = (fertilePrediction != null && !selectedCalendarDate.isBefore(fertilePrediction.fertileWindowStart) && !selectedCalendarDate.isAfter(fertilePrediction.fertileWindowEnd)) || selectedCalendarDate in predictedFertileRanges

            val hasActiveCycle = latestCycle != null || periodDates.isNotEmpty()

            val phaseBadgeText = when {
                !hasActiveCycle -> stringResource(R.string.dash_bbt_status_no_data)
                isActualHaid -> stringResource(R.string.cal_legend_menstruation)
                isPredictedHaid -> stringResource(R.string.cal_legend_predicted_period)
                isPeakOvulation -> stringResource(R.string.app_phase_ovulation_peak)
                isFertile -> stringResource(R.string.cal_legend_fertile_window)
                else -> stringResource(R.string.app_phase_follicular)
            }

            val cycleDayForSelected = latestCycle?.let {
                val diff = ChronoUnit.DAYS.between(it.startDate, selectedCalendarDate) + 1L
                if (diff > 0) diff else null
            }
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderCol),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${selectedCalendarDate.dayOfMonth} ${selectedCalendarDate.monthName()} ${selectedCalendarDate.year}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when {
                                        !hasActiveCycle -> if (isDarkMode) DarkBackground else Slate100
                                        isPredictedHaid || isActualHaid -> Color(0xFFFFF1F2)
                                        else -> Color(0xFFCFFAFE)
                                    }
                                ) {
                                    Text(
                                        text = phaseBadgeText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            !hasActiveCycle -> Slate500
                                            isPredictedHaid || isActualHaid -> Coral600
                                            else -> Color(0xFF0E7490)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = when {
                                    !hasActiveCycle -> stringResource(R.string.cal_no_active_cycle_hint)
                                    isActualHaid -> stringResource(R.string.cal_status_active_menstrual_bleeding)
                                    isPredictedHaid -> stringResource(R.string.cal_status_predicted_period_start)
                                    isPeakOvulation -> stringResource(R.string.cal_status_peak_conception_chance)
                                    isFertile -> stringResource(R.string.cal_status_fertile_window)
                                    selectedCalendarDate == today && cycleDayForSelected != null -> stringResource(R.string.cal_today_cycle_day_format, cycleDayForSelected)
                                    selectedCalendarDate == today -> stringResource(R.string.cal_status_today_no_active_cycle)
                                    cycleDayForSelected != null -> stringResource(R.string.cal_cycle_day_format, cycleDayForSelected)
                                    else -> stringResource(R.string.cal_status_outside_active_cycle)
                                },
                                fontSize = 13.sp,
                                color = textSecondary
                            )
                        }
                        Button(
                            onClick = { onOpenLog(selectedCalendarDate) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Coral500),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (log != null) stringResource(R.string.cal_action_edit_log) else stringResource(R.string.cal_action_add_log), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val bbtVal = log?.basalBodyTempCelsius?.let { String.format(Locale.US, "%.2f °C", it) } ?: "-- °C"
                    val mucusVal = log?.cervicalMucus?.let { mucusLabelFor(it, resources) } ?: "--"
                    val painVal = log?.let {
                        if (it.painVasScore > 0) "VAS ${it.painVasScore}" else stringResource(R.string.app_pain_free)
                    } ?: stringResource(R.string.app_pain_free)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ParameterBox(stringResource(R.string.cal_param_basal_temp), bbtVal, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox(stringResource(R.string.dash_log_cervical_mucus), mucusVal, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox(stringResource(R.string.cal_param_pain_scale), painVal, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                    }
                }
            }
        }

        // 4. COMPREHENSIVE CYCLE & FERTILITY PREDICTION INSIGHT CARD
        item {
            CyclePredictionInsightCard(
                fertilePrediction = fertilePrediction,
                latestCycle = latestCycle,
                cycleStats = cycleStats,
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
fun LegendPill(color: Color, label: String, textSecondary: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = textSecondary)
    }
}

@Composable
fun ParameterBox(label: String, value: String, modifier: Modifier, isDarkMode: Boolean, textPrimary: Color, textSecondary: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isDarkMode) DarkBackground else Slate50,
        border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(label, fontSize = 12.sp, color = textSecondary)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
        }
    }
}

// 4. SPOG MEDICAL REPORT SCREEN: Wired to Room Database completedCycles and FIGO calculations

@Composable
fun CyclePredictionInsightCard(
    fertilePrediction: FertilePrediction?,
    latestCycle: CycleEntity?,
    cycleStats: CycleStats?,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorder else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500

    val today = remember { LocalDate.now() }

    val nextDate = fertilePrediction?.predictedNextPeriodDate
    val daysToNextPeriod = nextDate?.let { ChronoUnit.DAYS.between(today, it) }

    val ovulationDate = fertilePrediction?.predictedOvulationDate
    val daysToOvulation = ovulationDate?.let { ChronoUnit.DAYS.between(today, it) }

    val fertileStart = fertilePrediction?.fertileWindowStart
    val fertileEnd = fertilePrediction?.fertileWindowEnd

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderCol),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFF1F2)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Coral600,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.insight_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 2
                        )
                        Text(
                            text = stringResource(R.string.insight_subtitle),
                            fontSize = 12.sp,
                            color = textSecondary,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFF1F2),
                    border = BorderStroke(1.dp, Color(0xFFFFE4E6))
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (daysToNextPeriod != null) {
                                when {
                                    daysToNextPeriod > 0 -> pluralStringResource(R.plurals.app_days_left, daysToNextPeriod.toInt(), daysToNextPeriod.toInt())
                                    daysToNextPeriod == 0L -> stringResource(R.string.cal_today)
                                    else -> pluralStringResource(R.plurals.app_days_late, (-daysToNextPeriod).toInt(), (-daysToNextPeriod).toInt())
                                }
                            } else stringResource(R.string.insight_status_ready),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = if (daysToNextPeriod != null && daysToNextPeriod < 0) Color(0xFFDC2626) else Coral700
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Bento Metric Tiles (Symmetrical heights & aligned baselines)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tile 1: Next Period
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 74.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkMode) DarkBackground else Slate50,
                    border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.insight_next_period_label), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textSecondary, maxLines = 2)
                        Text(
                            text = if (nextDate != null) "${nextDate.dayOfMonth} ${nextDate.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}" else "--",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Coral600,
                            maxLines = 2
                        )
                        Text(
                            text = if (daysToNextPeriod != null) {
                                when {
                                    daysToNextPeriod > 0 -> pluralStringResource(R.plurals.app_days_left, daysToNextPeriod.toInt(), daysToNextPeriod.toInt())
                                    daysToNextPeriod == 0L -> stringResource(R.string.dash_day_progress_label)
                                    else -> pluralStringResource(R.plurals.app_days_late, (-daysToNextPeriod).toInt(), (-daysToNextPeriod).toInt())
                                }
                            } else "--",
                            fontSize = 12.sp,
                            color = if (daysToNextPeriod != null && daysToNextPeriod < 0) Color(0xFFDC2626) else textSecondary,
                            maxLines = 2
                        )
                    }
                }

                // Tile 2: Puncak Ovulasi
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 74.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkMode) DarkBackground else Slate50,
                    border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.app_phase_ovulation_peak), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textSecondary, maxLines = 2)
                        Text(
                            text = if (ovulationDate != null) "${ovulationDate.dayOfMonth} ${ovulationDate.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}" else "--",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = MedicalTeal,
                            maxLines = 2
                        )
                        Text(
                            text = if (daysToOvulation != null) {
                                if (daysToOvulation > 0) pluralStringResource(R.plurals.app_days_left, daysToOvulation.toInt(), daysToOvulation.toInt()) else if (daysToOvulation == 0L) stringResource(R.string.app_today_exclaim) else stringResource(R.string.insight_ovulation_passed)
                            } else "--",
                            fontSize = 12.sp,
                            color = textSecondary,
                            maxLines = 2
                        )
                    }
                }

                // Tile 3: Jendela Subur
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 74.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkMode) DarkBackground else Slate50,
                    border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.cal_legend_fertile_window), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textSecondary, maxLines = 2)
                        Text(
                            text = if (fertileStart != null && fertileEnd != null) "${fertileStart.dayOfMonth}-${fertileEnd.dayOfMonth} ${fertileStart.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}" else "--",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalCyan,
                            maxLines = 2
                        )
                        Text(if (fertileStart != null) stringResource(R.string.insight_fertile_days) else "--", fontSize = 12.sp, color = textSecondary, maxLines = 2)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clinical Note on Prediction Formula
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECFDF5), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.report_luteal_assumption_note, cycleStats?.averageLength?.let { String.format(Locale.US, "%.0f", it) } ?: "28"),
                    fontSize = 12.sp,
                    color = Color(0xFF065F46)
                )
            }
        }
    }
}

@Composable
fun SpOgReportScreenView(
    isDarkMode: Boolean,
    isPro: Boolean,
    anonymousRecoveryKey: String,
    cycleStats: CycleStats?,
    completedCycles: List<CycleEntity>,
    anomalies: List<AnomalyAlert>,
    allLogs: List<DailyLogEntity>,
    latestCycle: CycleEntity? = null,
    fertilePrediction: FertilePrediction? = null,
    onSharePdf: (() -> Unit)? = null,
    onExportCsv: (() -> Unit)? = null,
    onBuyPro: () -> Unit = {},
    onNavigateToCalendar: (LocalDate) -> Unit = {},
    onToast: (String) -> Unit = {}
) {
    val resources = LocalContext.current.resources
    var selectedCycleForDetail by remember { mutableStateOf<CycleEntity?>(null) }
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorder else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500

    val today = remember { LocalDate.now() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.report_title_cycle_analysis), fontSize = 20.sp, fontWeight = FontWeight.Black, color = textPrimary)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDarkMode) DarkCardBackground else Slate100
                ) {
                    Text(stringResource(R.string.report_section_data_summary), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate600, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }

        // Clinical Document Card
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderCol),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(R.string.report_section_clinical_summary), fontSize = 14.sp, fontWeight = FontWeight.Black, color = textPrimary)
                            Text(stringResource(R.string.report_anonymous_id_label, anonymousRecoveryKey), fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = textSecondary)
                        }
                        Text(
                            text = "${today.dayOfMonth} ${today.month.name.lowercase().take(3).replaceFirstChar { c -> c.uppercase() }} ${today.year}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val rStr = cycleStats?.averageLength?.let { stringResource(R.string.unit_days_decimal_format, it) } ?: if (latestCycle != null) stringResource(R.string.report_metric_estimate_placeholder) else stringResource(R.string.dash_avg_cycle_no_data)
                        val vStr = cycleStats?.standardDeviation?.let { stringResource(R.string.report_metric_variation_format, it) } ?: if (latestCycle != null) stringResource(R.string.report_metric_initial_estimate) else stringResource(R.string.dash_avg_cycle_no_data)
                        val dStr = cycleStats?.averagePeriodDuration?.let { stringResource(R.string.unit_days_decimal_format, it) } ?: if (latestCycle != null) stringResource(R.string.unit_days_decimal_format, latestCycle.periodDurationDays.toFloat()) else stringResource(R.string.dash_avg_cycle_no_data)
                        ParameterBox(stringResource(R.string.report_metric_average), rStr, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox(stringResource(R.string.report_metric_cycle_variation), vStr, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox(stringResource(R.string.report_metric_period_length), dStr, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Mini BBT Curve in Report
                    val bbtLogs = allLogs.filter { it.basalBodyTempCelsius != null }.sortedBy { it.date }
                    if (bbtLogs.size >= 2) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDarkMode) DarkBackground else Slate50,
                            border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(stringResource(R.string.report_bbt_biphasic_pattern), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Canvas(modifier = Modifier.fillMaxWidth().height(36.dp)) {
                                    drawLine(
                                        color = Color(0xFFCBD5E1),
                                        start = Offset(0f, size.height * 0.55f),
                                        end = Offset(size.width, size.height * 0.55f),
                                        strokeWidth = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                                    )
                                    val minT = 36.0
                                    val maxT = 37.0
                                    val w = size.width
                                    val h = size.height
                                    val pts = bbtLogs.mapIndexed { idx, item ->
                                        val x = w * (idx.toFloat() / (bbtLogs.size - 1).coerceAtLeast(1).toFloat())
                                        val temp = item.basalBodyTempCelsius ?: 36.4
                                        val y = h * (1f - ((temp - minT) / (maxT - minT)).toFloat().coerceIn(0.1f, 0.9f))
                                        Offset(x, y)
                                    }
                                    val path = Path().apply {
                                        moveTo(pts.first().x, pts.first().y)
                                        for (i in 1 until pts.size) {
                                            val prev = pts[i - 1]
                                            val curr = pts[i]
                                            val midX = (prev.x + curr.x) / 2f
                                            cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                                        }
                                    }
                                    drawPath(path, Coral600, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(stringResource(R.string.report_coverline_value), fontSize = 12.sp, color = textSecondary)
                                    Text(stringResource(R.string.report_metric_biphasic_shift), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MedicalTeal)
                                    Text(stringResource(R.string.report_metric_luteal_phase), fontSize = 12.sp, color = textSecondary)
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDarkMode) DarkBackground else Slate50,
                            border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = stringResource(R.string.report_bbt_basal_pattern),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.report_bbt_empty_hint),
                                    fontSize = 12.sp,
                                    color = textSecondary,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Real Anomaly Alert Box (if anomalies detected in Room DB)
                    if (anomalies.isNotEmpty()) {
                        val alert = anomalies.first()
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF1F2),
                            border = BorderStroke(1.dp, Color(0xFFFFE4E6))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MedicalRose, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.report_attention_prefix) + stringResource(alert.type.descriptionRes), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MedicalRose)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = alert.localizedDetail(resources),
                                    fontSize = 12.sp,
                                    color = Color(0xFF9F1239),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    } else if (completedCycles.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.report_status_normal), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF065F46))
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDarkMode) DarkBackground else Slate50,
                            border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Shield, contentDescription = null, tint = Coral600, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.report_status_monitoring), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.report_section_timeline_visualization),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textSecondary
                        )
                        Text(
                            text = pluralStringResource(R.plurals.report_cycle_count, completedCycles.size + if (latestCycle != null) 1 else 0, completedCycles.size + if (latestCycle != null) 1 else 0),
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    var activeFilter by remember { mutableStateOf(CycleFilter.ALL) }

                    // Filter Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CycleFilter.entries.forEach { filter ->
                            val isSelected = activeFilter == filter
                            Surface(
                                onClick = { activeFilter = filter },
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) Coral500 else if (isDarkMode) DarkBackground else Slate100,
                                border = if (!isSelected) BorderStroke(1.dp, borderCol) else null
                            ) {
                                Text(
                                    text = stringResource(filter.labelRes),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else textSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Color Legend Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Coral500))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.app_timeline_menstruation), fontSize = 12.sp, color = textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF67E8F9)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.cal_legend_fertile_window), fontSize = 12.sp, color = textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.5.dp, Color(0xFF0891B2), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.app_timeline_ovulation), fontSize = 12.sp, color = textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.width(2.dp).height(8.dp).background(if (isDarkMode) Color.White else Color(0xFF1E293B)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.cal_today), fontSize = 12.sp, color = textSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Cycle Timeline List (Canvas-based Visual Bar)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDarkMode) DarkBackground else Slate50,
                        border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (completedCycles.isNotEmpty() || latestCycle != null) {
                                if (latestCycle != null) {
                                    val currentDay = ChronoUnit.DAYS.between(latestCycle.startDate, today).toInt() + 1
                                    val expLen = (cycleStats?.averageLength ?: 28.0).toInt().coerceIn(21, 45)
                                    CycleHistoryRow(
                                        cycle = latestCycle,
                                        activeFilter = activeFilter,
                                        elapsedDaysIfActive = currentDay,
                                        expectedCycleLength = expLen,
                                        textColor = textPrimary,
                                        subTextColor = textSecondary,
                                        trackColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0),
                                        onClick = { selectedCycleForDetail = latestCycle },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    if (completedCycles.isNotEmpty()) {
                                        HorizontalDivider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))
                                    }
                                }
                                val expLen = (cycleStats?.averageLength ?: 28.0).toInt().coerceIn(21, 45)
                                completedCycles.takeLast(6).reversed().forEach { c ->
                                    CycleHistoryRow(
                                        cycle = c,
                                        activeFilter = activeFilter,
                                        expectedCycleLength = expLen,
                                        textColor = textPrimary,
                                        subTextColor = textSecondary,
                                        trackColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0),
                                        onClick = { selectedCycleForDetail = c },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.report_empty_history_message),
                                        fontSize = 13.sp,
                                        color = textSecondary,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Recent Daily Logs Table (Real Historical Log Entries)
                    Text(stringResource(R.string.report_section_recent_daily_logs), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = textSecondary)
                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.6f))
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isDarkMode) DarkBackground else Slate50)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.app_column_date), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSecondary, modifier = Modifier.weight(1.2f))
                                Text(stringResource(R.string.app_column_blood), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSecondary, modifier = Modifier.weight(1f))
                                Text(stringResource(R.string.app_column_bbt_temp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSecondary, modifier = Modifier.weight(1f))
                                Text(stringResource(R.string.app_column_pain), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSecondary, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))

                            if (allLogs.isNotEmpty()) {
                                allLogs.sortedByDescending { it.date }.take(5).forEach { log ->
                                    val dateStr = "${log.date.dayOfMonth} ${log.date.monthAbbr()}"
                                    val flowStr = when (log.flow) {
                                        FlowIntensity.SPOTTING -> stringResource(R.string.app_column_spotting)
                                        FlowIntensity.LIGHT -> stringResource(R.string.dash_vas_mild)
                                        FlowIntensity.MEDIUM -> stringResource(R.string.app_option_medium)
                                        FlowIntensity.HEAVY -> stringResource(R.string.app_option_heavy)
                                        else -> stringResource(R.string.app_option_no)
                                    }
                                    val bbtStr = log.basalBodyTempCelsius?.let { String.format(Locale.US, "%.2f°C", it) } ?: "--"
                                    val painStr = if (log.painVasScore > 0) "VAS ${log.painVasScore}" else stringResource(R.string.app_option_none)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(dateStr, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textPrimary, modifier = Modifier.weight(1.2f))
                                        Text(
                                            flowStr,
                                            fontSize = 12.sp,
                                            fontWeight = if (log.flow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY)) FontWeight.Bold else FontWeight.Normal,
                                            color = if (log.flow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY)) Coral600 else textPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(bbtStr, fontSize = 12.sp, color = textPrimary, modifier = Modifier.weight(1f))
                                        Text(painStr, fontSize = 12.sp, color = if (log.painVasScore >= 4) MedicalRose else textPrimary, modifier = Modifier.weight(1f))
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.report_empty_daily_logs),
                                        fontSize = 12.sp,
                                        color = textSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // 1. Primary Button: PDF Medis
                Button(
                    onClick = { onSharePdf?.invoke() ?: onToast(resources.getString(R.string.report_export_preparing_summary)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral500)
                ) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.report_action_download_summary_pdf), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // 2. Secondary Button: CSV Mentah (Excel / Sheets)
                OutlinedButton(
                    onClick = { onExportCsv?.invoke() ?: onToast(resources.getString(R.string.app_export_exporting_raw_csv)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Icon(Icons.Rounded.TableView, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.app_action_download_raw_csv), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                }

                // 3. Subtle Pro Text Link (Placemarked underneath both action buttons)
                if (!isPro) {
                    TextButton(
                        onClick = { onBuyPro() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append(stringResource(R.string.report_export_ads_hint))
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = PrimaryCoral)) {
                                    append(stringResource(R.string.app_action_open_lifetime_pro_license))
                                }
                            },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (selectedCycleForDetail != null) {
        CycleDetailBottomSheet(
            cycle = selectedCycleForDetail!!,
            allLogs = allLogs,
            isDarkMode = isDarkMode,
            onDismiss = { selectedCycleForDetail = null },
            onOpenCalendar = { date ->
                selectedCycleForDetail = null
                onNavigateToCalendar(date)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleDetailBottomSheet(
    cycle: CycleEntity,
    allLogs: List<DailyLogEntity>,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onOpenCalendar: (LocalDate) -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("d MMM yyyy")
    val isOngoing = cycle.endDate == null
    val today = remember { LocalDate.now() }
    val cycleLength = if (isOngoing) {
        maxOf(28, (ChronoUnit.DAYS.between(cycle.startDate, today) + 1L).toInt())
    } else {
        cycle.cycleLengthDays ?: 28
    }

    val periodEnd = cycle.startDate.plusDays((cycle.periodDurationDays - 1).toLong().coerceAtLeast(0L))
    val ovulationDay = (cycleLength - 14).coerceIn(1, cycleLength)
    val ovulationDate = cycle.startDate.plusDays((ovulationDay - 1).toLong())
    val fertileStart = cycle.startDate.plusDays((ovulationDay - 6).toLong().coerceAtLeast(0L))

    val cycleLogs = remember(cycle, allLogs) {
        allLogs.filter {
            !it.date.isBefore(cycle.startDate) && (cycle.endDate == null || !it.date.isAfter(cycle.endDate))
        }
    }

    val bbtValues = cycleLogs.mapNotNull { it.basalBodyTempCelsius }
    val avgBbtStr = if (bbtValues.isNotEmpty()) String.format(Locale.US, "%.2f °C", bbtValues.average()) else stringResource(R.string.report_value_not_recorded)

    val symptomsList = remember(cycleLogs) {
        cycleLogs.mapNotNull { it.painLocation }
            .flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    val peakPain = remember(cycleLogs) {
        cycleLogs.maxOfOrNull { it.painVasScore } ?: 0
    }

    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = cardBg,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isOngoing) stringResource(R.string.report_metric_current_cycle) else stringResource(R.string.detail_title_cycle_history),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = textPrimary
                    )
                    Text(
                        text = "${cycle.startDate.format(fmt)} – ${if (isOngoing) stringResource(R.string.detail_status_ongoing) else cycle.endDate!!.format(fmt)}",
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = textSecondary)
                }
            }

            // Summary Pill Cards
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkMode) DarkBackground else Slate50,
                border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Row 1: Total Siklus & Status
                    DetailMetricRow(
                        icon = Icons.Rounded.Sync,
                        iconTint = Coral600,
                        label = stringResource(R.string.detail_metric_total_cycle_length),
                        value = stringResource(R.string.detail_cycle_length_format, cycleLength),
                        valueColor = Coral600
                    )
                    HorizontalDivider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))

                    // Row 2: Masa Haid
                    DetailMetricRow(
                        icon = Icons.Rounded.WaterDrop,
                        iconTint = Coral500,
                        label = stringResource(R.string.detail_metric_menstrual_bleeding),
                        value = stringResource(R.string.detail_period_duration_format, cycle.periodDurationDays, cycle.startDate.dayOfMonth, periodEnd.dayOfMonth, cycle.startDate.monthAbbr()),
                        valueColor = textPrimary
                    )
                    HorizontalDivider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))

                    // Row 3: Masa Subur
                    DetailMetricRow(
                        icon = Icons.Rounded.Favorite,
                        iconTint = Color(0xFF0891B2),
                        label = stringResource(R.string.detail_metric_fertile_window),
                        value = stringResource(R.string.detail_date_range_format, fertileStart.dayOfMonth, ovulationDate.dayOfMonth, ovulationDate.monthAbbr()),
                        valueColor = Color(0xFF0891B2)
                    )
                    HorizontalDivider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))

                    // Row 4: Ovulasi
                    DetailMetricRow(
                        icon = Icons.Rounded.WbSunny,
                        iconTint = Color(0xFF0891B2),
                        label = stringResource(R.string.detail_metric_ovulation_peak_estimate),
                        value = stringResource(R.string.detail_ovulation_day_format, ovulationDay, ovulationDate.dayOfMonth, ovulationDate.monthAbbr()),
                        valueColor = Color(0xFF0891B2)
                    )
                    HorizontalDivider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))

                    // Row 5: BBT & Nyeri
                    DetailMetricRow(
                        icon = Icons.Rounded.Thermostat,
                        iconTint = Coral600,
                        label = stringResource(R.string.detail_metric_average_temperature),
                        value = avgBbtStr,
                        valueColor = textPrimary
                    )
                    HorizontalDivider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))

                    DetailMetricRow(
                        icon = Icons.Rounded.Bolt,
                        iconTint = if (peakPain >= 7) Coral600 else Slate500,
                        label = stringResource(R.string.detail_metric_peak_pain_scale),
                        value = if (peakPain > 0) "VAS $peakPain / 10" else stringResource(R.string.app_pain_free),
                        valueColor = if (peakPain >= 7) Coral600 else textPrimary
                    )
                }
            }

            // Symptoms tags if any
            if (symptomsList.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.detail_symptoms_recorded), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        symptomsList.forEach { sym ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFF1F2),
                                border = BorderStroke(1.dp, Coral400)
                            ) {
                                Text(
                                    text = sym,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Coral600,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Teleport to Calendar Button
            Button(
                onClick = {
                    onOpenCalendar(cycle.startDate)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Coral500)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.detail_open_in_calendar, cycle.startDate.monthName()),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DetailMetricRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 13.sp, color = Slate500)
        }
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

// 5. SETTINGS SCREEN: Wired to Cloudflare Backup, Recovery Key, and Data Nuke
/** Text-size steps offered in Settings: multiplier over the system font scale, and its label. */
private val TEXT_SIZE_STEPS = listOf(
    0.90f to R.string.settings_text_size_small,
    1.00f to R.string.settings_text_size_normal,
    1.15f to R.string.settings_text_size_large,
    1.30f to R.string.settings_text_size_extra
)

@Composable
fun SettingsScreenView(
    isDarkMode: Boolean,
    isDiscreet: Boolean,
    isPro: Boolean,
    appLanguage: String = "system",
    onLanguageChanged: (String) -> Unit = {},
    appTextScale: Float = 1f,
    onTextScaleChanged: (Float) -> Unit = {},
    isPromilMode: Boolean = false,
    onTogglePromilMode: (Boolean) -> Unit = {},
    isPinConfigured: Boolean,
    anonymousRecoveryKey: String,
    onToggleDiscreet: () -> Unit,
    onToggleDark: () -> Unit,
    onOpenPin: () -> Unit,
    onBuyPro: () -> Unit,
    onCopyRecoveryKey: (() -> Unit)? = null,
    onBackupLocal: ((Boolean, String?) -> Unit)? = null,
    onRestoreLocal: (() -> Unit)? = null,
    onNukeData: (String) -> Boolean,
    onToast: (String) -> Unit
) {
    val resources = LocalContext.current.resources
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorderColor else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500
    var isBiometricEnabled by remember { mutableStateOf(false) }
    var isBackupOptionsDialogOpen by remember { mutableStateOf(false) }
    var isBackupEncrypted by remember { mutableStateOf(false) }
    var backupPinInput by remember { mutableStateOf("") }
    var nukeStage by remember { mutableStateOf(NukeStage.Closed) }
    var nukeKeyword by remember { mutableStateOf("") }
    var nukePin by remember { mutableStateOf("") }
    var isNukePinWrong by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.settings_section_preferences), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Coral600)
                    Text(stringResource(R.string.title_settings), fontSize = 20.sp, fontWeight = FontWeight.Black, color = textPrimary)
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDarkMode) DarkCardBackground else Slate100,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Slate500, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // GROUP 0: MONETISASI / STATUS LISENSI
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = if (isPro) Color(0xFF059669) else Color(0xFFF59E0B)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        if (isPro) stringResource(R.string.settings_pro_lifetime_active) else stringResource(R.string.settings_free_version_ads),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        if (isPro) stringResource(R.string.settings_pro_ads_free_forever) else stringResource(R.string.settings_upgrade_lifetime_pro),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = if (isPro) stringResource(R.string.settings_pro_features_unlimited) else stringResource(R.string.settings_pro_purchase_description),
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    if (!isPro) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onBuyPro,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.settings_open_pro_license), color = Color(0xFFB45309), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // GROUP: PILIHAN BAHASA / LANGUAGE PREFERENCE
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderCol),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.section_language_title),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Coral600
                    )

                    val toastFollowSystem = stringResource(R.string.settings_language_follow_system)
                    val toastIndonesian = stringResource(R.string.settings_language_set_indonesian)
                    val toastEnglish = stringResource(R.string.settings_language_set_english)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("system", stringResource(R.string.language_option_system), Icons.Default.Language),
                            Triple("id", stringResource(R.string.language_option_id), Icons.Default.Public),
                            Triple("en", stringResource(R.string.language_option_en), Icons.Default.Translate)
                        ).forEach { (langCode, label, icon) ->
                            val isSelected = appLanguage == langCode
                            Surface(
                                onClick = {
                                    onLanguageChanged(langCode)
                                    onToast(
                                        when (langCode) {
                                            "en" -> toastEnglish
                                            "id" -> toastIndonesian
                                            else -> toastFollowSystem
                                        }
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFFFF1F2) else if (isDarkMode) DarkBackground else Slate100,
                                border = BorderStroke(1.dp, if (isSelected) Coral400 else Color.Transparent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Coral600 else Slate500,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Coral600 else textPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // GROUP: UKURAN TEKS / TEXT SIZE
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderCol),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.section_text_size_title),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Coral600
                    )
                    Text(
                        text = stringResource(R.string.settings_text_size_hint),
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TEXT_SIZE_STEPS.forEach { (scale, labelRes) ->
                            val isSelected = Math.abs(appTextScale - scale) < 0.01f
                            Surface(
                                onClick = { onTextScaleChanged(scale) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFFFF1F2) else if (isDarkMode) DarkBackground else Slate100,
                                border = BorderStroke(1.dp, if (isSelected) Coral400 else Color.Transparent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(labelRes),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Coral600 else textPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // GROUP: TUJUAN APLIKASI (GOAL)
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderCol),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.settings_section_tracking_goal), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Coral600)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(stringResource(R.string.settings_promil_mode), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(
                                text = if (isPromilMode)
                                    stringResource(R.string.settings_promil_on_description)
                                else
                                    stringResource(R.string.settings_promil_off_description),
                                fontSize = 13.sp,
                                color = textSecondary,
                                lineHeight = 15.sp
                            )
                        }
                        Switch(
                            checked = isPromilMode,
                            onCheckedChange = {
                                onTogglePromilMode(it)
                                onToast(if (it) resources.getString(R.string.settings_promil_mode_enabled) else resources.getString(R.string.settings_normal_mode_enabled))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Coral500)
                        )
                    }
                }
            }
        }
        // GROUP 1: KEAMANAN & AKSES APLIKASI
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderCol),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.settings_section_security), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Coral600)

                    // Row 1: PIN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_pin_lock), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(
                                text = if (isPinConfigured) stringResource(R.string.settings_pin_active) else stringResource(R.string.settings_pin_unset),
                                fontSize = 13.sp,
                                color = textSecondary
                            )
                        }
                        Button(
                            onClick = onOpenPin,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Coral500),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(stringResource(R.string.settings_set_pin), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = borderCol)

                    // Row 2: Biometric (Sidik Jari / Wajah)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_biometric_lock), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(stringResource(R.string.settings_biometric_lock_subtitle), fontSize = 13.sp, color = textSecondary)
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = {
                                isBiometricEnabled = it
                                onToast(if (it) resources.getString(R.string.settings_biometric_enabled) else resources.getString(R.string.settings_biometric_disabled))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Coral500)
                        )
                    }

                    HorizontalDivider(color = borderCol)

                    // Row 3: Auto-Lock Timeout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_auto_lock), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(stringResource(R.string.settings_auto_lock_subtitle), fontSize = 13.sp, color = textSecondary)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDarkMode) DarkBackground else Slate100
                        ) {
                            Text(stringResource(R.string.settings_auto_lock_30_seconds), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }

        // GROUP 2: PRIVASI & CADANGAN DATA
        // GROUP 2: CADANGAN DATA MANDIRI
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderCol),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.settings_section_backup), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Coral600)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.settings_backup_recovery_key), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text(
                            text = stringResource(R.string.settings_copy),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Coral600,
                            modifier = Modifier.clickable { onCopyRecoveryKey?.invoke() ?: onToast(resources.getString(R.string.settings_toast_recovery_key_copied, anonymousRecoveryKey)) }
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDarkMode) DarkBackground else Slate50,
                        border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = anonymousRecoveryKey,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = textSecondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Text(stringResource(R.string.settings_backup_recovery_key_hint), fontSize = 12.sp, color = textSecondary)

                    HorizontalDivider(color = borderCol)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_personal_data_backup), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(stringResource(R.string.settings_personal_data_backup_description), fontSize = 13.sp, color = textSecondary, lineHeight = 15.sp)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Text(
                            text = stringResource(R.string.settings_backup_privacy_note),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                backupPinInput = ""
                                isBackupEncrypted = false
                                isBackupOptionsDialogOpen = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Coral600, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.settings_backup_data), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                onRestoreLocal?.invoke() ?: onToast(resources.getString(R.string.settings_opening_file_manager))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MedicalTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.settings_restore_data), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // GROUP 3: TAMPILAN & PREFERENSI
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderCol),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.settings_section_display), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Coral600)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_discreet_mode), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(stringResource(R.string.settings_discreet_mode_subtitle), fontSize = 12.sp, color = textSecondary)
                        }
                        Switch(
                            checked = isDiscreet,
                            onCheckedChange = { onToggleDiscreet() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Coral500)
                        )
                    }
                    HorizontalDivider(color = borderCol)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_oled_dark_mode), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(stringResource(R.string.settings_oled_dark_mode_subtitle), fontSize = 12.sp, color = textSecondary)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onToggleDark() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Coral500)
                        )
                    }
                    HorizontalDivider(color = borderCol)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_bbt_reminder), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(stringResource(R.string.settings_bbt_reminder_subtitle), fontSize = 12.sp, color = textSecondary)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF1F2),
                            border = BorderStroke(1.dp, Color(0xFFFFE4E6))
                        ) {
                            Text("05:30", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Coral700, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }
            }
        }

        // GROUP 4: PANDUAN SINGKAT & EDUKASI MEDIS FIGO
        item {
            var isGuideExpanded by remember { mutableStateOf(false) }
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderCol),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isGuideExpanded = !isGuideExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFF1F2),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Coral600, modifier = Modifier.size(17.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(stringResource(R.string.settings_quick_guide_faq), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Text(stringResource(R.string.settings_quick_guide_subtitle), fontSize = 12.sp, color = textSecondary)
                            }
                        }
                        Icon(
                            imageVector = if (isGuideExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Slate500
                        )
                    }

                    if (isGuideExpanded) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = borderCol)
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Point 1: Cara Catat Haid
                            Column {
                                Text(stringResource(R.string.settings_faq_1_title), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Coral600)
                                Text(
                                    text = stringResource(R.string.settings_faq_1_body_prefix) + stringResource(R.string.settings_faq_1_body_suffix),
                                    fontSize = 12.sp,
                                    color = textSecondary,
                                    lineHeight = 15.sp
                                )
                            }

                            // Point 2: Mekanisme Prediksi Dinamis
                            Column {
                                Text(stringResource(R.string.settings_faq_2_title), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Coral600)
                                Text(
                                    text = stringResource(R.string.settings_faq_2_body),
                                    fontSize = 12.sp,
                                    color = textSecondary,
                                    lineHeight = 15.sp
                                )
                            }

                            // Point 3: Arti Warna Kalender
                            Column {
                                Text(stringResource(R.string.settings_faq_3_title), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Coral600)
                                Text(
                                    text = stringResource(R.string.settings_faq_3_body),
                                    fontSize = 12.sp,
                                    color = textSecondary,
                                    lineHeight = 15.sp
                                )
                            }

                            // Point 4: Standar Medis FIGO
                            Column {
                                Text(stringResource(R.string.settings_faq_4_title), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Coral600)
                                Text(
                                    text = stringResource(R.string.settings_faq_4_body),
                                    fontSize = 12.sp,
                                    color = textSecondary,
                                    lineHeight = 15.sp
                                )
                            }

                            // Tautan Dokumentasi Resmi Lengkap
                            Surface(
                                onClick = {
                                    try {
                                        uriHandler.openUri("https://asridigital.com/cyclejournal/docs")
                                    } catch (e: Exception) {
                                        onToast(resources.getString(R.string.settings_opening_browser))
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDarkMode) DarkBackground else Slate100,
                                border = BorderStroke(1.dp, borderCol),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Coral600, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(stringResource(R.string.settings_documentation_references), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                            Text("asridigital.com/cyclejournal/docs", fontSize = 12.sp, color = textSecondary)
                                        }
                                    }
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Coral600, modifier = Modifier.size(15.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Danger Zone Nuke
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFFFFF1F2),
                border = BorderStroke(1.dp, Color(0xFFFFE4E6))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.settings_section_delete_all_data), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MedicalRose)
                    Text(
                        text = stringResource(R.string.settings_delete_all_data_description),
                        fontSize = 12.sp,
                        color = Color(0xFF9F1239),
                        lineHeight = 15.sp
                    )
                    Button(
                        onClick = {
                            nukeKeyword = ""
                            nukePin = ""
                            isNukePinWrong = false
                            nukeStage = NukeStage.Confirm
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalRose),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_delete_all_data_button), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // App Version Footer
        item {
            Text(
                text = stringResource(R.string.app_version_footer, BuildConfig.VERSION_NAME),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Slate400,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
            )
        }
    }
        if (isBackupOptionsDialogOpen) {
            AlertDialog(
                onDismissRequest = { isBackupOptionsDialogOpen = false },
                title = {
                    Text(stringResource(R.string.settings_backup_data_self_managed), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.settings_backup_lock_method_prompt),
                            fontSize = 14.sp,
                            color = textSecondary
                        )

                        // Option 1: Standar
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (!isBackupEncrypted) Color(0xFFFFF1F2) else if (isDarkMode) DarkBackground else Slate100,
                            border = BorderStroke(1.dp, if (!isBackupEncrypted) Coral400 else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isBackupEncrypted = false }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = !isBackupEncrypted,
                                    onClick = { isBackupEncrypted = false },
                                    colors = RadioButtonDefaults.colors(selectedColor = Coral500)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(stringResource(R.string.settings_backup_lock_standard), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (!isBackupEncrypted) Coral600 else textPrimary)
                                    Text(stringResource(R.string.settings_backup_lock_standard_description), fontSize = 12.sp, color = textSecondary, lineHeight = 14.sp)
                                }
                            }
                        }

                        // Option 2: Terenkripsi PIN
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isBackupEncrypted) Color(0xFFFFF1F2) else if (isDarkMode) DarkBackground else Slate100,
                            border = BorderStroke(1.dp, if (isBackupEncrypted) Coral400 else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isBackupEncrypted = true }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isBackupEncrypted,
                                        onClick = { isBackupEncrypted = true },
                                        colors = RadioButtonDefaults.colors(selectedColor = Coral500)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(stringResource(R.string.settings_backup_lock_encrypted), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isBackupEncrypted) Coral600 else textPrimary)
                                        Text(stringResource(R.string.settings_backup_lock_encrypted_description), fontSize = 12.sp, color = textSecondary, lineHeight = 14.sp)
                                    }
                                }
                                if (isBackupEncrypted) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = backupPinInput,
                                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) backupPinInput = it },
                                        placeholder = { Text(stringResource(R.string.settings_pin_entry_hint)) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = !isBackupEncrypted || backupPinInput.length == 4,
                        onClick = {
                            isBackupOptionsDialogOpen = false
                            onBackupLocal?.invoke(
                                isBackupEncrypted,
                                if (isBackupEncrypted) backupPinInput else null
                            ) ?: onToast(resources.getString(R.string.settings_creating_backup_file))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Coral500),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(R.string.settings_create_backup), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isBackupOptionsDialogOpen = false }) {
                        Text(stringResource(R.string.settings_cancel))
                    }
                }
            )
        }
        val isNukeKeywordMatched = NUKE_KEYWORDS.any { it.equals(nukeKeyword.trim(), ignoreCase = true) }
        val canConfirmNuke = isNukeKeywordMatched && (!isPinConfigured || nukePin.length == 4)
        if (nukeStage == NukeStage.Confirm) {
            AlertDialog(
                onDismissRequest = { nukeStage = NukeStage.Closed },
                title = {
                    Text(stringResource(R.string.nuke_confirm_title), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MedicalRose)
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_nuke_local_message),
                            fontSize = 14.sp,
                            color = textSecondary,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = stringResource(R.string.nuke_confirm_consequences),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalRose,
                            lineHeight = 15.sp
                        )
                        OutlinedTextField(
                            value = nukeKeyword,
                            onValueChange = { nukeKeyword = it },
                            label = { Text(stringResource(R.string.nuke_confirm_hint), fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (isPinConfigured) {
                            Text(
                                text = stringResource(R.string.nuke_pin_prompt),
                                fontSize = 13.sp,
                                color = textSecondary
                            )
                            OutlinedTextField(
                                value = nukePin,
                                onValueChange = { input ->
                                    if (input.length <= 4 && input.all { it.isDigit() }) {
                                        nukePin = input
                                        isNukePinWrong = false
                                    }
                                },
                                placeholder = { Text("\u2022\u2022\u2022\u2022", fontSize = 14.sp) },
                                isError = isNukePinWrong,
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (isNukePinWrong) {
                                Text(
                                    text = stringResource(R.string.nuke_wrong_pin),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedicalRose
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (onNukeData(nukePin)) {
                                nukeStage = NukeStage.Wiping
                            } else {
                                isNukePinWrong = true
                                nukePin = ""
                            }
                        },
                        enabled = canConfirmNuke,
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalRose),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(R.string.nuke_confirm_action), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = {
                                nukeStage = NukeStage.Closed
                                isBackupOptionsDialogOpen = true
                            }
                        ) {
                            Text(stringResource(R.string.nuke_backup_first), fontSize = 14.sp, color = Coral600)
                        }
                        TextButton(onClick = { nukeStage = NukeStage.Closed }) {
                            Text(stringResource(R.string.settings_cancel), fontSize = 14.sp)
                        }
                    }
                }
            )
        }
        if (nukeStage == NukeStage.Wiping) {
            AlertDialog(
                onDismissRequest = { },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                title = {
                    Text(stringResource(R.string.nuke_wiping_title), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MedicalRose)
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MedicalRose, strokeWidth = 2.dp)
                        Text(stringResource(R.string.nuke_wiping_message), fontSize = 14.sp, color = textSecondary)
                    }
                },
                confirmButton = { }
            )
        }
}

// 6. DAILY LOG BOTTOM SHEET: Pre-populated with real database record
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLogBottomSheet(
    isDarkMode: Boolean,
    targetDate: LocalDate = LocalDate.now(),
    initialLog: DailyLogEntity? = null,
    onDismiss: () -> Unit,
    onSave: (DailyLogEntity) -> Unit
) {
    val resources = LocalContext.current.resources
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500
    var vasScore by remember(initialLog) { mutableStateOf((initialLog?.painVasScore ?: 0).toFloat()) }
    var selectedFlow by remember(initialLog) { mutableStateOf(initialLog?.flow ?: FlowIntensity.NONE) }
    var selectedMucus by remember(initialLog) { mutableStateOf(initialLog?.cervicalMucus ?: CervicalMucusType.NONE) }
    var hasTakenAnalgesic by remember(initialLog) { mutableStateOf(initialLog?.takenAnalgesic ?: false) }
    var isBbtRecorded by remember(initialLog) {
        mutableStateOf(initialLog?.basalBodyTempCelsius != null)
    }
    var bbtInputText by remember(initialLog) {
        mutableStateOf(initialLog?.basalBodyTempCelsius?.let { String.format(Locale.US, "%.2f", it) } ?: "36.50")
    }
    var isBbtDialogOpen by remember { mutableStateOf(false) }
    var tempBbtDialogInput by remember { mutableStateOf("") }

    val context = LocalContext.current
    val customPrefs = remember { context.getSharedPreferences("custom_symptoms_store", Context.MODE_PRIVATE) }

    val defaultSymptoms = listOf(
        stringResource(R.string.daily_symptom_pelvic_cramps), stringResource(R.string.daily_symptom_lower_back_pain), stringResource(R.string.daily_symptom_tender_breasts), stringResource(R.string.daily_symptom_headache),
        stringResource(R.string.daily_symptom_bloating), stringResource(R.string.daily_symptom_sensitive_mood), stringResource(R.string.daily_symptom_fatigue), stringResource(R.string.daily_symptom_nausea)
    )

    val customSymptomsList = remember {
        mutableStateListOf<String>().apply {
            val saved = customPrefs.getStringSet("custom_symptoms", emptySet()) ?: emptySet()
            addAll(saved)
        }
    }

    var isAddCustomDialogOpen by remember { mutableStateOf(false) }
    var newCustomSymptomInput by remember { mutableStateOf("") }
    val storedNotes = remember(initialLog) {
        val raw = initialLog?.notes ?: ""
        if (raw.contains(resources.getString(R.string.daily_notes_prefix))) {
            raw.substringAfter(resources.getString(R.string.daily_notes_prefix)).trim()
        } else if (!raw.startsWith(resources.getString(R.string.daily_symptoms_prefix))) {
            raw
        } else {
            ""
        }
    }
    var clinicalNotesInput by remember(initialLog) { mutableStateOf(storedNotes) }

    val allAvailableSymptoms = remember(customSymptomsList.size) {
        (defaultSymptoms + customSymptomsList).distinct()
    }

    val selectedSymptoms = remember(initialLog) {
        val list = mutableStateListOf<String>()
        initialLog?.painLocation?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.let {
            list.addAll(it)
        }
        list
    }

    val storedSymptoms = remember(initialLog) {
        initialLog?.painLocation?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }
    val storedBbt = remember(initialLog) {
        initialLog?.basalBodyTempCelsius?.let { String.format(Locale.US, "%.2f", it) }
    }

    val isPeriodFlow = selectedFlow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY)
    val detailCount = listOf(
        selectedSymptoms.isNotEmpty(),
        isBbtRecorded,
        selectedMucus != CervicalMucusType.NONE,
        hasTakenAnalgesic,
        clinicalNotesInput.isNotBlank()
    ).count { it }

    // The slower clinical fields start collapsed: a day without complaints stays two taps.
    // They open on their own only when the stored log for this date already carries detail.
    var isDetailExpanded by remember(initialLog) {
        mutableStateOf(initialLog != null && detailCount > 0)
    }

    val isDirty = vasScore.toInt() != (initialLog?.painVasScore ?: 0) ||
        selectedFlow != (initialLog?.flow ?: FlowIntensity.NONE) ||
        selectedMucus != (initialLog?.cervicalMucus ?: CervicalMucusType.NONE) ||
        hasTakenAnalgesic != (initialLog?.takenAnalgesic ?: false) ||
        isBbtRecorded != (initialLog?.basalBodyTempCelsius != null) ||
        (isBbtRecorded && bbtInputText != storedBbt) ||
        selectedSymptoms.toList() != storedSymptoms ||
        clinicalNotesInput.trim() != storedNotes

    var showDiscardDialog by remember { mutableStateOf(false) }
    val requestDismiss: () -> Unit = { if (isDirty) showDiscardDialog = true else onDismiss() }

    val symptomsPrefix = stringResource(R.string.daily_notes_symptoms_prefix)
    val notePrefix = stringResource(R.string.daily_notes_note_prefix)
    val saveLog: () -> Unit = {
        onSave(
            DailyLogEntity(
                date = targetDate,
                flow = selectedFlow,
                basalBodyTempCelsius = if (isBbtRecorded) bbtInputText.replace(",", ".").toDoubleOrNull() else null,
                cervicalMucus = selectedMucus,
                painVasScore = vasScore.toInt(),
                painLocation = selectedSymptoms.takeIf { it.isNotEmpty() }?.joinToString(", "),
                takenAnalgesic = hasTakenAnalgesic,
                notes = buildString {
                    if (selectedSymptoms.isNotEmpty()) append(symptomsPrefix + selectedSymptoms.joinToString(", "))
                    if (clinicalNotesInput.isNotBlank()) {
                        if (isNotEmpty()) append(notePrefix)
                        append(clinicalNotesInput.trim())
                    }
                }.takeIf { it.isNotBlank() }
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = requestDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (isDarkMode) DarkCardBackground else Color.White
    ) {
        // Sheet frame: scrollable content on top, pinned save action below.
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                // The scroll area takes the remaining height so the save action stays pinned
                // at the bottom of the sheet instead of hiding below the fold.
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.daily_log_title), fontSize = 17.sp, fontWeight = FontWeight.Black)
                        Text(
                            text = "${targetDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}, ${targetDate.dayOfMonth} ${targetDate.monthName()} ${targetDate.year}",
                            fontSize = 13.sp,
                            color = Slate400
                        )
                    }
                    IconButton(onClick = requestDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Slate400)
                    }
                }

                // Flow — the primary question. Chips wrap instead of squeezing five equal columns.
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.daily_flow_title), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isPeriodFlow) stringResource(R.string.daily_flow_active) else stringResource(R.string.daily_flow_inactive),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPeriodFlow) Coral600 else Slate400
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            FlowIntensity.NONE to stringResource(R.string.app_option_no),
                            FlowIntensity.SPOTTING to stringResource(R.string.app_column_spotting),
                            FlowIntensity.LIGHT to stringResource(R.string.dash_vas_mild),
                            FlowIntensity.MEDIUM to stringResource(R.string.app_option_medium),
                            FlowIntensity.HEAVY to stringResource(R.string.app_option_heavy)
                        ).forEach { (flow, label) ->
                            val isSelected = flow == selectedFlow
                            Surface(
                                modifier = Modifier.clickable { selectedFlow = flow },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Coral500 else if (isDarkMode) DarkBackground else Slate100,
                                border = BorderStroke(1.dp, if (isSelected) Coral500 else if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Slate600,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                                )
                            }
                        }
                    }
                    // Guidance matters only while the day is not logged as a period day.
                    if (!isPeriodFlow) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.daily_flow_hint),
                            fontSize = 12.sp,
                            color = Slate400,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Clinical Pain VAS Card (Refined functional rating)
                val num = vasScore.toInt()
                val (badgeText, cardBg, textCol, impactText) = when {
                    num == 0 -> Quadruple(stringResource(R.string.app_pain_free), Color(0xFFECFDF5), Color(0xFF047857), stringResource(R.string.daily_pain_impact_none))
                    num <= 3 -> Quadruple(stringResource(R.string.dash_vas_mild), Slate100, Slate700, stringResource(R.string.daily_pain_impact_mild))
                    num <= 6 -> Quadruple(stringResource(R.string.daily_pain_badge_monitor), Color(0xFFFFFBEB), Color(0xFFB45309), stringResource(R.string.daily_pain_impact_moderate))
                    num <= 8 -> Quadruple(stringResource(R.string.daily_pain_badge_attention), Color(0xFFFFF1F2), Color(0xFFBE123C), stringResource(R.string.daily_pain_impact_severe))
                    else -> Quadruple(stringResource(R.string.daily_pain_badge_consult), Color(0xFFFEE2E2), Color(0xFF991B1B), stringResource(R.string.daily_pain_impact_critical))
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, textCol.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.daily_pain_scale_title), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textCol)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = textCol.copy(alpha = 0.15f)
                            ) {
                                Text(badgeText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textCol, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text("$num / 10", fontSize = 16.sp, fontWeight = FontWeight.Black, color = textCol)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = impactText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textCol,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Slider(
                            value = vasScore,
                            onValueChange = { vasScore = it },
                            valueRange = 0f..10f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = textCol,
                                activeTrackColor = textCol
                            )
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDetailExpanded = !isDetailExpanded },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDarkMode) DarkBackground else Slate50,
                    border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(if (isDetailExpanded) R.string.daily_detail_hide else R.string.daily_detail_show),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (detailCount == 0) stringResource(R.string.daily_detail_subtitle_empty)
                                else stringResource(R.string.daily_detail_subtitle_filled, detailCount),
                                fontSize = 12.sp,
                                color = Slate400,
                                lineHeight = 16.sp
                            )
                        }
                        Icon(
                            imageVector = if (isDetailExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Slate400
                        )
                    }
                }

                if (isDetailExpanded) {
                    // Symptom chips with custom symptom support
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.daily_body_symptoms_title), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = stringResource(R.string.daily_symptoms_selected_format, selectedSymptoms.size),
                                fontSize = 12.sp,
                                color = Coral600,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allAvailableSymptoms.forEach { sym ->
                                val isSelected = selectedSymptoms.contains(sym)
                                val isCustom = customSymptomsList.contains(sym)
                                Surface(
                                    modifier = Modifier.clickable {
                                        if (isSelected) selectedSymptoms.remove(sym) else selectedSymptoms.add(sym)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Color(0xFFFFF1F2) else if (isDarkMode) DarkBackground else Slate100,
                                    border = BorderStroke(1.dp, if (isSelected) Coral400 else Color.Transparent)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isCustom) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(Coral600)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = sym,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Coral600 else Slate600
                                        )
                                    }
                                }
                            }

                            // + Tambah Gejala Button
                            Surface(
                                modifier = Modifier.clickable {
                                    newCustomSymptomInput = ""
                                    isAddCustomDialogOpen = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, Coral500)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Coral500, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.daily_add_symptom),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Coral500
                                    )
                                }
                            }
                        }
                    }

                    // BBT Number Stepper & Analgesic Checkbox
                    Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDarkMode) DarkBackground else Slate50,
                            border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(stringResource(R.string.daily_basal_temp_title), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
                                    Text(
                                        text = if (isBbtRecorded) stringResource(R.string.daily_basal_temp_remove) else stringResource(R.string.daily_basal_temp_record),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBbtRecorded) Coral600 else Color(0xFF059669),
                                        modifier = Modifier.clickable {
                                            isBbtRecorded = !isBbtRecorded
                                            if (isBbtRecorded && bbtInputText.isBlank()) {
                                                bbtInputText = "36.50"
                                            }
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                if (isBbtRecorded) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isDarkMode) DarkCardBackground else Slate200,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clickable {
                                                    val current = bbtInputText.toDoubleOrNull() ?: 36.50
                                                    val next = (current - 0.10).coerceAtLeast(35.00)
                                                    bbtInputText = String.format(Locale.US, "%.2f", next)
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("-", fontSize = 16.sp, fontWeight = FontWeight.Black, color = textPrimary)
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isDarkMode) DarkCardBackground else Color(0xFFFFF1F2),
                                            border = BorderStroke(1.dp, Coral400),
                                            modifier = Modifier.clickable {
                                                tempBbtDialogInput = bbtInputText
                                                isBbtDialogOpen = true
                                            }
                                        ) {
                                            Text(
                                                text = "$bbtInputText °C",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                color = Coral600,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = if (isDarkMode) DarkCardBackground else Slate200,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clickable {
                                                    val current = bbtInputText.toDoubleOrNull() ?: 36.50
                                                    val next = (current + 0.10).coerceAtMost(39.50)
                                                    bbtInputText = String.format(Locale.US, "%.2f", next)
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("+", fontSize = 16.sp, fontWeight = FontWeight.Black, color = textPrimary)
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        text = stringResource(R.string.daily_temp_not_measured),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate400,
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .clickable { isBbtRecorded = true }
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDarkMode) DarkBackground else Slate50,
                            border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(stringResource(R.string.daily_analgesic_label), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
                                    Text(stringResource(R.string.daily_take_medication), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Checkbox(
                                    checked = hasTakenAnalgesic,
                                    onCheckedChange = { hasTakenAnalgesic = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Coral500)
                                )
                            }
                        }
                    }

                    // Cervical Mucus Selector
                    // Cervical Mucus Selector — chips wrap, so longer labels never get clipped.
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.daily_cervical_mucus_title), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                CervicalMucusType.NONE to stringResource(R.string.app_option_no),
                                CervicalMucusType.DRY to stringResource(R.string.mucus_dry),
                                CervicalMucusType.STICKY to stringResource(R.string.mucus_sticky),
                                CervicalMucusType.CREAMY to stringResource(R.string.mucus_creamy),
                                CervicalMucusType.WATERY to stringResource(R.string.mucus_watery),
                                CervicalMucusType.EGG_WHITE to stringResource(R.string.mucus_egg_white)
                            ).forEach { (mucus, label) ->
                                val isSelected = mucus == selectedMucus
                                Surface(
                                    modifier = Modifier.clickable { selectedMucus = mucus },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Color(0xFFFFF1F2) else if (isDarkMode) DarkBackground else Slate100,
                                    border = BorderStroke(1.dp, if (isSelected) Coral400 else if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Coral600 else Slate600,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                                    )
                                }
                            }
                        }
                    }



                    // Clinical Notes Field
                    Column {
                        Text(stringResource(R.string.daily_notes_title), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = clinicalNotesInput,
                            onValueChange = { clinicalNotesInput = it },
                            placeholder = { Text(stringResource(R.string.daily_notes_hint), fontSize = 13.sp, color = Slate400) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2,
                            maxLines = 3
                        )
                    }
                } // end detail
            }

            // Layer 3 — one primary action, always reachable without scrolling.
            Surface(
                color = if (isDarkMode) DarkCardBackground else Color.White,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 10.dp, bottom = 20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.daily_save_hint),
                        fontSize = 12.sp,
                        color = Slate400,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = saveLog,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Coral500)
                    ) {
                        Text(stringResource(R.string.daily_save_log), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        if (showDiscardDialog) {
            AlertDialog(
                onDismissRequest = { showDiscardDialog = false },
                title = { Text(stringResource(R.string.daily_discard_title), fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = { Text(stringResource(R.string.daily_discard_message), fontSize = 13.sp, color = textSecondary) },
                confirmButton = {
                    TextButton(onClick = {
                        showDiscardDialog = false
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.daily_discard_confirm), fontSize = 13.sp, color = Coral600, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscardDialog = false }) {
                        Text(stringResource(R.string.daily_discard_keep), fontSize = 13.sp)
                    }
                }
            )
        }

        if (isAddCustomDialogOpen) {
            AlertDialog(
                onDismissRequest = { isAddCustomDialogOpen = false },
                title = { Text(stringResource(R.string.daily_custom_symptom_title), fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(stringResource(R.string.daily_custom_symptom_prompt), fontSize = 14.sp, color = Slate500)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newCustomSymptomInput,
                            onValueChange = { newCustomSymptomInput = it },
                            placeholder = { Text(stringResource(R.string.daily_custom_symptom_hint), fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val trimmed = newCustomSymptomInput.trim()
                            if (trimmed.isNotEmpty()) {
                                if (!customSymptomsList.contains(trimmed)) {
                                    customSymptomsList.add(trimmed)
                                    val currentSet = customPrefs.getStringSet("custom_symptoms", emptySet())?.toMutableSet() ?: mutableSetOf()
                                    currentSet.add(trimmed)
                                    customPrefs.edit().putStringSet("custom_symptoms", currentSet).apply()
                                }
                                if (!selectedSymptoms.contains(trimmed)) {
                                    selectedSymptoms.add(trimmed)
                                }
                            }
                            isAddCustomDialogOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Coral500)
                    ) {
                        Text(stringResource(R.string.daily_save), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isAddCustomDialogOpen = false }) {
                        Text(stringResource(R.string.settings_cancel), fontSize = 14.sp)
                    }
                }
            )
        }
        if (isBbtDialogOpen) {
            AlertDialog(
                onDismissRequest = { isBbtDialogOpen = false },
                title = { Text(stringResource(R.string.daily_basal_temp_dialog_title), fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(stringResource(R.string.daily_basal_temp_dialog_hint), fontSize = 14.sp, color = textSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = tempBbtDialogInput,
                            onValueChange = { tempBbtDialogInput = it },
                            placeholder = { Text("36.50") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val parsed = tempBbtDialogInput.replace(",", ".").toDoubleOrNull()
                        if (parsed != null && parsed in 34.0..42.0) {
                            bbtInputText = String.format(Locale.US, "%.2f", parsed)
                            isBbtRecorded = true
                        }
                        isBbtDialogOpen = false
                    }) {
                        Text(stringResource(R.string.daily_save), fontWeight = FontWeight.Bold, color = Coral600)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isBbtDialogOpen = false }) {
                        Text(stringResource(R.string.settings_cancel))
                    }
                }
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

// 7. FLOATING ACTION BUTTON (Tombol + di Navigasi Bawah): Elevated with zIndex(2f) without rectangular clipping
@Composable
fun AppBottomNavigation(
    currentScreen: AppScreen,
    isDarkMode: Boolean,
    onSelect: (AppScreen) -> Unit,
    onOpenFab: () -> Unit
) {
    val navBg = if (isDarkMode) DarkCardBackground.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.96f)
    val borderCol = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            // Fixed height on purpose: the Row inside fills the height, so an unbounded
            // height would let it grow and squeeze the screen content to nothing.
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            color = navBg,
            border = BorderStroke(1.dp, borderCol),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavButton(Icons.Default.Home, stringResource(R.string.nav_home), currentScreen == AppScreen.DASHBOARD) { onSelect(AppScreen.DASHBOARD) }
                NavButton(Icons.Default.DateRange, stringResource(R.string.nav_calendar), currentScreen == AppScreen.CALENDAR) { onSelect(AppScreen.CALENDAR) }

                // Spacer to reserve central notch space for the elevated FAB
                Spacer(modifier = Modifier.size(54.dp))

                NavButton(Icons.Default.Description, stringResource(R.string.nav_report), currentScreen == AppScreen.REPORT) { onSelect(AppScreen.REPORT) }
                NavButton(Icons.Default.Settings, stringResource(R.string.nav_settings), currentScreen == AppScreen.SETTINGS) { onSelect(AppScreen.SETTINGS) }
            }
        }

        // Elevated Floating Central Add Button - Placed ABOVE navigation layer with zIndex(2f)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
                .zIndex(2f)
                .size(54.dp)
                .shadow(12.dp, CircleShape, spotColor = Coral600)
                .clip(CircleShape)
                .background(CoralLinearGradient)
                .clickable { onOpenFab() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.nav_log_daily),
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun NavButton(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClick() }
    ) {
        Icon(icon, contentDescription = label, tint = if (isSelected) Coral600 else Slate400, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Coral600 else Slate400)
    }
}

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
