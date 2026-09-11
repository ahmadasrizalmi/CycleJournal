package com.app.cyclejournal.ui
import com.app.cyclejournal.export.pdf.PdfShareHelper
import com.app.cyclejournal.ui.home.CycleFilter
import com.app.cyclejournal.ui.report.CycleHistoryRow

import androidx.compose.animation.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import android.content.Context
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.app.cyclejournal.R
import com.app.cyclejournal.BuildConfig
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import java.util.Locale

enum class AppScreen {
    DASHBOARD, CALENDAR, REPORT, SETTINGS
}

enum class CyclePhase {
    MENSTRUATION, FOLLICULAR, FERTILE, OVULATION, LUTEAL
}

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

private fun mucusLabelFor(type: CervicalMucusType): String = when (type) {
    CervicalMucusType.NONE -> "Tidak ada"
    CervicalMucusType.DRY -> "Kering"
    CervicalMucusType.STICKY -> "Lengket"
    CervicalMucusType.CREAMY -> "Krim"
    CervicalMucusType.WATERY -> "Cair"
    CervicalMucusType.EGG_WHITE -> "Putih Telur"
}

@Composable
fun CycleJournalApp(
    onSharePdf: (() -> Unit)? = null,
    onExportCsv: (() -> Unit)? = null,
    onBuyPro: (() -> Unit)? = null,
    onBackupLocal: ((Boolean, String?) -> Unit)? = null,
    onRestoreLocal: (() -> Unit)? = null,
    onNukeData: (() -> Unit)? = null,
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
    onDismissDownloadDialog: () -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var isDarkMode by remember { mutableStateOf(false) }
    var isDiscreetMode by remember { mutableStateOf(false) }
    var isPinModalOpen by remember { mutableStateOf(false) }
    var isLogModalOpen by remember { mutableStateOf(false) }
    var logModalDate by remember { mutableStateOf(LocalDate.now()) }
    var isProLicenseActive by remember(isProUserActive) { mutableStateOf(isProUserActive) }
    var pinStatus by remember(isPinSet) { mutableStateOf(if (isPinSet) "Aktif (PIN 4-Digit)" else "Belum diatur (Opsional)") }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var isPromilMode by remember(isPromilModeInitial) { mutableStateOf(isPromilModeInitial) }

    val today = remember { LocalDate.now() }
    var selectedCalendarDate by remember { mutableStateOf(today) }
    // Dynamically build week days from real backend data (Monday to Sunday around today)
    val weekDays = remember(today, allLogs, periodDates, fertilePrediction, latestCycle) {
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        (0..6).map { offset ->
            val date = monday.plusDays(offset.toLong())
            val dayName = when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "Sen"
                DayOfWeek.TUESDAY -> "Sel"
                DayOfWeek.WEDNESDAY -> "Rab"
                DayOfWeek.THURSDAY -> "Kam"
                DayOfWeek.FRIDAY -> "Jum"
                DayOfWeek.SATURDAY -> "Sab"
                DayOfWeek.SUNDAY -> "Min"
                else -> "Sen"
            }
            val log = allLogs.find { it.date == date }
            val bbt = log?.basalBodyTempCelsius?.let { String.format(Locale.US, "%.2f °C", it) } ?: "-- °C"
            val pain = log?.let {
                if (it.painVasScore > 0) "VAS ${it.painVasScore}" else "Bebas Nyeri"
            } ?: "Bebas Nyeri"
            val mucus = log?.cervicalMucus?.let { mucusLabelFor(it) } ?: "Kering"

            val periodDays = (latestCycle?.periodDurationDays ?: 5).toLong().coerceAtLeast(1L)
            val isHaid = date in periodDates || (latestCycle != null && !date.isBefore(latestCycle.startDate) && date.isBefore(latestCycle.startDate.plusDays(periodDays)))
            val isOvulation = fertilePrediction != null && date == fertilePrediction.predictedOvulationDate
            val isFertile = fertilePrediction != null && !date.isBefore(fertilePrediction.fertileWindowStart) && !date.isAfter(fertilePrediction.fertileWindowEnd)

            val (phase, dotColor, title) = when {
                isHaid -> Triple(CyclePhase.MENSTRUATION, Color(0xFFFB7185), "Fase Haid")
                isOvulation -> Triple(CyclePhase.OVULATION, MedicalTeal, "Puncak Ovulasi")
                isFertile -> Triple(CyclePhase.FERTILE, Coral500, "Jendela Subur")
                else -> Triple(CyclePhase.FOLLICULAR, Color(0xFFCBD5E1), "Fase Folikuler")
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
                        showToast(if (isDiscreetMode) "Mode Samaran Aktif: Istilah sensitif disamarkan" else "Mode Standar Aktif")
                    },
                    onToggleDarkMode = {
                        isDarkMode = !isDarkMode
                        showToast(if (isDarkMode) "Mode Subuh Gelap Aktif (Ramah Mata)" else "Mode Terang Aktif")
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
                            showToast("Menampilkan data ${it.dayOfMonth} ${it.date.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${it.date.year}")
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
                                showToast("Menyiapkan Berkas Rekap Siklus...")
                            }
                        },
                        onExportCsv = {
                            onExportCsv?.invoke() ?: showToast("Mengekspor Berkas CSV Mentah")
                        },
                        onBuyPro = {
                            onBuyPro?.invoke() ?: run {
                                isProLicenseActive = true
                                showToast("Google Play Billing: Lisensi Pro Aktif Selamanya!")
                            }
                        },
                        onNavigateToCalendar = { date ->
                            selectedCalendarDate = date
                            currentScreen = AppScreen.CALENDAR
                            showToast("Membuka ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} di Kalender")
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
                        pinStatus = pinStatus,
                        anonymousRecoveryKey = anonymousRecoveryKey,
                        onToggleDiscreet = { isDiscreetMode = !isDiscreetMode },
                        onToggleDark = { isDarkMode = !isDarkMode },
                        onOpenPin = { isPinModalOpen = true },
                        onBuyPro = {
                            onBuyPro?.invoke() ?: run {
                                isProLicenseActive = true
                                showToast("Google Play Billing: Berhasil Upgrade ke Lifetime Pro!")
                            }
                        },
                        onCopyRecoveryKey = {
                            showToast("Kunci Pemulihan Cadangan Disalin: $anonymousRecoveryKey")
                        },
                        onBackupLocal = onBackupLocal,
                        onRestoreLocal = onRestoreLocal,
                        onNukeData = {
                            onNukeData?.invoke() ?: run {
                                showToast("Seluruh data lokal berhasil dibersihkan")
                                currentScreen = AppScreen.DASHBOARD
                            }
                        },
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
                    fontSize = 12.sp,
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
                    pinStatus = "Aktif (PIN 4-Digit)"
                    isPinModalOpen = false
                    showToast("PIN Keamanan Berhasil Diaktifkan")
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
                    showToast(if (isBleed) "Hari Haid Disimpan • Prediksi Berhasil Dihitung!" else "Catatan Harian Disimpan (Bukan Hari Haid)")
                }
            )
        }
        if (downloadedReport != null) {
            val res = downloadedReport
            val context = LocalContext.current
            val isCsv = res.fileName.endsWith(".csv", ignoreCase = true)
            val isBackup = res.fileName.endsWith(".cjbackup", ignoreCase = true)
            val fileTypeTitle = when {
                isBackup -> "Berkas Cadangan (.cjbackup)"
                isCsv -> "Data CSV (Excel)"
                else -> "Rekap Siklus PDF"
            }
            val openButtonLabel = when {
                isBackup -> "Bagikan ke Drive / Chat"
                isCsv -> "Buka CSV"
                else -> "Buka PDF"
            }

            AlertDialog(
                onDismissRequest = onDismissDownloadDialog,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unduhan Selesai", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "$fileTypeTitle berhasil disimpan di folder Download perangkat:",
                            fontSize = 12.sp,
                            color = if (isDarkMode) Slate400 else Slate600
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDarkMode) DarkBackground else Slate100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Download/CycleJournal/${res.fileName}",
                                fontSize = 11.sp,
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
                                com.app.cyclejournal.export.csv.CsvExportHelper.shareCsv(context, res, "Berkas Cadangan CycleJournal")
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
                            Text("Bagikan")
                        }
                        TextButton(onClick = onDismissDownloadDialog) {
                            Text("Tutup")
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
                contentDescription = "Logo CycleJournal",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = if (isDiscreetMode) "MODE SAMARAN AKTIF" else "MODE PRIVAT OFFLINE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDiscreetMode) Slate400 else Coral600
                )
                Text(
                    text = if (isDiscreetMode) "CJ Journal" else "CycleJournal",
                    fontSize = 17.sp,
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
        val monthStr = it.month.name.lowercase().take(3).replaceFirstChar { c -> c.uppercase() }
        "${it.dayOfMonth} $monthStr"
    } ?: "--"

    val nextPeriodSubtext = when {
        !hasActiveCycle -> "Catat haid pertama"
        daysUntilNextPeriod == null -> "Belum ada prediksi"
        daysUntilNextPeriod > 0 -> "$daysUntilNextPeriod hari lagi"
        daysUntilNextPeriod == 0 -> "Hari ini!"
        else -> "Terlambat ${-daysUntilNextPeriod} hari"
    }

    val periodDays = (latestCycle?.periodDurationDays ?: 5).toLong()
    val subtitleDynamic = when {
        isDiscreet -> "Pencatatan normal berlangsung"
        !hasActiveCycle -> "Catat hari pertama haid untuk mengaktifkan kalkulasi"
        isBleedingToday && currentCycleDay != null -> {
            val remainingDays = (periodDays - currentCycleDay + 1).coerceAtLeast(1L)
            "Perkiraan selesai $remainingDays hari lagi"
        }
        selectedDay.phase == CyclePhase.OVULATION -> "Pelepasan sel telur aktif hari ini"
        isFertileToday -> "Peluang terbaik dalam siklus ini"
        else -> "Kondisi hormon stabil"
    }

    val phaseTitle = when {
        isDiscreet -> if (hasActiveCycle) "Periode Tengah" else "Mulai Jurnal"
        !hasActiveCycle -> "Mulai Jurnal Anda"
        isBleedingToday -> "Fase Menstruasi"
        selectedDay.phase == CyclePhase.OVULATION -> "Puncak Ovulasi"
        isFertileToday -> "Jendela Subur"
        else -> "Fase Folikuler"
    }

    val conceptionChance = when {
        !hasActiveCycle -> "--"
        selectedDay.phase == CyclePhase.OVULATION -> "Puncak Subur"
        isFertileToday -> "Tinggi"
        isBleedingToday -> "Sangat Rendah"
        else -> "Rendah"
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
                                    fontSize = 12.sp,
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
                                    text = "Hari ini",
                                    fontSize = 10.sp,
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
                                    text = "HAID BERIKUTNYA",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.85f),
                                    letterSpacing = 0.5.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = nextPeriodValueStr,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = nextPeriodSubtext,
                                    fontSize = 9.sp,
                                    color = Color(0xFFFFE4E6),
                                    maxLines = 1
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
                                            "MASA SUBUR & OVULASI",
                                            "--",
                                            "Catat haid untuk prediksi",
                                            Color.White
                                        )
                                        // State 3: HARI-H PUNCAK OVULASI
                                        today == ovDate -> Quadruple(
                                            "PUNCAK OVULASI HARI INI",
                                            "Waktu Terbaik Promil",
                                            "Peluang Hamil Maksimal",
                                            Color(0xFFFEF08A)
                                        )
                                        // State 2: JENDELA SUBUR (H-5 s/d H-1 sebelum ovulasi)
                                        fertileStart != null && !today.isBefore(fertileStart) && today.isBefore(ovDate) -> Quadruple(
                                            "JENDELA MASA SUBUR",
                                            "Peluang Tinggi",
                                            "Puncak ovulasi: $ovDateStr (H-${daysUntilOvulation ?: 1})",
                                            Color(0xFFA5F3FC)
                                        )
                                        // State 4: PASCA OVULASI (Masa subur lewat)
                                        today.isAfter(ovDate) -> Quadruple(
                                            "MASA SUBUR SELESAI",
                                            "Peluang Rendah",
                                            "Menunggu siklus baru",
                                            Color.White.copy(alpha = 0.9f)
                                        )
                                        // State 1: MENUJU MASA SUBUR (Countdown)
                                        else -> Quadruple(
                                            "MASA SUBUR & OVULASI",
                                            ovDateStr,
                                            "${daysUntilOvulation ?: 0} hari lagi",
                                            Color(0xFFA5F3FC)
                                        )
                                    }

                                    Text(
                                        text = headerText,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 0.5.sp,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = valueText,
                                        fontSize = if (valueText.length > 10) 11.5.sp else 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = valueColor,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = subText,
                                        fontSize = 9.sp,
                                        color = Color(0xFFFFE4E6),
                                        maxLines = 1
                                    )
                                } else {
                                    Text(
                                        text = "RATA-RATA SIKLUS",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 0.5.sp,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val avgText = when {
                                        cycleStats != null -> String.format(Locale.US, "%.0f Hari", cycleStats.averageLength)
                                        hasActiveCycle -> "28 Hari"
                                        else -> "-- Hari"
                                    }
                                    Text(
                                        text = avgText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    val varText = when {
                                        cycleStats?.standardDeviation != null -> "Variasi ±${String.format(Locale.US, "%.1f", cycleStats.standardDeviation)} hari"
                                        hasActiveCycle -> "Estimasi awal"
                                        else -> "Belum ada riwayat"
                                    }
                                    Text(
                                        text = varText,
                                        fontSize = 9.sp,
                                        color = Color(0xFFFFE4E6),
                                        maxLines = 1
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
                                text = "Minggu Ini • ${today.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${today.year}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Coral500))
                        }

                        Text(
                            text = "Buka Kalender Penuh >",
                            fontSize = 11.sp,
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
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color(0xFFFCA5A5) else if (isPeak) MedicalTeal else Slate400
                                )
                                Text(
                                    text = "${item.dayOfMonth}",
                                    fontSize = 13.sp,
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                            Column {
                                Text("Tren Kurva Suhu Basal (BBT)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Text("Pantauan Pergeseran Biphasik (3-over-6)", fontSize = 10.sp, color = textSecondary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (hasRealBbt) Color(0xFFECFDF5) else Slate100,
                            border = BorderStroke(1.dp, if (hasRealBbt) Color(0xFFA7F3D0) else Slate200)
                        ) {
                            Text(
                                text = if (hasRealBbt) "Normal" else "Belum Ada Data",
                                fontSize = 10.sp,
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
                            text = "Coverline 36.40°C",
                            fontSize = 9.sp,
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
                                text = "Belum ada rekaman suhu BBT harian.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Ukur suhu basal pagi hari sebelum beranjak dari tempat tidur untuk memantau pergeseran ovulasi.",
                                fontSize = 10.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Folikuler (Rendah)", fontSize = 8.5.sp, color = textSecondary)
                        Text("Kenaikan BBT (+0.28°C)", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Coral600)
                        Text("Luteal (Tinggi)", fontSize = 8.5.sp, color = textSecondary)
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
                                text = "Catatan Hari Ini (${selectedDay.dayOfMonth} ${selectedDay.date.month.name.lowercase().take(3).replaceFirstChar { c -> c.uppercase() }})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }

                        Text(
                            text = "Ubah Catatan >",
                            fontSize = 11.sp,
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
                                Text("Suhu Basal (BBT)", fontSize = 9.sp, color = textSecondary)
                                Text(if (log?.basalBodyTempCelsius != null) String.format(Locale.US, "%.2f °C", log.basalBodyTempCelsius) else "-- °C", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, color = textPrimary)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDarkMode) DarkBackground else Slate50,
                            border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Lendir Serviks", fontSize = 9.sp, color = textSecondary)
                                Text(if (log != null && log.cervicalMucus != CervicalMucusType.NONE) mucusLabelFor(log.cervicalMucus) else "--", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
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
                        isPainAlert -> "Nyeri Tinggi"
                        isPainFree -> "Bebas Nyeri"
                        else -> "Ringan"
                    }
                    val vasDisplayDesc = when {
                        isPainAlert -> "$vasScore / 10 • Nyeri Pelvis & Pinggang"
                        isPainFree -> "0 / 10 • Bebas Nyeri"
                        else -> "$vasScore / 10 • Nyeri Ringan"
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
                                    "Skala Nyeri (VAS)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = vasTitleColor
                                )
                                Text(
                                    text = vasDisplayDesc,
                                    fontSize = 11.sp,
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
                                    fontSize = 9.sp,
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
                    Text("PETA SIKLUS & OVULASI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Coral600)
                    Text("Kalender Siklus", fontSize = 20.sp, fontWeight = FontWeight.Black, color = textPrimary)
                }

                Surface(
                    onClick = {
                        currentYearMonth = java.time.YearMonth.from(today)
                        onSelectDate(today)
                        onToast("Kembali ke hari ini: ${today.dayOfMonth} ${today.month.name.lowercase()} ${today.year}")
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF1F2),
                    border = BorderStroke(1.dp, Color(0xFFFFE4E6))
                ) {
                    Text("Hari Ini", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Coral600, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
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
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Bulan Lalu", tint = textPrimary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${currentYearMonth.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${currentYearMonth.year}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textPrimary
                            )
                            Text(
                                text = if (fertilePrediction != null) {
                                    val nextP = fertilePrediction.predictedNextPeriodDate
                                    val ov = fertilePrediction.predictedOvulationDate
                                    "Haid: ${nextP.dayOfMonth} ${nextP.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }} • Ovulasi: ${ov.dayOfMonth} ${ov.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}"
                                } else {
                                    "Catat hari haid untuk memunculkan prediksi"
                                },
                                fontSize = 10.sp,
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
                            Icon(Icons.Default.ChevronRight, contentDescription = "Bulan Depan", tint = textPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val headers = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        headers.forEach { h ->
                            Text(h, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textSecondary, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
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
                                                    onToast("Dipilih: $dayNum ${cellDate.month.name.lowercase()} (Ketuk lagi untuk isi jurnal)")
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$dayNum",
                                                fontSize = 11.sp,
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
                        LegendPill(color = Color(0xFFFFE4E6), label = "Menstruasi", textSecondary)
                        LegendPill(color = Color(0xFFFECDD3), label = "Prediksi Haid", textSecondary)
                        LegendPill(color = Color(0xFFCFFAFE), label = "Masa Subur", textSecondary)
                        LegendPill(color = MedicalCyan, label = "Puncak Ovulasi", textSecondary)
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
                !hasActiveCycle -> "Belum Ada Data"
                isActualHaid -> "Menstruasi"
                isPredictedHaid -> "Prediksi Haid"
                isPeakOvulation -> "Puncak Ovulasi"
                isFertile -> "Masa Subur"
                else -> "Fase Folikuler"
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
                                    text = "${selectedCalendarDate.dayOfMonth} ${selectedCalendarDate.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${selectedCalendarDate.year}",
                                    fontSize = 13.sp,
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
                                        fontSize = 9.sp,
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
                                    !hasActiveCycle -> "Belum ada siklus aktif. Silakan isi jurnal pada hari haid pertama."
                                    isActualHaid -> "Pendarahan Menstruasi Aktif"
                                    isPredictedHaid -> "Estimasi Mulai Haid Berikutnya"
                                    isPeakOvulation -> "Peluang Konsepsi Tertinggi Siklus Ini"
                                    isFertile -> "Jendela Subur Siklus"
                                    selectedCalendarDate == today && cycleDayForSelected != null -> "Hari Ini • Hari ke-$cycleDayForSelected Siklus"
                                    selectedCalendarDate == today -> "Hari Ini • Belum Ada Siklus Aktif"
                                    cycleDayForSelected != null -> "Hari ke-$cycleDayForSelected Siklus"
                                    else -> "Tanggal di luar siklus aktif"
                                },
                                fontSize = 11.sp,
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
                            Text(if (log != null) "Ubah Jurnal" else "Isi Jurnal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val bbtVal = log?.basalBodyTempCelsius?.let { String.format(Locale.US, "%.2f °C", it) } ?: "-- °C"
                    val mucusVal = log?.cervicalMucus?.let { mucusLabelFor(it) } ?: "--"
                    val painVal = log?.let {
                        if (it.painVasScore > 0) "VAS ${it.painVasScore}" else "Bebas Nyeri"
                    } ?: "Bebas Nyeri"

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ParameterBox("Suhu Basal", bbtVal, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox("Lendir Serviks", mucusVal, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox("Skala Nyeri", painVal, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
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
        Text(label, fontSize = 10.sp, color = textSecondary)
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
            Text(label, fontSize = 9.sp, color = textSecondary)
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary)
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
                            text = "Prediksi Siklus & Kesuburan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Estimasi berdasarkan rata-rata siklus tercatat",
                            fontSize = 10.sp,
                            color = textSecondary,
                            maxLines = 1
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
                                    daysToNextPeriod > 0 -> "$daysToNextPeriod Hari Lagi"
                                    daysToNextPeriod == 0L -> "Hari Ini"
                                    else -> "Terlambat ${-daysToNextPeriod} Hari"
                                }
                            } else "Siap Dihitung",
                            fontSize = 11.sp,
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
                        Text("Estimasi Haid", fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, color = textSecondary, maxLines = 1)
                        Text(
                            text = if (nextDate != null) "${nextDate.dayOfMonth} ${nextDate.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}" else "--",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Coral600,
                            maxLines = 1
                        )
                        Text(
                            text = if (daysToNextPeriod != null) {
                                when {
                                    daysToNextPeriod > 0 -> "$daysToNextPeriod hari lagi"
                                    daysToNextPeriod == 0L -> "Hari ini"
                                    else -> "Terlambat ${-daysToNextPeriod} hari"
                                }
                            } else "--",
                            fontSize = 8.5.sp,
                            color = if (daysToNextPeriod != null && daysToNextPeriod < 0) Color(0xFFDC2626) else textSecondary,
                            maxLines = 1
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
                        Text("Puncak Ovulasi", fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, color = textSecondary, maxLines = 1)
                        Text(
                            text = if (ovulationDate != null) "${ovulationDate.dayOfMonth} ${ovulationDate.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}" else "--",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = MedicalTeal,
                            maxLines = 1
                        )
                        Text(
                            text = if (daysToOvulation != null) {
                                if (daysToOvulation > 0) "$daysToOvulation hari lagi" else if (daysToOvulation == 0L) "Hari ini!" else "Terlewati"
                            } else "--",
                            fontSize = 8.5.sp,
                            color = textSecondary,
                            maxLines = 1
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
                        Text("Masa Subur", fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, color = textSecondary, maxLines = 1)
                        Text(
                            text = if (fertileStart != null && fertileEnd != null) "${fertileStart.dayOfMonth}-${fertileEnd.dayOfMonth} ${fertileStart.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}" else "--",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalCyan,
                            maxLines = 1
                        )
                        Text(if (fertileStart != null) "6 Hari Subur" else "--", fontSize = 8.5.sp, color = textSecondary, maxLines = 1)
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
                    text = "Estimasi berdasarkan asumsi fase luteal 14 hari dengan rata-rata siklus ${cycleStats?.averageLength?.let { String.format(Locale.US, "%.0f", it) } ?: "28"} hari.",
                    fontSize = 9.sp,
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
                Text("Analisis Siklus", fontSize = 20.sp, fontWeight = FontWeight.Black, color = textPrimary)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDarkMode) DarkCardBackground else Slate100
                ) {
                    Text("Ringkasan Data", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate600, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
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
                            Text("REKAPITULASI SIKLUS KLINIS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = textPrimary)
                            Text("ID Anonim: $anonymousRecoveryKey", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = textSecondary)
                        }
                        Text(
                            text = "${today.dayOfMonth} ${today.month.name.lowercase().take(3).replaceFirstChar { c -> c.uppercase() }} ${today.year}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val rStr = cycleStats?.averageLength?.let { String.format(Locale.US, "%.1f Hari", it) } ?: if (latestCycle != null) "28.0 Hari*" else "-- Hari"
                        val vStr = cycleStats?.standardDeviation?.let { String.format(Locale.US, "±%.1f Hari", it) } ?: if (latestCycle != null) "Estimasi Awal" else "-- Hari"
                        val dStr = cycleStats?.averagePeriodDuration?.let { String.format(Locale.US, "%.1f Hari", it) } ?: if (latestCycle != null) "${latestCycle.periodDurationDays}.0 Hari" else "-- Hari"
                        ParameterBox("Rata-rata", rStr, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox("Variasi Siklus", vStr, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox("Lama Haid", dStr, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
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
                                Text("Pola Temperatur Biphasik (Ovulasi Terkonfirmasi)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textPrimary)
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
                                    Text("Coverline: 36.40°C", fontSize = 8.sp, color = textSecondary)
                                    Text("Pergeseran Biphasik", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MedicalTeal)
                                    Text("Fase Luteal", fontSize = 8.sp, color = textSecondary)
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
                                    text = "Pola Temperatur Basal (BBT)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Belum ada rekaman suhu BBT. Catat suhu basal harian di menu catatan harian untuk menyertakan grafik suhu pada rekap.",
                                    fontSize = 9.sp,
                                    color = textSecondary,
                                    lineHeight = 13.sp
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
                                    Text("Perhatian: " + alert.type.description, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedicalRose)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = alert.details,
                                    fontSize = 10.sp,
                                    color = Color(0xFF9F1239),
                                    lineHeight = 14.sp
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
                                Text("Siklus berjalan normal: tidak ada catatan yang perlu diperhatikan", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF065F46))
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
                                Text("Pemantauan aktif: catatan akan dianalisis setiap kali kamu mencatat haid baru", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = textSecondary)
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
                            text = "VISUALISASI TIMELINE SIKLUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textSecondary
                        )
                        Text(
                            text = "${completedCycles.size + if (latestCycle != null) 1 else 0} siklus",
                            fontSize = 10.sp,
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
                                    text = filter.label,
                                    fontSize = 11.sp,
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
                            Text("Haid", fontSize = 10.sp, color = textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF67E8F9)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Masa Subur", fontSize = 10.sp, color = textSecondary)
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
                            Text("Ovulasi", fontSize = 10.sp, color = textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.width(2.dp).height(8.dp).background(if (isDarkMode) Color.White else Color(0xFF1E293B)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hari Ini", fontSize = 10.sp, color = textSecondary)
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
                                        text = "Belum ada riwayat siklus yang tercatat.\nCatat hari pertama haid untuk memulai.",
                                        fontSize = 11.sp,
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
                    Text("LOG HARIAN TERAKHIR", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = textSecondary)
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
                                Text("Tanggal", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondary, modifier = Modifier.weight(1.2f))
                                Text("Darah", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondary, modifier = Modifier.weight(1f))
                                Text("Suhu BBT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondary, modifier = Modifier.weight(1f))
                                Text("Nyeri", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondary, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))

                            if (allLogs.isNotEmpty()) {
                                allLogs.sortedByDescending { it.date }.take(5).forEach { log ->
                                    val dateStr = "${log.date.dayOfMonth} ${log.date.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}"
                                    val flowStr = when (log.flow) {
                                        FlowIntensity.SPOTTING -> "Bercak"
                                        FlowIntensity.LIGHT -> "Ringan"
                                        FlowIntensity.MEDIUM -> "Sedang"
                                        FlowIntensity.HEAVY -> "Deras"
                                        else -> "Tidak"
                                    }
                                    val bbtStr = log.basalBodyTempCelsius?.let { String.format(Locale.US, "%.2f°C", it) } ?: "--"
                                    val painStr = if (log.painVasScore > 0) "VAS ${log.painVasScore}" else "Bebas"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(dateStr, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = textPrimary, modifier = Modifier.weight(1.2f))
                                        Text(
                                            flowStr,
                                            fontSize = 10.sp,
                                            fontWeight = if (log.flow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY)) FontWeight.Bold else FontWeight.Normal,
                                            color = if (log.flow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY)) Coral600 else textPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(bbtStr, fontSize = 10.sp, color = textPrimary, modifier = Modifier.weight(1f))
                                        Text(painStr, fontSize = 10.sp, color = if (log.painVasScore >= 4) MedicalRose else textPrimary, modifier = Modifier.weight(1f))
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
                                        text = "Belum ada catatan harian yang diinput.",
                                        fontSize = 10.sp,
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
                    onClick = { onSharePdf?.invoke() ?: onToast("Menyiapkan Berkas Rekap Siklus...") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral500)
                ) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unduh Rekap Siklus (PDF)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // 2. Secondary Button: CSV Mentah (Excel / Sheets)
                OutlinedButton(
                    onClick = { onExportCsv?.invoke() ?: onToast("Mengekspor Berkas CSV Mentah") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Icon(Icons.Rounded.TableView, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unduh CSV Mentah (Excel / Sheets)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
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
                                append("Ingin ekspor langsung tanpa iklan? ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = PrimaryCoral)) {
                                    append("Buka Lisensi Pro Seumur Hidup")
                                }
                            },
                            fontSize = 12.sp,
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
    val avgBbtStr = if (bbtValues.isNotEmpty()) String.format(Locale.US, "%.2f °C", bbtValues.average()) else "Tidak tercatat"

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
                        text = if (isOngoing) "Siklus Berjalan" else "Detail Riwayat Siklus",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = textPrimary
                    )
                    Text(
                        text = "${cycle.startDate.format(fmt)} – ${if (isOngoing) "Hari Ini (Aktif)" else cycle.endDate!!.format(fmt)}",
                        fontSize = 11.sp,
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
                        label = "Total Panjang Siklus",
                        value = "$cycleLength Hari",
                        valueColor = Coral600
                    )
                    HorizontalDivider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))

                    // Row 2: Masa Haid
                    DetailMetricRow(
                        icon = Icons.Rounded.WaterDrop,
                        iconTint = Coral500,
                        label = "Masa Perdarahan Haid",
                        value = "${cycle.periodDurationDays} Hari (${cycle.startDate.dayOfMonth} – ${periodEnd.dayOfMonth} ${cycle.startDate.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }})",
                        valueColor = textPrimary
                    )
                    HorizontalDivider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))

                    // Row 3: Masa Subur
                    DetailMetricRow(
                        icon = Icons.Rounded.Favorite,
                        iconTint = Color(0xFF0891B2),
                        label = "Jendela Masa Subur",
                        value = "${fertileStart.dayOfMonth} – ${ovulationDate.dayOfMonth} ${ovulationDate.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}",
                        valueColor = Color(0xFF0891B2)
                    )
                    HorizontalDivider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))

                    // Row 4: Ovulasi
                    DetailMetricRow(
                        icon = Icons.Rounded.WbSunny,
                        iconTint = Color(0xFF0891B2),
                        label = "Estimasi Puncak Ovulasi",
                        value = "Hari ke-$ovulationDay (${ovulationDate.dayOfMonth} ${ovulationDate.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }})",
                        valueColor = Color(0xFF0891B2)
                    )
                    HorizontalDivider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))

                    // Row 5: BBT & Nyeri
                    DetailMetricRow(
                        icon = Icons.Rounded.Thermostat,
                        iconTint = Coral600,
                        label = "Rata-rata Suhu (BBT)",
                        value = avgBbtStr,
                        valueColor = textPrimary
                    )
                    HorizontalDivider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))

                    DetailMetricRow(
                        icon = Icons.Rounded.Bolt,
                        iconTint = if (peakPain >= 7) Coral600 else Slate500,
                        label = "Skala Nyeri Puncak",
                        value = if (peakPain > 0) "VAS $peakPain / 10" else "Bebas Nyeri",
                        valueColor = if (peakPain >= 7) Coral600 else textPrimary
                    )
                }
            }

            // Symptoms tags if any
            if (symptomsList.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Gejala Tercatat dalam Siklus Ini:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textSecondary)
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
                                    fontSize = 10.sp,
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
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Coral500)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buka ${cycle.startDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} di Kalender",
                    fontSize = 12.sp,
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
            Text(label, fontSize = 11.sp, color = Slate500)
        }
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

// 5. SETTINGS SCREEN: Wired to Cloudflare Backup, Recovery Key, and Data Nuke
@Composable
fun SettingsScreenView(
    isDarkMode: Boolean,
    isDiscreet: Boolean,
    isPro: Boolean,
    isPromilMode: Boolean = false,
    onTogglePromilMode: (Boolean) -> Unit = {},
    pinStatus: String,
    anonymousRecoveryKey: String,
    onToggleDiscreet: () -> Unit,
    onToggleDark: () -> Unit,
    onOpenPin: () -> Unit,
    onBuyPro: () -> Unit,
    onCopyRecoveryKey: (() -> Unit)? = null,
    onBackupLocal: ((Boolean, String?) -> Unit)? = null,
    onRestoreLocal: (() -> Unit)? = null,
    onNukeData: () -> Unit,
    onToast: (String) -> Unit
) {
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorderColor else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500
    var isBiometricEnabled by remember { mutableStateOf(false) }
    var isBackupOptionsDialogOpen by remember { mutableStateOf(false) }
    var isBackupEncrypted by remember { mutableStateOf(false) }
    var backupPinInput by remember { mutableStateOf("") }
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
                    Text("PREFERENSI & KONTROL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Coral600)
                    Text("Pengaturan", fontSize = 20.sp, fontWeight = FontWeight.Black, color = textPrimary)
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
                        if (isPro) "LISENSI PRO SEUMUR HIDUP AKTIF" else "VERSI GRATIS (DIDUKUNG IKLAN)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        if (isPro) "100% Bebas Iklan Selamanya" else "Upgrade ke Lifetime Pro",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = if (isPro) "Semua fitur ekspor dan sinkronisasi aktif tanpa batas." else "Beli putus sekali seumur hidup: 100% bebas iklan, ekspor PDF tanpa batas & sinkronisasi cloud terenkripsi.",
                        fontSize = 11.sp,
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
                            Text("Buka Lisensi Pro", color = Color(0xFFB45309), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    Text("TUJUAN PELACAKAN SIKLUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Coral600)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text("Mode Program Hamil (Promil)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(
                                text = if (isPromilMode)
                                    "Aktif: Beranda menampilkan masa subur, ovulasi & peluang konsepsi."
                                else
                                    "Nonaktif: Beranda bersih & fokus jadwal haid berikutnya (ramah remaja/lajang).",
                                fontSize = 10.5.sp,
                                color = textSecondary,
                                lineHeight = 14.sp
                            )
                        }
                        Switch(
                            checked = isPromilMode,
                            onCheckedChange = {
                                onTogglePromilMode(it)
                                onToast(if (it) "Mode Promil Diaktifkan" else "Mode Biasa (Pantau Siklus) Aktif")
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
                    Text("KEAMANAN & KUNCI APLIKASI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Coral600)

                    // Row 1: PIN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Kunci PIN 4-Digit", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(pinStatus, fontSize = 11.sp, color = textSecondary)
                        }
                        Button(
                            onClick = onOpenPin,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Coral500),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Atur PIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                            Text("Kunci Sidik Jari / Wajah", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("Buka cepat saat ponsel dipinjam", fontSize = 11.sp, color = textSecondary)
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = {
                                isBiometricEnabled = it
                                onToast(if (it) "Kunci Sidik Jari / Biometrik Aktif" else "Kunci Sidik Jari Dinonaktifkan")
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
                            Text("Kunci Otomatis", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("Saat aplikasi di latar belakang", fontSize = 11.sp, color = textSecondary)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDarkMode) DarkBackground else Slate100
                        ) {
                            Text("30 Detik", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
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
                    Text("CADANGAN DATA MANDIRI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Coral600)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kunci Pemulihan Cadangan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text(
                            text = "Salin",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Coral600,
                            modifier = Modifier.clickable { onCopyRecoveryKey?.invoke() ?: onToast("Recovery Key Disalin: $anonymousRecoveryKey") }
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
                            fontSize = 11.sp,
                            color = textSecondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Text("Gunakan kode rahasia ini jika Anda berganti perangkat baru.", fontSize = 10.sp, color = textSecondary)

                    HorizontalDivider(color = borderCol)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Cadangan Berkas (.cjbackup)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("Simpan di HP, Google Drive, atau kirim ke chat pribadi", fontSize = 10.sp, color = textSecondary)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Text(
                            text = "100% Bebas Server • Data Milik Anda Sepenuhnya",
                            fontSize = 10.sp,
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
                            Text("Cadangkan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                onRestoreLocal?.invoke() ?: onToast("Membuka pengelola berkas...")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MedicalTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pulihkan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    Text("TAMPILAN & NOTIFIKASI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Coral600)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Mode Samaran (Anti-Intip)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("Samarkan istilah sensitif di publik", fontSize = 10.sp, color = textSecondary)
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
                            Text("Mode Gelap Subuh (OLED)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("Ramah mata saat bangun ukur suhu", fontSize = 10.sp, color = textSecondary)
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
                            Text("Pengingat Suhu Basal (BBT)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("Alarm lembut pukul 05:30 pagi", fontSize = 10.sp, color = textSecondary)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF1F2),
                            border = BorderStroke(1.dp, Color(0xFFFFE4E6))
                        ) {
                            Text("05:30", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Coral700, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
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
                                Text("Panduan Singkat & FAQ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Text("Cara input haid, prediksi siklus & arti warna", fontSize = 10.sp, color = textSecondary)
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
                                Text("1. Cara Mencatat Hari Pertama Haid", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Coral600)
                                Text(
                                    text = "Buka form catatan harian pada tanggal haid, lalu pilih intensitas darah: Ringan, Sedang, atau Deras. Opsi \"Tidak\" atau \"Bercak\" dianggap sebagai gejala harian biasa tanpa pendarahan haid.",
                                    fontSize = 10.sp,
                                    color = textSecondary,
                                    lineHeight = 14.sp
                                )
                            }

                            // Point 2: Mekanisme Prediksi Dinamis
                            Column {
                                Text("2. Mekanisme Prediksi Siklus (Dinamis)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Coral600)
                                Text(
                                    text = "• Siklus ke-1: Menggunakan baseline 28 hari sebagai estimasi awal.\n• Siklus ke-2 ke atas: Sepenuhnya dinamis menghitung rata-rata riil tubuh Anda sendiri. Bukan angka saklek 28 hari.",
                                    fontSize = 10.sp,
                                    color = textSecondary,
                                    lineHeight = 14.sp
                                )
                            }

                            // Point 3: Arti Warna Kalender
                            Column {
                                Text("3. Arti Warna pada Kalender", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Coral600)
                                Text(
                                    text = "• Merah Muda: Hari haid aktif yang Anda catat.\n• Dot Merah Muda: Prediksi hari haid berikutnya (bisa dicek di bulan depan via tombol >).\n• Biru Muda: Jendela subur (6 hari peluang konsepsi).\n• Toska Tua: Puncak ovulasi (pelepasan sel telur).",
                                    fontSize = 10.sp,
                                    color = textSecondary,
                                    lineHeight = 14.sp
                                )
                            }

                            // Point 4: Standar Medis FIGO
                            Column {
                                Text("4. Siklus Normal & Tanda yang Perlu Diperhatikan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Coral600)
                                Text(
                                    text = "Siklus normal wanita berjarak 24 hingga 38 hari. Jika siklus Anda <24 hari, >38 hari, atau sangat tidak teratur (selisih ≥8 hari), aplikasi akan memberi peringatan di tab Analisis Siklus.",
                                    fontSize = 10.sp,
                                    color = textSecondary,
                                    lineHeight = 14.sp
                                )
                            }

                            // Tautan Dokumentasi Resmi Lengkap
                            Surface(
                                onClick = {
                                    try {
                                        uriHandler.openUri("https://asridigital.com/cyclejournal/docs")
                                    } catch (e: Exception) {
                                        onToast("Membuka peramban...")
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
                                            Text("Dokumentasi & Referensi Siklus", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                            Text("asridigital.com/cyclejournal/docs", fontSize = 9.sp, color = textSecondary)
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
                    Text("HAPUS DATA & RESET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MedicalRose)
                    Text("Menghapus seluruh catatan siklus lokal di ponsel dan cadangan cloud secara permanen sesuai hak privasi Anda.", fontSize = 10.sp, color = Color(0xFF9F1239))
                    Button(
                        onClick = onNukeData,
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalRose),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hapus Seluruh Data Permanen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // App Version Footer
        item {
            Text(
                text = "CycleJournal v${BuildConfig.VERSION_NAME} • Pemantau Siklus Pribadi",
                fontSize = 11.sp,
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
                    Text("Cadangkan Data Mandiri", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Pilih metode penguncian berkas cadangan Anda:",
                            fontSize = 12.sp,
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
                                    Text("Standar (Bebas PIN)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!isBackupEncrypted) Coral600 else textPrimary)
                                    Text("Langsung pulih tanpa password. Cocok jika Anda sering lupa PIN dan menyimpan file di Google Drive pribadi.", fontSize = 10.sp, color = textSecondary, lineHeight = 13.sp)
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
                                        Text("Terenkripsi dengan PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isBackupEncrypted) Coral600 else textPrimary)
                                        Text("Diberi kunci enkripsi AES-256. Wajib memasukkan PIN yang sama saat memulihkan berkas.", fontSize = 10.sp, color = textSecondary, lineHeight = 13.sp)
                                    }
                                }
                                if (isBackupEncrypted) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = backupPinInput,
                                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) backupPinInput = it },
                                        placeholder = { Text("Ketik 4 Digit PIN") },
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
                            ) ?: onToast("Membuat berkas cadangan...")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Coral500),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Buat Cadangan", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isBackupOptionsDialogOpen = false }) {
                        Text("Batal")
                    }
                }
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
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500
    var vasScore by remember(initialLog) { mutableStateOf((initialLog?.painVasScore ?: 0).toFloat()) }
    var selectedFlow by remember(initialLog) {
        mutableStateOf(
            when (initialLog?.flow) {
                FlowIntensity.SPOTTING -> "Bercak"
                FlowIntensity.LIGHT -> "Ringan"
                FlowIntensity.MEDIUM -> "Sedang"
                FlowIntensity.HEAVY -> "Deras"
                else -> "Tidak"
            }
        )
    }
    var selectedMucus by remember(initialLog) {
        mutableStateOf(
            when (initialLog?.cervicalMucus) {
                CervicalMucusType.DRY -> "Kering"
                CervicalMucusType.STICKY -> "Lengket"
                CervicalMucusType.CREAMY -> "Krim"
                CervicalMucusType.WATERY -> "Cair"
                CervicalMucusType.EGG_WHITE -> "Putih Telur"
                else -> "Tidak"
            }
        )
    }
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

    val defaultSymptoms = remember {
        listOf(
            "Kram Pelvis", "Sakit Pinggang", "Payudara Sensitif", "Sakit Kepala",
            "Perut Kembung", "Mood Sensitif", "Kelelahan", "Mual"
        )
    }

    val customSymptomsList = remember {
        mutableStateListOf<String>().apply {
            val saved = customPrefs.getStringSet("custom_symptoms", emptySet()) ?: emptySet()
            addAll(saved)
        }
    }

    var isAddCustomDialogOpen by remember { mutableStateOf(false) }
    var newCustomSymptomInput by remember { mutableStateOf("") }
    var clinicalNotesInput by remember(initialLog) {
        val raw = initialLog?.notes ?: ""
        val cleaned = if (raw.contains("Catatan: ")) {
            raw.substringAfter("Catatan: ").trim()
        } else if (!raw.startsWith("Gejala: ")) {
            raw
        } else {
            ""
        }
        mutableStateOf(cleaned)
    }

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (isDarkMode) DarkCardBackground else Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Jurnal Kondisi Hari Ini", fontSize = 17.sp, fontWeight = FontWeight.Black)
                    Text(
                        text = "${targetDate.dayOfWeek.name.lowercase().replaceFirstChar { c -> c.uppercase() }}, ${targetDate.dayOfMonth} ${targetDate.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${targetDate.year}",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Slate400)
                }
            }

            // Flow Pills
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Pendarahan Menstruasi (Flow)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (selectedFlow in listOf("Ringan", "Sedang", "Deras")) "• Fase Haid Aktif" else "• Bukan Hari Haid",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedFlow in listOf("Ringan", "Sedang", "Deras")) Coral600 else Slate400
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Pilih Ringan, Sedang, atau Deras untuk mencatat hari haid dan mengaktifkan prediksi siklus.",
                    fontSize = 9.5.sp,
                    color = Slate400,
                    lineHeight = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Tidak", "Bercak", "Ringan", "Sedang", "Deras").forEach { flow ->
                        val isSelected = flow == selectedFlow
                        Surface(
                            modifier = Modifier.weight(1f).clickable { selectedFlow = flow },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Coral500 else if (isDarkMode) DarkBackground else Slate100
                        ) {
                            Text(
                                flow,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Slate600,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Clinical Pain VAS Card (Refined functional rating)
            val num = vasScore.toInt()
            val (badgeText, cardBg, textCol, impactText) = when {
                num == 0 -> Quadruple("Bebas Nyeri", Color(0xFFECFDF5), Color(0xFF047857), "Bebas Nyeri • Nyaman beraktivitas")
                num <= 3 -> Quadruple("Ringan", Slate100, Slate700, "Nyeri Ringan • Terasa pegal, aktivitas normal")
                num <= 6 -> Quadruple("Perlu Pantauan", Color(0xFFFFFBEB), Color(0xFFB45309), "Nyeri Sedang • Mengganggu, butuh jeda istirahat")
                num <= 8 -> Quadruple("Perhatian", Color(0xFFFFF1F2), Color(0xFFBE123C), "Nyeri Berat • Membatasi gerak, butuh pereda nyeri")
                else -> Quadruple("Konsultasi Segera", Color(0xFFFEE2E2), Color(0xFF991B1B), "Sangat Hebat • Tirah baring total / darurat")
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
                        Text("Tingkat Nyeri (Skala 0–10)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textCol)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = textCol.copy(alpha = 0.15f)
                        ) {
                            Text(badgeText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textCol, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text("$num / 10", fontSize = 16.sp, fontWeight = FontWeight.Black, color = textCol)
                        Text(impactText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = textCol)
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

            // Dynamic Symptom Chips with Custom Symptom Support
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Gejala Tubuh Hari Ini", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${selectedSymptoms.size} dipilih",
                        fontSize = 10.sp,
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
                                    fontSize = 10.sp,
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
                                text = "Tambah Gejala",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Coral500
                            )
                        }
                    }
                }
            }

            // BBT Number Stepper & Analgesic Checkbox
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            Text("Suhu Basal (°C)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                            Text(
                                text = if (isBbtRecorded) "Hapus" else "+ Catat",
                                fontSize = 9.sp,
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
                                        fontSize = 13.sp,
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
                                text = "-- °C (Belum diukur)",
                                fontSize = 11.sp,
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
                            Text("Analgesik", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                            Text("Minum Obat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            // Cervical Mucus Selector (2 rows x 3 columns for balanced width)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Lendir Serviks (Sintotermal)", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                // Row 1: Tidak, Kering, Lengket
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Tidak", "Kering", "Lengket").forEach { mucus ->
                        val isSelected = mucus == selectedMucus
                        Surface(
                            modifier = Modifier.weight(1f).clickable { selectedMucus = mucus },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFFFFF1F2) else if (isDarkMode) DarkBackground else Slate100,
                            border = BorderStroke(1.dp, if (isSelected) Coral400 else Color.Transparent)
                        ) {
                            Text(
                                text = mucus,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Coral600 else Slate600,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // Row 2: Krim, Cair, Putih Telur
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Krim", "Cair", "Putih Telur").forEach { mucus ->
                        val isSelected = mucus == selectedMucus
                        Surface(
                            modifier = Modifier.weight(1f).clickable { selectedMucus = mucus },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFFFFF1F2) else if (isDarkMode) DarkBackground else Slate100,
                            border = BorderStroke(1.dp, if (isSelected) Coral400 else Color.Transparent)
                        ) {
                            Text(
                                text = mucus,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Coral600 else Slate600,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }


            // Clinical Notes Field
            Column {
                Text("Catatan Tambahan (Opsional)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = clinicalNotesInput,
                    onValueChange = { clinicalNotesInput = it },
                    placeholder = { Text("Misal: Dosis obat, keluhan spesifik, saran dokter...", fontSize = 11.sp, color = Slate400) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 3
                )
            }

            Button(
                onClick = {
                    val flowEnum = when (selectedFlow) {
                        "Bercak" -> FlowIntensity.SPOTTING
                        "Ringan" -> FlowIntensity.LIGHT
                        "Sedang" -> FlowIntensity.MEDIUM
                        "Deras" -> FlowIntensity.HEAVY
                        else -> FlowIntensity.NONE
                    }
                    val mucusEnum = when (selectedMucus) {
                        "Kering" -> CervicalMucusType.DRY
                        "Lengket" -> CervicalMucusType.STICKY
                        "Krim" -> CervicalMucusType.CREAMY
                        "Cair" -> CervicalMucusType.WATERY
                        "Putih Telur" -> CervicalMucusType.EGG_WHITE
                        else -> CervicalMucusType.NONE
                    }
                    val bbtVal = if (isBbtRecorded) bbtInputText.replace(",", ".").toDoubleOrNull() else null
                    val logEntity = DailyLogEntity(
                        date = targetDate,
                        flow = flowEnum,
                        basalBodyTempCelsius = bbtVal,
                        cervicalMucus = mucusEnum,
                        painVasScore = vasScore.toInt(),
                        painLocation = if (selectedSymptoms.isNotEmpty()) selectedSymptoms.joinToString(", ") else null,
                        takenAnalgesic = hasTakenAnalgesic,
                        notes = buildString {
                            if (selectedSymptoms.isNotEmpty()) append("Gejala: ${selectedSymptoms.joinToString(", ")}")
                            if (clinicalNotesInput.isNotBlank()) {
                                if (isNotEmpty()) append("\nCatatan: ")
                                append(clinicalNotesInput.trim())
                            }
                        }.takeIf { it.isNotBlank() }
                    )
                    onSave(logEntity)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Coral500)
            ) {
                Text("Simpan Catatan Hari Ini", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        if (isAddCustomDialogOpen) {
            AlertDialog(
                onDismissRequest = { isAddCustomDialogOpen = false },
                title = { Text("Tambah Gejala Kustom", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Ketikkan nama gejala tubuh yang Anda rasakan:", fontSize = 12.sp, color = Slate500)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newCustomSymptomInput,
                            onValueChange = { newCustomSymptomInput = it },
                            placeholder = { Text("Misal: Migrain, Nyeri Sendi, Insomnia...", fontSize = 11.sp) },
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
                        Text("Simpan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isAddCustomDialogOpen = false }) {
                        Text("Batal", fontSize = 12.sp)
                    }
                }
            )
        }
        if (isBbtDialogOpen) {
            AlertDialog(
                onDismissRequest = { isBbtDialogOpen = false },
                title = { Text("Masukkan Suhu Basal", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Ketik suhu tubuh pagi hari (misal: 36.65):", fontSize = 12.sp, color = textSecondary)
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
                        Text("Simpan", fontWeight = FontWeight.Bold, color = Coral600)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isBbtDialogOpen = false }) {
                        Text("Batal")
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
                NavButton(Icons.Default.Home, "Beranda", currentScreen == AppScreen.DASHBOARD) { onSelect(AppScreen.DASHBOARD) }
                NavButton(Icons.Default.DateRange, "Kalender", currentScreen == AppScreen.CALENDAR) { onSelect(AppScreen.CALENDAR) }

                // Spacer to reserve central notch space for the elevated FAB
                Spacer(modifier = Modifier.size(54.dp))

                NavButton(Icons.Default.Description, "Laporan", currentScreen == AppScreen.REPORT) { onSelect(AppScreen.REPORT) }
                NavButton(Icons.Default.Settings, "Pengaturan", currentScreen == AppScreen.SETTINGS) { onSelect(AppScreen.SETTINGS) }
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
                contentDescription = "Catat Harian",
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
        Text(label, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Coral600 else Slate400)
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
                Text("Setup Kunci PIN 4-Digit", fontSize = 15.sp, fontWeight = FontWeight.Black, color = textPrimary)
                Text("Lindungi privasi saat ponsel Anda dipinjam", fontSize = 11.sp, color = Slate400)

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
                                        .height(44.dp)
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
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral500)
                ) {
                    Text("Simpan Kunci Keamanan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
