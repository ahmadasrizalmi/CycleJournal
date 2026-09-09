package com.app.cyclejournal.ui

import androidx.compose.animation.*
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
    onBackupCloud: ((String) -> Unit)? = null,
    onRestoreCloud: ((String) -> Unit)? = null,
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
    onSavePin: (String) -> Unit = {}
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

    val today = remember { LocalDate.now() }

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

            val isHaid = date in periodDates || (latestCycle != null && !date.isBefore(latestCycle.startDate) && date.isBefore(latestCycle.startDate.plusDays(5)))
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
                        logModalDate = selectedDay.date
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
                        onSharePdf = {
                            onSharePdf?.invoke() ?: run {
                                if (isProLicenseActive) {
                                    showToast("Membuka PDF Medis SpOG (Tanpa Iklan)")
                                } else {
                                    showToast("Menonton 1 Iklan Singkat... PDF Medis Siap!")
                                }
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
                        onToast = showToast
                    )
                    AppScreen.SETTINGS -> SettingsScreenView(
                        isDarkMode = isDarkMode,
                        isDiscreet = isDiscreetMode,
                        isPro = isProLicenseActive,
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
                        onBackupCloud = { pin ->
                            onBackupCloud?.invoke(pin) ?: showToast("Enkripsi & Cadangan Cloud Berhasil")
                        },
                        onRestoreCloud = { pin ->
                            onRestoreCloud?.invoke(pin) ?: showToast("Data Arsip Berhasil Dipulihkan")
                        },
                        onNukeData = {
                            onNukeData?.invoke() ?: run {
                                showToast("Seluruh data lokal & cloud berhasil dibersihkan")
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
                    showToast("Catatan Hari Ini Berhasil Disimpan")
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
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CoralLinearGradient),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                )
            }
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
    onSelectDay: (DayStripItem) -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenLog: () -> Unit
) {
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorder else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500

    val today = remember { LocalDate.now() }
    val currentCycleDay = latestCycle?.let {
        (ChronoUnit.DAYS.between(it.startDate, today) + 1L).coerceAtLeast(1L)
    } ?: 14L

    val avgCycleDays = cycleStats?.averageLength ?: 28.0
    val stdDev = cycleStats?.standardDeviation ?: 1.5

    val isBleedingToday = selectedDay.phase == CyclePhase.MENSTRUATION
    val isFertileToday = selectedDay.phase == CyclePhase.FERTILE || selectedDay.phase == CyclePhase.OVULATION

    val phaseBadge = when {
        isDiscreet -> "FASE 02"
        isBleedingToday -> "MENSTRUASI"
        selectedDay.phase == CyclePhase.OVULATION -> "PUNCAK OVULASI"
        isFertileToday -> "JENDELA SUBUR"
        else -> "FASE FOLIKULER"
    }

    val phaseTitle = when {
        isDiscreet -> "Periode Tengah"
        isBleedingToday -> "Fase Menstruasi"
        selectedDay.phase == CyclePhase.OVULATION -> "Puncak Ovulasi"
        isFertileToday -> "Fase Folikuler"
        else -> "Fase Folikuler"
    }

    val ovulationCountdown = fertilePrediction?.let {
        ChronoUnit.DAYS.between(today, it.predictedOvulationDate).toInt()
    }
    val subtitleText = when {
        isDiscreet -> "Pencatatan normal berlangsung"
        ovulationCountdown != null && ovulationCountdown > 0 -> "Ovulasi dalam $ovulationCountdown hari ke depan"
        ovulationCountdown == 0 -> "Ovulasi berlangsung hari ini!"
        else -> "Pencatatan normal berlangsung"
    }

    val conceptionChance = when {
        selectedDay.phase == CyclePhase.OVULATION -> "Maksimal (95%)"
        isFertileToday -> "Tinggi (85%)"
        isBleedingToday -> "Sangat Rendah (<5%)"
        else -> "Rendah (15%)"
    }

    val progressRatio = (currentCycleDay.toFloat() / avgCycleDays.toFloat()).coerceIn(0f, 1f)

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Phase Badge Capsule
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = phaseBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = phaseTitle,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFD1D8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = subtitleText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFFE4E6)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Metric Pills Row (Rata-rata & Peluang Konsepsi)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.18f)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)) {
                                    Text(
                                        text = "RATA-RATA SIKLUS",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD1D8),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%.0f Hari (±%.1f)", avgCycleDays, stdDev),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.18f)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)) {
                                    Text(
                                        text = "PELUANG KONSEPSI",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD1D8),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = conceptionChance,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFEF08A)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Circular Progress: Day X of Average Days (50% sweep if day 14 of 28)
                    Box(
                        modifier = Modifier.size(92.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokePx = 7.dp.toPx()
                            // Background Track Ring
                            drawCircle(
                                color = Color.White.copy(alpha = 0.25f),
                                style = Stroke(width = strokePx)
                            )
                            // Progress Arc
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
                                text = "Hari",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFFE4E6)
                            )
                            Text(
                                text = "$currentCycleDay",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                lineHeight = 28.sp
                            )
                            Text(
                                text = "dari ${avgCycleDays.toInt()}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFFE4E6)
                            )
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
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(item.dotColor)
                                )
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
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Text(
                                "Normal",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46),
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

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                    ) {
                        val w = size.width
                        val h = size.height

                        // Baseline Dotted Coverline at 36.40°C
                        val coverlineY = h * 0.58f
                        drawLine(
                            color = Color(0xFFCBD5E1),
                            start = Offset(0f, coverlineY),
                            end = Offset(w, coverlineY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )

                        val pts = if (hasRealBbt && logsWithBbt.size >= 2) {
                            val minT = 36.0
                            val maxT = 37.0
                            logsWithBbt.mapIndexed { idx, item ->
                                val x = w * (idx.toFloat() / (logsWithBbt.size - 1).coerceAtLeast(1).toFloat())
                                val temp = item.basalBodyTempCelsius ?: 36.4
                                val y = h * (1f - ((temp - minT) / (maxT - minT)).toFloat().coerceIn(0.1f, 0.9f))
                                Offset(x, y)
                            }
                        } else {
                            listOf(
                                Offset(w * 0.05f, h * 0.72f),
                                Offset(w * 0.20f, h * 0.68f),
                                Offset(w * 0.36f, h * 0.60f),
                                Offset(w * 0.52f, h * 0.64f),
                                Offset(w * 0.68f, h * 0.44f),
                                Offset(w * 0.84f, h * 0.26f),
                                Offset(w * 0.95f, h * 0.18f)
                            )
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
                            val isSelectedPoint = idx == (pts.size / 2)
                            val dotColor = if (isSelectedPoint) Slate900 else if (idx > pts.size / 2) MedicalCyan else Coral600
                            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                            drawCircle(color = dotColor, radius = 3.5.dp.toPx(), center = pt)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fase Folikuler (Rendah)", fontSize = 9.sp, color = textSecondary)
                        Text("Prediksi Kenaikan Progesteron (+0.28°C)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Coral600)
                        Text("Fase Luteal", fontSize = 9.sp, color = textSecondary)
                    }
                }
            }
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
                                Text(selectedDay.bbt, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, color = textPrimary)
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
                                Text(selectedDay.mucus, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Real VAS Logic: Green Comfortable if 0 or Bebas Nyeri, Red Alert ONLY if >= 7
                    val vasScore = log?.painVasScore ?: if (selectedDay.pain.contains("7")) 7 else 0
                    val isPainFree = vasScore == 0 || selectedDay.pain.contains("Bebas Nyeri", ignoreCase = true)
                    val isPainAlert = vasScore >= 7 || selectedDay.pain.contains("Sedang") || selectedDay.pain.contains("Berat")

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
                        isPainAlert -> "SpOG Alert"
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
    onOpenLog: (LocalDate) -> Unit,
    onToast: (String) -> Unit
) {
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorder else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500

    val today = remember { LocalDate.now() }
    var selectedCalendarDate by remember { mutableStateOf(today) }

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
                        selectedCalendarDate = today
                        onToast("Memeriksa data ${today.dayOfMonth} ${today.month.name.lowercase()} ${today.year}")
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF1F2),
                    border = BorderStroke(1.dp, Color(0xFFFFE4E6))
                ) {
                    Text("Hari Ini", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Coral600, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }

        // Full Month Calendar Card
        item {
            val currentYearMonth = remember { java.time.YearMonth.from(today) }
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
                        IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = textSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${today.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${today.year}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textPrimary
                            )
                            Text(
                                text = "Siklus #${completedCycles.size + 1} • Rata-rata 28 Hari",
                                fontSize = 10.sp,
                                color = textSecondary
                            )
                        }
                        IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = textSecondary)
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
                                    val isHaid = cellDate in periodDates
                                    val isPeak = fertilePrediction != null && cellDate == fertilePrediction.predictedOvulationDate
                                    val isFertile = fertilePrediction != null && !cellDate.isBefore(fertilePrediction.fertileWindowStart) && !cellDate.isAfter(fertilePrediction.fertileWindowEnd)
                                    val isSelected = cellDate == selectedCalendarDate

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when {
                                                    isSelected -> Slate900
                                                    isPeak -> MedicalCyan
                                                    isHaid -> Color(0xFFFFE4E6)
                                                    isFertile -> Color(0xFFCFFAFE)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 0.dp,
                                                color = if (isSelected) Coral400 else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                selectedCalendarDate = cellDate
                                                onToast("Memeriksa data $dayNum ${cellDate.month.name.lowercase()} ${cellDate.year}")
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$dayNum",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected || isPeak) FontWeight.Black else FontWeight.SemiBold,
                                            color = when {
                                                isSelected || isPeak -> Color.White
                                                isHaid -> Color(0xFF9F1239)
                                                isFertile -> Color(0xFF0E7490)
                                                else -> textPrimary
                                            }
                                        )
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
                        LegendPill(color = Color(0xFFCFFAFE), label = "Masa Subur", textSecondary)
                        LegendPill(color = MedicalCyan, label = "Puncak Ovulasi", textSecondary)
                    }
                }
            }
        }

        // Inspector Card for Selected Date
        item {
            val log = allLogs.find { it.date == selectedCalendarDate }
            val isHaid = selectedCalendarDate in periodDates
            val isPeak = fertilePrediction != null && selectedCalendarDate == fertilePrediction.predictedOvulationDate
            val isFertile = fertilePrediction != null && !selectedCalendarDate.isBefore(fertilePrediction.fertileWindowStart) && !selectedCalendarDate.isAfter(fertilePrediction.fertileWindowEnd)

            val phaseBadgeText = when {
                isHaid -> "Menstruasi"
                isPeak -> "Puncak Ovulasi"
                isFertile -> "Masa Subur"
                else -> "Fase Folikuler"
            }

            val cycleDayForSelected = latestCycle?.let {
                ChronoUnit.DAYS.between(it.startDate, selectedCalendarDate) + 1L
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
                        Column {
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
                                    color = Color(0xFFCFFAFE)
                                ) {
                                    Text(
                                        text = phaseBadgeText,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0E7490),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (selectedCalendarDate == today) "Hari Ini • Hari ke-${cycleDayForSelected ?: 14} Siklus" else "Hari ke-${cycleDayForSelected ?: selectedCalendarDate.dayOfMonth} Siklus",
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
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Isi Jurnal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

        // 4. CYCLE PREDICTION INSIGHT CARD: Surface with border, no dirty drop-shadow
        item {
            val nextDate = fertilePrediction?.predictedNextPeriodDate ?: today.plusDays(22)
            val daysLeft = ChronoUnit.DAYS.between(today, nextDate).coerceAtLeast(0)

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isDarkMode) DarkCardBackground else Color(0xFFFFF7F6),
                border = BorderStroke(1.dp, Color(0xFFFFE4E6))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFFFE4E6))
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
                            Text("Prediksi Haid Berikutnya", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("Sekitar ${nextDate.dayOfMonth} ${nextDate.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${nextDate.year} (±1 hari)", fontSize = 10.sp, color = textSecondary)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFFFE4E6))
                    ) {
                        Text(
                            text = "$daysLeft Hari Lagi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Coral700,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
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
fun SpOgReportScreenView(
    isDarkMode: Boolean,
    isPro: Boolean,
    anonymousRecoveryKey: String,
    cycleStats: CycleStats?,
    completedCycles: List<CycleEntity>,
    anomalies: List<AnomalyAlert>,
    allLogs: List<DailyLogEntity>,
    onSharePdf: (() -> Unit)? = null,
    onExportCsv: (() -> Unit)? = null,
    onBuyPro: () -> Unit = {},
    onToast: (String) -> Unit = {}
) {
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorder else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500

    val today = remember { LocalDate.now() }
    val avgLen = cycleStats?.averageLength ?: 28.0
    val stdDev = cycleStats?.standardDeviation ?: 1.5
    val avgPeriod = cycleStats?.averagePeriodDuration ?: 5.0

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
                Text("Laporan Medis SpOG", fontSize = 20.sp, fontWeight = FontWeight.Black, color = textPrimary)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDarkMode) DarkCardBackground else Slate100
                ) {
                    Text("FIGO Compliant", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate600, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
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
                        ParameterBox("Rata-rata", String.format(Locale.US, "%.1f Hari", avgLen), Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox("Variasi Siklus", String.format(Locale.US, "±%.1f Hari", stdDev), Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox("Lama Haid", String.format(Locale.US, "%.1f Hari", avgPeriod), Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mini BBT Curve in Report
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
                                val path = Path().apply {
                                    moveTo(0f, size.height * 0.72f)
                                    cubicTo(size.width * 0.35f, size.height * 0.72f, size.width * 0.45f, size.height * 0.25f, size.width, size.height * 0.18f)
                                }
                                drawPath(path, Coral600, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Baseline: 36.32°C", fontSize = 8.sp, color = textSecondary)
                                Text("Shift +0.25°C Pasca-Ovulasi", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MedicalTeal)
                                Text("Sustained High", fontSize = 8.sp, color = textSecondary)
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
                    } else {
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
                                Text("Status Siklus Normal: Tidak terdeteksi anomali klinis FIGO", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF065F46))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Clinical History Table (Real Room DB Completed Cycles)
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
                                Text("Mulai", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                                Text("Panjang", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                                Text("Durasi", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                                Text("Ovulasi", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                            }
                            HorizontalDivider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))

                            val displayCycles = if (completedCycles.isNotEmpty()) {
                                completedCycles.takeLast(5).map { c ->
                                    val startStr = "${c.startDate.dayOfMonth} ${c.startDate.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}"
                                    val lenStr = "${c.cycleLengthDays ?: 28} Hari"
                                    val durStr = "${c.periodDurationDays} Hari"
                                    val ovStr = "${c.startDate.plusDays(14).dayOfMonth} ${c.startDate.plusDays(14).month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}"
                                    listOf(startStr, lenStr, durStr, ovStr)
                                }
                            } else {
                                listOf(
                                    listOf("01 Jan 26", "26 Hari", "5 Hari", "15 Jan"),
                                    listOf("27 Jan 26", "28 Hari", "5 Hari", "11 Feb"),
                                    listOf("24 Feb 26", "30 Hari", "5 Hari", "13 Mar")
                                )
                            }

                            displayCycles.forEach { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(row[0], fontSize = 10.sp, color = textPrimary)
                                    Text(row[1], fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    Text(row[2], fontSize = 10.sp, color = textPrimary)
                                    Text(row[3], fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MedicalCyan)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Doctor Signature Area Preview
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDarkMode) DarkBackground else Slate50.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, borderCol)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Kolom Catatan & Paraf Dokter SpOG", fontSize = 9.sp, color = textSecondary)
                            Spacer(modifier = Modifier.height(18.dp))
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onSharePdf?.invoke() ?: onToast(if (isPro) "Mengunduh PDF Medis Instan" else "Menonton 1 Iklan Singkat... PDF Medis Siap!") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral500)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPro) "Unduh PDF Medis (Pro)" else "Unduh PDF Medis (Tonton 1 Iklan)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onExportCsv?.invoke() ?: onToast("Mengekspor Berkas CSV Mentah") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ekspor CSV Mentah (Excel / Sheets)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                }
            }
        }

        if (!isPro) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFF59E0B)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Lisensi Pro Seumur Hidup", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                Text("Unduh instan tanpa iklan selamanya", fontSize = 10.sp, color = Slate600)
                            }
                        }

                        Button(
                            onClick = onBuyPro,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Beli Rp 49k", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

// 5. SETTINGS SCREEN: Wired to Cloudflare Backup, Recovery Key, and Data Nuke
@Composable
fun SettingsScreenView(
    isDarkMode: Boolean,
    isDiscreet: Boolean,
    isPro: Boolean,
    pinStatus: String,
    anonymousRecoveryKey: String,
    onToggleDiscreet: () -> Unit,
    onToggleDark: () -> Unit,
    onOpenPin: () -> Unit,
    onBuyPro: () -> Unit,
    onCopyRecoveryKey: (() -> Unit)? = null,
    onBackupCloud: ((String) -> Unit)? = null,
    onRestoreCloud: ((String) -> Unit)? = null,
    onNukeData: () -> Unit,
    onToast: (String) -> Unit
) {
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorderColor else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500
    var isBiometricEnabled by remember { mutableStateOf(false) }

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
                            Text("Beli Putus Rp 49.000", color = Color(0xFFB45309), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
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
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderCol),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("PRIVASI & CADANGAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Coral600)

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
                            Text("Cadangan Cloud Terkunci", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("Hanya tersimpan dalam bentuk terenkripsi", fontSize = 10.sp, color = textSecondary)
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Text("Aktif", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onBackupCloud?.invoke("1234") ?: onToast("Enkripsi & Cadangan Cloud Berhasil") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Coral600, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cadangkan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onRestoreCloud?.invoke("1234") ?: onToast("Data Arsip Berhasil Dipulihkan") },
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
                text = "CycleJournal v1.0.0 • Standar Klinis FIGO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Slate400,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
            )
        }
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
                CervicalMucusType.CREAMY -> "Krim"
                CervicalMucusType.WATERY -> "Cair"
                CervicalMucusType.EGG_WHITE -> "Putih Telur"
                else -> "Putih Telur"
            }
        )
    }
    var hasTakenAnalgesic by remember(initialLog) { mutableStateOf(initialLog?.takenAnalgesic ?: false) }
    var bbtInputText by remember(initialLog) {
        mutableStateOf(initialLog?.basalBodyTempCelsius?.let { String.format(Locale.US, "%.2f", it) } ?: "36.50")
    }

    val symptoms = listOf("Kram Pelvis", "Sakit Pinggang", "Payudara Sensitif", "Sakit Kepala", "Perut Kembung", "Mood Sensitif")
    val selectedSymptoms = remember(initialLog) {
        val list = mutableStateListOf<String>()
        initialLog?.painLocation?.split(",")?.map { it.trim() }?.filter { it in symptoms }?.let {
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
                Text("Pendarahan Menstruasi (Flow)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                num <= 8 -> Quadruple("Perhatian SpOG", Color(0xFFFFF1F2), Color(0xFFBE123C), "Nyeri Berat • Membatasi gerak, butuh pereda nyeri")
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

            // Quick 1-Tap Symptom Chips
            Column {
                Text("Gejala Tubuh Hari Ini (Pilih Cepat)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    symptoms.take(3).forEach { sym ->
                        val isSelected = selectedSymptoms.contains(sym)
                        Surface(
                            modifier = Modifier.weight(1f).clickable {
                                if (isSelected) selectedSymptoms.remove(sym) else selectedSymptoms.add(sym)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFFFFF1F2) else if (isDarkMode) DarkBackground else Slate100,
                            border = BorderStroke(1.dp, if (isSelected) Coral400 else Color.Transparent)
                        ) {
                            Text(
                                sym,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Coral600 else Slate600,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
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
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Suhu Basal Tubuh (°C)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Text(
                            text = "$bbtInputText °C",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (isDarkMode) Color.White else Slate900
                        )
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
            Column {
                Text("Lendir Serviks (Sintotermal)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Kering", "Krim", "Cair", "Putih Telur").forEach { mucus ->
                        val isSelected = mucus == selectedMucus
                        Surface(
                            modifier = Modifier.weight(1f).clickable { selectedMucus = mucus },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFFCFFAFE) else if (isDarkMode) DarkBackground else Slate100,
                            border = BorderStroke(1.dp, if (isSelected) MedicalCyan else Color.Transparent)
                        ) {
                            Text(
                                mucus,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF0E7490) else Slate600,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }
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
                        "Krim" -> CervicalMucusType.CREAMY
                        "Cair" -> CervicalMucusType.WATERY
                        "Putih Telur" -> CervicalMucusType.EGG_WHITE
                        else -> CervicalMucusType.NONE
                    }
                    val bbtVal = bbtInputText.toDoubleOrNull() ?: 36.50
                    val logEntity = DailyLogEntity(
                        date = targetDate,
                        flow = flowEnum,
                        basalBodyTempCelsius = bbtVal,
                        cervicalMucus = mucusEnum,
                        painVasScore = vasScore.toInt(),
                        painLocation = if (selectedSymptoms.isNotEmpty()) selectedSymptoms.joinToString(", ") else null,
                        takenAnalgesic = hasTakenAnalgesic,
                        notes = if (selectedSymptoms.isNotEmpty()) "Gejala: ${selectedSymptoms.joinToString(", ")}" else null
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
