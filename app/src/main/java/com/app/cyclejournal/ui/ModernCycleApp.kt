package com.app.cyclejournal.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image as AppLogoImage
import androidx.compose.ui.geometry.Offset
import com.app.cyclejournal.R
import com.app.cyclejournal.data.local.entity.CervicalMucusType
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import com.app.cyclejournal.domain.model.CycleStats
import com.app.cyclejournal.domain.model.FertilePrediction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

val CoralLight = Color(0xFFFF8A71)
val CoralDeep = Color(0xFFFF5E7D)
val CoralBackground = Color(0xFFFFF1F2)
val CyanMedical = Color(0xFF06B6D4)
val TealMedical = Color(0xFF0D9488)
val DarkNavyBackground = Color(0xFF0B0F19)
val DarkCardSurface = Color(0xFF151D2E)
val DarkBorderColor = Color(0xFF243048)
val LightSurfaceBg = Color(0xFFFBFBFC)
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

val CoralPinkGradient = Brush.linearGradient(
    colors = listOf(CoralLight, CoralDeep)
)
val FertileCyanGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF14B8A6), Color(0xFF06B6D4))
)
val MenstrualRoseGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF43F5E), Color(0xFFBE123C))
)
val AmberProGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF59E0B), Color(0xFFEA580C))
)
val PearlIridescentGradient = Brush.radialGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFFEE2E2), Color(0xFFCBD5E1))
)

enum class AppScreen {
    SPLASH, DASHBOARD, CALENDAR, REPORT, SETTINGS
}

enum class CyclePhaseType {
    MENSTRUATION, FOLLICULAR, FERTILE, OVULATION, LUTEAL
}

data class DayLog(
    val dayOfMonth: Int,
    val dayLabel: String,
    val phaseName: String,
    val bbtString: String,
    val painVas: Int,
    val painDesc: String,
    val mucus: String,
    val phaseType: CyclePhaseType,
    val date: LocalDate? = null,
    val bbtValue: Double? = null
)

data class CalendarDateInfo(
    val dayOfMonth: Int,
    val title: String,
    val phaseBadge: String,
    val bbt: String,
    val pain: String,
    val mucus: String,
    val phaseType: CyclePhaseType
)

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
    onSavePin: (String) -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var isDarkMode by remember { mutableStateOf(false) }
    var isDiscreetMode by remember { mutableStateOf(false) }
    var isPinModalOpen by remember { mutableStateOf(false) }
    var isLogModalOpen by remember { mutableStateOf(false) }
    var pinStatusText by remember { mutableStateOf("Belum diatur (Opsional)") }
    var isProLicenseActive by remember(isProUserActive) { mutableStateOf(isProUserActive) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val weekDays = remember(allLogs, latestCycle, fertilePrediction) {
        // Bangun strip 7 hari dari data nyata (data layer)
        val last7 = allLogs.sortedBy { it.date }.takeLast(7)
        if (last7.isEmpty()) emptyList() else last7.map { it.toDayLog(latestCycle, fertilePrediction) }
    }
    val defaultDayLog = DayLog(
        LocalDate.now().dayOfMonth, shortDayName(LocalDate.now()), "Fase Folikuler",
        "-- °C", 0, "Bebas Nyeri", "Tidak ada", CyclePhaseType.FOLLICULAR,
        LocalDate.now(), null
    )
    var selectedDayLog by remember { mutableStateOf(weekDays.lastOrNull() ?: defaultDayLog) }

    // ===== REAL DATA DARI DATA LAYER (Room + engine) =====
    val todayCycleDay = latestCycle?.let {
        (ChronoUnit.DAYS.between(it.startDate, LocalDate.now()) + 1).toInt()
    }?.coerceAtLeast(1) ?: 1
    val ovulationCountdown = fertilePrediction?.let {
        ChronoUnit.DAYS.between(LocalDate.now(), it.predictedOvulationDate).toInt()
    }?.takeIf { it >= 0 }
    val nextPeriodCountdown = fertilePrediction?.let {
        ChronoUnit.DAYS.between(LocalDate.now(), it.predictedNextPeriodDate).toInt()
    }?.takeIf { it >= 0 }
    val averageCycle = cycleStats?.averageLength ?: 28.0
    val stdDev = cycleStats?.standardDeviation ?: 0.0

    val triggerToast: (String) -> Unit = { message ->
        toastMessage = message
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2400)
            toastMessage = null
        }
    }

    val backgroundColor = if (isDarkMode) DarkNavyBackground else LightSurfaceBg

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (currentScreen == AppScreen.SPLASH) {
            CycleSplashScreen(
                onEnterApp = { currentScreen = AppScreen.DASHBOARD }
            )
        } else {
            Scaffold(
                containerColor = backgroundColor,
                topBar = {
                    CycleTopAppBar(
                        isDarkMode = isDarkMode,
                        isDiscreetMode = isDiscreetMode,
                        onToggleDiscreet = {
                            isDiscreetMode = !isDiscreetMode
                            triggerToast(if (isDiscreetMode) "Mode Samaran Aktif: Istilah sensitif disamarkan" else "Mode Standar Aktif")
                        },
                        onToggleDarkMode = {
                            isDarkMode = !isDarkMode
                            triggerToast(if (isDarkMode) "Mode Gelap Subuh Aktif (Ramah Mata)" else "Mode Terang Aktif")
                        },
                        onOpenSettings = { currentScreen = AppScreen.SETTINGS }
                    )
                },
                bottomBar = {
                    CycleBottomNavBar(
                        currentScreen = currentScreen,
                        isDarkMode = isDarkMode,
                        onScreenSelect = { currentScreen = it },
                        onFabClick = { isLogModalOpen = true }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (currentScreen) {
                        AppScreen.DASHBOARD -> DashboardScreen(
                            isDarkMode = isDarkMode,
                            isDiscreetMode = isDiscreetMode,
                            weekDays = weekDays,
                            selectedDay = selectedDayLog,
                            cycleDay = todayCycleDay,
                            ovulationCountdown = ovulationCountdown,
                            nextPeriodCountdown = nextPeriodCountdown,
                            averageCycle = averageCycle,
                            stdDev = stdDev,
                            onSelectDay = { selectedDayLog = it },
                            onOpenCalendar = { currentScreen = AppScreen.CALENDAR },
                            onOpenLogModal = { isLogModalOpen = true }
                        )
                        AppScreen.CALENDAR -> CalendarScreen(
                            isDarkMode = isDarkMode,
                            selectedDay = selectedDayLog,
                            periodDates = periodDates,
                            fertilePrediction = fertilePrediction,
                            onOpenLogModal = { isLogModalOpen = true },
                            onDaySelected = { day ->
                                triggerToast("Memeriksa data tanggal $day ${LocalDate.now().month.name.lowercase()}")
                            }
                        )
                        AppScreen.REPORT -> MedicalReportScreen(
                            isDarkMode = isDarkMode,
                            isProUser = isProLicenseActive,
                            onSharePdf = {
                                onSharePdf?.invoke() ?: run {
                                    if (isProLicenseActive) {
                                        triggerToast("Membuka PDF Medis SpOG (Tanpa Iklan)")
                                    } else {
                                        triggerToast("Menonton 1 Iklan Singkat... PDF Medis Siap!")
                                    }
                                }
                            },
                            onExportCsv = {
                                onExportCsv?.invoke() ?: triggerToast("Berkas CSV mentah berhasil diekspor")
                            },
                            onUpgradePro = {
                                onBuyPro?.invoke() ?: run {
                                    isProLicenseActive = true
                                    triggerToast("Google Play Billing: Berhasil Upgrade ke Lifetime Pro!")
                                }
                            }
                        )
                        AppScreen.SETTINGS -> SettingsScreen(
                            isDarkMode = isDarkMode,
                            isDiscreetMode = isDiscreetMode,
                            isProUser = isProLicenseActive,
                            pinStatus = pinStatusText,
                            onOpenPinSetup = { isPinModalOpen = true },
                            onToggleDiscreet = {
                                isDiscreetMode = !isDiscreetMode
                                triggerToast(if (isDiscreetMode) "Mode Samaran Aktif" else "Mode Standar Aktif")
                            },
                            onToggleDarkMode = {
                                isDarkMode = !isDarkMode
                                triggerToast(if (isDarkMode) "Mode Gelap Subuh Aktif" else "Mode Terang Aktif")
                            },
                            onBuyPro = {
                                onBuyPro?.invoke() ?: run {
                                    isProLicenseActive = true
                                    triggerToast("Google Play Billing: Lisensi Pro Aktif Selamanya!")
                                }
                            },
                            onCopyRecoveryKey = {
                                triggerToast("Kunci Pemulihan Cadangan Disalin: $anonymousRecoveryKey")
                            },
                            onBackupCloud = { pin ->
                                onBackupCloud?.invoke(pin) ?: triggerToast("Cadangan Enkripsi Cloud Berhasil")
                            },
                            onRestoreCloud = { pin ->
                                onRestoreCloud?.invoke(pin) ?: triggerToast("Data Arsip Berhasil Dipulihkan")
                            },
                            onNukeData = {
                                onNukeData?.invoke() ?: run {
                                    triggerToast("Seluruh data lokal & cloud berhasil dibersihkan")
                                    currentScreen = AppScreen.SPLASH
                                }
                            }
                        )
                        AppScreen.SPLASH -> {}
                    }
                }
            }
        }

        if (isPinModalOpen) {
            PinSetupModalDialog(
                isDarkMode = isDarkMode,
                onDismiss = { isPinModalOpen = false },
                onPinSaved = { pin ->
                    onSavePin(pin)
                    pinStatusText = "Aktif (PIN 4-Digit)"
                    isPinModalOpen = false
                    triggerToast("Kunci PIN Keamanan Berhasil Diaktifkan")
                }
            )
        }

        if (isLogModalOpen) {
            DailyJournalLogModal(
                isDarkMode = isDarkMode,
                onDismiss = { isLogModalOpen = false },
                onSave = { log ->
                    // Wire ke data layer: simpan ke Room via CycleViewModel.saveDailyLog()
                    onSaveDailyLog(log)
                    isLogModalOpen = false
                    triggerToast("Jurnal Hari Ini Berhasil Disimpan")
                }
            )
        }

        toastMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 44.dp, start = 20.dp, end = 20.dp)
                    .shadow(16.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate900.copy(alpha = 0.94f))
                    .padding(horizontal = 18.dp, vertical = 10.dp)
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
    }
}

@Composable
fun CycleSplashScreen(onEnterApp: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "AuroraTransition")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val blobOffset1 by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob1"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF5F3), Color.White, Color(0xFFFFF0F3))
                )
            )
            .padding(24.dp)
    ) {
        // Living Aurora Ambient Glow Blobs
        Box(
            modifier = Modifier
                .offset(x = (-30 + blobOffset1).dp, y = (-20).dp)
                .size(240.dp)
                .clip(CircleShape)
                .background(CoralLight.copy(alpha = 0.28f))
                .blur(50.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 30.dp)
                .size(260.dp)
                .clip(CircleShape)
                .background(CoralDeep.copy(alpha = 0.22f))
                .blur(50.dp)
        )

        // Top Badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFF1F2))
                    .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "FIGO STANDARD",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoralDeep,
                    letterSpacing = 1.sp
                )
            }
            Text("v1.0", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(CoralPinkGradient)
                        .blur(26.dp)
                )

                // Official CycleJournal logo (transparent master asset)
                AppLogoImage(
                    painter = painterResource(id = R.drawable.logo_transparent),
                    contentDescription = "CycleJournal Logo",
                    modifier = Modifier
                        .size(124.dp)
                        .scale(pulseScale)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "Cycle",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate900,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Journal",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    style = TextStyle(brush = CoralPinkGradient)
                )
            }

            Text(
                text = "PRIVASI PENUH • STANDAR DOKTER KANDUNGAN",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Frosted Glass Trust Capsule
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.82f))
                    .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                    .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Slate900.copy(alpha = 0.05f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFECFDF5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Enkripsi Mandiri • 100% Offline di Ponsel",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate600
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onEnterApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = CoralDeep, spotColor = CoralDeep),
                colors = ButtonDefaults.buttonColors(containerColor = CoralDeep),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Masuk Aplikasi", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Text(
                text = "Mulai Gratis • Tanpa Pendaftaran Akun",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Slate400
            )
        }
    }
}

@Composable
fun CycleTopAppBar(
    isDarkMode: Boolean,
    isDiscreetMode: Boolean,
    onToggleDiscreet: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val cardBg = if (isDarkMode) DarkCardSurface else Color.White
    val textColor = if (isDarkMode) Color.White else Slate900
    val borderCol = if (isDarkMode) DarkBorderColor else Slate100

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppLogoImage(
                painter = painterResource(id = R.drawable.logo_transparent),
                contentDescription = "CycleJournal Logo",
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = if (isDiscreetMode) "MODE SAMARAN AKTIF" else "MODE PRIVAT OFFLINE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDiscreetMode) Slate400 else CoralDeep
                )
                Text(
                    text = if (isDiscreetMode) "CJ Journal" else "CycleJournal",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
                onClick = onToggleDiscreet,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardBg)
                    .border(1.dp, borderCol, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = if (isDiscreetMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Mode Samaran",
                    tint = if (isDiscreetMode) CoralDeep else Slate500,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onToggleDarkMode,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardBg)
                    .border(1.dp, borderCol, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Mode Gelap Subuh",
                    tint = if (isDarkMode) Color(0xFFFBBF24) else Slate500,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardBg)
                    .border(1.dp, borderCol, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = "Pengaturan",
                    tint = Slate500,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    isDarkMode: Boolean,
    isDiscreetMode: Boolean,
    weekDays: List<DayLog>,
    selectedDay: DayLog,
    cycleDay: Int,
    ovulationCountdown: Int?,
    nextPeriodCountdown: Int?,
    averageCycle: Double,
    stdDev: Double,
    onSelectDay: (DayLog) -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenLogModal: () -> Unit
) {
    val cardBg = if (isDarkMode) DarkCardSurface else Color.White
    val borderCol = if (isDarkMode) DarkBorderColor else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500

    val heroGradient = when (selectedDay.phaseType) {
        CyclePhaseType.MENSTRUATION -> MenstrualRoseGradient
        CyclePhaseType.FERTILE, CyclePhaseType.OVULATION -> FertileCyanGradient
        else -> CoralPinkGradient
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // HERO PHASE CARD
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(heroGradient)
                    .shadow(12.dp, RoundedCornerShape(26.dp), ambientColor = CoralDeep.copy(alpha = 0.35f))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isDiscreetMode) "FASE 02" else selectedDay.phaseName.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isDiscreetMode) "Periode Tengah" else selectedDay.phaseName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Text(
                            text = if (isDiscreetMode) "Pencatatan normal berlangsung" else (ovulationCountdown?.let { cd ->
                                when {
                                    cd == 0 -> "Ovulasi diperkirakan hari ini"
                                    cd == 1 -> "Ovulasi dalam 1 hari"
                                    else -> "Ovulasi dalam $cd hari"
                                }
                            } ?: "Perkiraan ovulasi belum tersedia") + (nextPeriodCountdown?.let { " • Haid dalam $it hari" } ?: ""),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Column {
                                    Text("RATA-RATA", fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                                    Text("${averageCycle.roundToInt()} Hari (±${stdDev.roundToInt()})", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Column {
                                    Text("KONSEPSI", fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                                    Text(
                                        if (selectedDay.phaseType == CyclePhaseType.FERTILE) "Tinggi (85%)" else "Rendah (<5%)",
                                        fontSize = 11.sp,
                                        color = if (selectedDay.phaseType == CyclePhaseType.FERTILE) Color(0xFFFDE68A) else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(82.dp)) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.25f),
                                style = Stroke(width = 16f)
                            )
                            drawArc(
                                color = Color.White,
                                startAngle = -90f,
                                sweepAngle = (cycleDay.toFloat() / averageCycle.toFloat()) * 360f,
                                useCenter = false,
                                style = Stroke(width = 16f, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hari", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("$cycleDay", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("dari ${averageCycle.roundToInt()}", fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(cardBg)
                    .border(1.dp, borderCol, RoundedCornerShape(22.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Minggu Ini • Sep 2026", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(CoralDeep))
                        }

                        Text(
                            text = "Buka Kalender Penuh >",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralDeep,
                            modifier = Modifier.clickable { onOpenCalendar() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDays.forEach { day ->
                            val isSelected = day.dayOfMonth == selectedDay.dayOfMonth
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) Slate900 else (if (isDarkMode) DarkNavyBackground else Slate100))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) CoralLight else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onSelectDay(day) }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = day.dayLabel,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) CoralLight else Slate400
                                )
                                Text(
                                    text = "${day.dayOfMonth}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (day.phaseType) {
                                                CyclePhaseType.MENSTRUATION -> Color(0xFFF43F5E)
                                                CyclePhaseType.FERTILE -> Color(0xFF06B6D4)
                                                CyclePhaseType.OVULATION -> Color(0xFF0D9488)
                                                else -> Slate400
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(cardBg)
                    .border(1.dp, borderCol, RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFF1F2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = CoralDeep, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Tren Kurva Suhu Basal (BBT)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Text("Aturan Pergeseran Biphasik 3-over-6", fontSize = 10.sp, color = textSecondary)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFECFDF5))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Normal", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        // ==== Data BBT nyata dari data layer ====
                        val bbtValues = weekDays.mapNotNull { it.bbtValue }
                        val minT = bbtValues.minOrNull() ?: 36.30
                        val maxT = bbtValues.maxOrNull() ?: 36.50
                        val range = ((maxT - minT).takeIf { it > 0.01 }) ?: 1.0
                        val avgT = bbtValues.average().takeIf { bbtValues.isNotEmpty() } ?: 36.40
                        val coverLineY = (height * (0.85f - 0.65f * ((avgT - minT) / range))).toFloat()

                        // Baseline Coverline (rata-rata BBT)
                        drawLine(
                            color = Color(0xFFCBD5E1),
                            start = Offset(0f, coverLineY),
                            end = Offset(width, coverLineY),
                            strokeWidth = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )

                        val points = if (bbtValues.size >= 2) {
                            bbtValues.mapIndexed { i, v ->
                                val x = width * (0.08f + 0.84f * (i.toFloat() / (bbtValues.size - 1)))
                                val y = (height * (0.85f - 0.65f * ((v - minT) / range))).toFloat().coerceIn(0f, height)
                                Offset(x, y)
                            }
                        } else {
                            listOf(Offset(width * 0.5f, height * 0.5f))
                        }

                        val curvePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val cur = points[i]
                                cubicTo(
                                    (prev.x + cur.x) / 2f, prev.y,
                                    (prev.x + cur.x) / 2f, cur.y,
                                    cur.x, cur.y
                                )
                            }
                        }

                        val fillPath = Path().apply {
                            addPath(curvePath)
                            lineTo(points.last().x, height)
                            lineTo(points.first().x, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(CoralDeep.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )

                        drawPath(
                            path = curvePath,
                            color = CoralDeep,
                            style = Stroke(width = 6f, cap = StrokeCap.Round)
                        )

                        points.forEachIndexed { index, point ->
                            val dotColor = if (index == points.lastIndex) Slate900 else CoralDeep
                            drawCircle(color = Color.White, radius = 9f, center = point)
                            drawCircle(color = dotColor, radius = 6f, center = point)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Folikuler (Rendah)", fontSize = 9.sp, color = textSecondary)
                        Text("Shift Progesteron (+0.28°C)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoralDeep)
                        Text("Luteal (Tinggi)", fontSize = 9.sp, color = textSecondary)
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(cardBg)
                    .border(1.dp, borderCol, RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CoralDeep))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Catatan Hari Ini (${selectedDay.dayOfMonth} Sep)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        }

                        Text(
                            text = "Ubah Catatan >",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralDeep,
                            modifier = Modifier.clickable { onOpenLogModal() }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDarkMode) DarkNavyBackground else Slate100)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Suhu Basal", fontSize = 9.sp, color = textSecondary)
                                Text(selectedDay.bbtString, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textPrimary)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDarkMode) DarkNavyBackground else Slate100)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Lendir Serviks", fontSize = 9.sp, color = textSecondary)
                                Text(selectedDay.mucus, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF1F2))
                            .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Tingkat Nyeri (Skala 0-10)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBE123C))
                                Text(
                                    text = "${selectedDay.painVas} / 10 • ${selectedDay.painDesc}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9F1239)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFBE123C))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("Perhatian SpOG", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarScreen(
    isDarkMode: Boolean,
    selectedDay: DayLog,
    periodDates: Set<LocalDate>,
    fertilePrediction: FertilePrediction?,
    onOpenLogModal: () -> Unit,
    onDaySelected: (Int) -> Unit
) {
    val cardBg = if (isDarkMode) DarkCardSurface else Color.White
    val borderCol = if (isDarkMode) DarkBorderColor else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500

    var currentSelectedCalendarDay by remember { mutableStateOf(LocalDate.now().dayOfMonth) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PETA SIKLUS & OVULASI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoralDeep)
                    Text("Kalender Siklus", fontSize = 20.sp, fontWeight = FontWeight.Black, color = textPrimary)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CoralBackground)
                        .clickable {
                            currentSelectedCalendarDay = 14
                            onDaySelected(14)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Hari Ini", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoralDeep)
                }
            }
        }

        // 30-Day Grid Month Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(cardBg)
                    .border(1.dp, borderCol, RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = textSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("September 2026", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textPrimary)
                            Text("Siklus #8 • Rata-rata 28 Hari", fontSize = 10.sp, color = textSecondary)
                        }
                        IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = textSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val daysHeader = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        daysHeader.forEach { header ->
                            Text(
                                text = header,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val days = (1..30).toList()
                    val rows = days.chunked(7)
                    rows.forEach { rowDays ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            rowDays.forEach { d ->
                                val today = LocalDate.now()
                                val isMenstruation = periodDates.any { it.dayOfMonth == d && it.month == today.month && it.year == today.year }
                                val isOvulation = fertilePrediction?.let { fp ->
                                    val od = fp.predictedOvulationDate
                                    od.dayOfMonth == d && od.month == today.month && od.year == today.year
                                } == true
                                val isFertile = fertilePrediction?.let { fp ->
                                    !isMenstruation && d in fp.fertileWindowStart.dayOfMonth..fp.fertileWindowEnd.dayOfMonth &&
                                        fp.fertileWindowStart.month == today.month
                                } == true
                                val isSelected = d == currentSelectedCalendarDay

                                val (cellBg, cellText) = when {
                                    isSelected -> Slate900 to Color.White
                                    isOvulation -> TealMedical to Color.White
                                    isMenstruation -> Color(0xFFFEE2E2) to Color(0xFFBE123C)
                                    isFertile -> Color(0xFFCFFAFE) to Color(0xFF0E7490)
                                    else -> Color.Transparent to textPrimary
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(cellBg)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = when {
                                                isSelected -> CoralLight
                                                isMenstruation -> Color(0xFFFDA4AF)
                                                isFertile -> Color(0xFF99F6E4)
                                                isOvulation -> TealMedical
                                                else -> Color(0xFFCBD5E1)
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            currentSelectedCalendarDay = d
                                            onDaySelected(d)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$d",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected || isOvulation) FontWeight.Black else FontWeight.SemiBold,
                                        color = cellText
                                    )
                                }
                            }
                            if (rowDays.size < 7) {
                                repeat(7 - rowDays.size) {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFEE2E2)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Haid", fontSize = 10.sp, color = textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFCFFAFE)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Subur", fontSize = 10.sp, color = textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TealMedical))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Puncak Ovulasi", fontSize = 10.sp, color = textSecondary)
                        }
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(cardBg)
                    .border(1.dp, borderCol, RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("$currentSelectedCalendarDay September 2026", fontSize = 13.sp, fontWeight = FontWeight.Black, color = textPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                val tagText = when {
                                    currentSelectedCalendarDay in 8..12 -> "Haid"
                                    currentSelectedCalendarDay == 20 -> "Ovulasi"
                                    currentSelectedCalendarDay in 17..21 -> "Subur"
                                    else -> "Fase Tenang"
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFCFFAFE))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(tagText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0E7490))
                                }
                            }
                            Text(
                                text = if (currentSelectedCalendarDay == 14) "Hari Ini • Hari ke-14 Siklus" else "Hari ke-$currentSelectedCalendarDay Siklus",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }

                        Button(
                            onClick = onOpenLogModal,
                            colors = ButtonDefaults.buttonColors(containerColor = CoralDeep),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Isi Jurnal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "Suhu Basal" to "36.50 °C",
                            "Lendir Serviks" to "Putih Telur",
                            "Skala Nyeri" to "Sedang (VAS 7)"
                        ).forEach { (label, value) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDarkMode) DarkNavyBackground else Slate100)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(label, fontSize = 9.sp, color = textSecondary)
                                    Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cycle Prediction Insight Card (22 Hari Lagi)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFFF1F2), Color(0xFFFFF7ED))
                        )
                    )
                    .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .shadow(2.dp, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CoralDeep, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Prediksi Haid Berikutnya", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Text("Sekitar 6 Oktober 2026 (±1 hari)", fontSize = 10.sp, color = Slate600)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("22 Hari Lagi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoralDeep)
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalReportScreen(
    isDarkMode: Boolean,
    isProUser: Boolean,
    onSharePdf: () -> Unit,
    onExportCsv: () -> Unit,
    onUpgradePro: () -> Unit
) {
    val cardBg = if (isDarkMode) DarkCardSurface else Color.White
    val borderCol = if (isDarkMode) DarkBorderColor else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Laporan Medis SpOG", fontSize = 20.sp, fontWeight = FontWeight.Black, color = textPrimary)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDarkMode) DarkNavyBackground else Slate100)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("FIGO Compliant", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate700)
                }
            }
        }

        // Document Container
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(cardBg)
                    .border(1.dp, borderCol, RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("REKAPITULASI SIKLUS KLINIS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = textPrimary)
                            Text("ID Anonim: px-7f9a2b1c4e0d", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = textSecondary)
                        }
                        Text("14 Sep 2026", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "Rata-rata" to "28.0 Hari",
                            "Variasi (SD)" to "±1.5 Hari",
                            "Lama Haid" to "5.0 Hari"
                        ).forEach { (title, value) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDarkMode) DarkNavyBackground else Slate100)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(title, fontSize = 8.sp, color = textSecondary)
                                    Text(value, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = textPrimary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mini BBT Chart Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDarkMode) DarkNavyBackground else Slate100)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Pola Temperatur Biphasik (Ovulasi Terkonfirmasi)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Canvas(modifier = Modifier.fillMaxWidth().height(36.dp)) {
                                drawLine(
                                    color = Color(0xFFCBD5E1),
                                    start = Offset(0f, size.height * 0.5f),
                                    end = Offset(size.width, size.height * 0.5f),
                                    strokeWidth = 2f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                )
                                val path = Path().apply {
                                    moveTo(0f, size.height * 0.7f)
                                    cubicTo(size.width * 0.3f, size.height * 0.7f, size.width * 0.4f, size.height * 0.3f, size.width, size.height * 0.2f)
                                }
                                drawPath(path, CoralDeep, style = Stroke(width = 4f, cap = StrokeCap.Round))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Baseline: 36.32°C", fontSize = 8.sp, color = textSecondary)
                                Text("Shift +0.25°C Pasca-Ovulasi", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TealMedical)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Anomaly Alert Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF1F2))
                            .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFBE123C), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Perhatian: Nyeri Haid Cukup Intens", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBE123C))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tercatat skala nyeri 7/10 disertai konsumsi obat pereda nyeri. Riwayat ini sangat berguna saat konsultasi bersama dokter SpOG Anda.",
                                fontSize = 10.sp,
                                color = Color(0xFF9F1239)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Clinical History Table
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDarkMode) DarkNavyBackground else Slate100)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mulai", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                            Text("Panjang", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                            Text("Durasi", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                            Text("Ovulasi", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                        }

                        listOf(
                            listOf("01 Jan 26", "26 Hari", "5 Hari", "15 Jan"),
                            listOf("27 Jan 26", "28 Hari", "5 Hari", "11 Feb"),
                            listOf("24 Feb 26", "30 Hari", "5 Hari", "13 Mar")
                        ).forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(row[0], fontSize = 10.sp, color = textPrimary)
                                Text(row[1], fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Text(row[2], fontSize = 10.sp, color = textPrimary)
                                Text(row[3], fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TealMedical)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Doctor Signature Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDarkMode) DarkNavyBackground else Slate50)
                            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Kolom Catatan Diagnosa & Paraf Dokter SpOG", fontSize = 9.sp, color = textSecondary)
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSharePdf,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDeep),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isProUser) "Unduh PDF Medis Siap Dokter" else "Unduh PDF Medis (Tonton 1 Iklan Singkat)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onExportCsv,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Ekspor CSV Mentah (Excel / Sheets)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                }

                if (!isProUser) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFEF3C7), Color(0xFFFFEDD5))
                                )
                            )
                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF59E0B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Lisensi Pro Seumur Hidup", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                    Text("Unduh instan tanpa iklan selamanya", fontSize = 10.sp, color = Slate600)
                                }
                            }

                            Button(
                                onClick = onUpgradePro,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(10.dp),
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
}

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    isDiscreetMode: Boolean,
    isProUser: Boolean,
    pinStatus: String,
    onOpenPinSetup: () -> Unit,
    onToggleDiscreet: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onBuyPro: () -> Unit,
    onCopyRecoveryKey: () -> Unit,
    onBackupCloud: (String) -> Unit,
    onRestoreCloud: (String) -> Unit,
    onNukeData: () -> Unit
) {
    val cardBg = if (isDarkMode) DarkCardSurface else Color.White
    val borderCol = if (isDarkMode) DarkBorderColor else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Text("Pengaturan", fontSize = 20.sp, fontWeight = FontWeight.Black, color = textPrimary)
        }

        // GROUP 0: STATUS LISENSI
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        if (isProUser) {
                            Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF0D9488)))
                        } else {
                            AmberProGradient
                        }
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isProUser) "STATUS: AKTIF SEUMUR HIDUP" else "VERSI GRATIS (DIDUKUNG IKLAN)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isProUser) "Lisensi Pro Seumur Hidup" else "Upgrade ke Lifetime Pro",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = if (isProUser) "100% bebas iklan dan semua fitur terbuka." else "Beli putus sekali: tanpa iklan & sinkronisasi awan terenkripsi.",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        if (!isProUser) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onBuyPro,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                            ) {
                                Text("Beli Putus Rp 49.000", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFEA580C))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        // GROUP 1: KEAMANAN & KUNCI APLIKASI
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(cardBg)
                    .border(1.dp, borderCol, RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("KEAMANAN & KUNCI APLIKASI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CoralDeep)

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
                            onClick = onOpenPinSetup,
                            colors = ButtonDefaults.buttonColors(containerColor = CoralDeep),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Atur PIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(color = borderCol)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Kunci Sidik Jari / Wajah", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("Buka cepat saat ponsel dipinjam", fontSize = 11.sp, color = textSecondary)
                        }
                        var biometricChecked by remember { mutableStateOf(false) }
                        Switch(
                            checked = biometricChecked,
                            onCheckedChange = { biometricChecked = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CoralDeep)
                        )
                    }
                }
            }
        }

        // GROUP 2: PRIVASI & CADANGAN AWAN
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(cardBg)
                    .border(1.dp, borderCol, RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("PRIVASI & CADANGAN AWAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CoralDeep)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kunci Pemulihan Cadangan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text("Salin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoralDeep, modifier = Modifier.clickable { onCopyRecoveryKey() })
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDarkMode) DarkNavyBackground else Slate100)
                            .padding(10.dp)
                    ) {
                        Text("px-7f9a2b1c4e0d", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = textSecondary)
                    }

                    var backupPin by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = backupPin,
                        onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) backupPin = it },
                        label = { Text("PIN 4-Digit untuk Cadangan/Pemulihan", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onBackupCloud(backupPin) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cadangkan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onRestoreCloud(backupPin) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Pulihkan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // GROUP 3: ZONA BERSIHKAN DATA
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFFFF1F2))
                    .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("HAPUS DATA & RESET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBE123C))
                    Text(
                        "Menghapus seluruh catatan siklus lokal di ponsel dan cadangan cloud secara permanen sesuai hak privasi Anda.",
                        fontSize = 11.sp,
                        color = Color(0xFF9F1239)
                    )
                    Button(
                        onClick = onNukeData,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hapus Seluruh Data Permanen", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PinSetupModalDialog(
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onPinSaved: (String) -> Unit
) {
    var pinText by remember { mutableStateOf("") }
    val cardBg = if (isDarkMode) DarkCardSurface else Color.White
    val textPrimary = if (isDarkMode) Color.White else Slate900

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(cardBg)
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(24.dp))
                    Text("Setup Kunci PIN 4-Digit", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = textPrimary)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Slate500)
                    }
                }

                Text("Lindungi catatan intim saat ponsel Anda dipinjam orang lain", fontSize = 11.sp, color = Slate500, textAlign = TextAlign.Center)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < pinText.length
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) CoralDeep else Color.Transparent)
                                .border(2.dp, if (isFilled) CoralDeep else Slate400, CircleShape)
                        )
                    }
                }

                val digits = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "<")
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    digits.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { char ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (char.isNotEmpty()) (if (isDarkMode) DarkNavyBackground else Slate100) else Color.Transparent)
                                        .clickable(enabled = char.isNotEmpty()) {
                                            if (char == "<") {
                                                if (pinText.isNotEmpty()) pinText = pinText.dropLast(1)
                                            } else if (pinText.length < 4) {
                                                pinText += char
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (char == "<") {
                                        Icon(Icons.Default.Backspace, contentDescription = null, tint = textPrimary, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text(char, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { onPinSaved(pinText) },
                    enabled = pinText.length == 4,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDeep),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Simpan Kunci Keamanan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DailyJournalLogModal(
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onSave: (DailyLogEntity) -> Unit
) {
    var vasScore by remember { mutableStateOf(7f) }
    var selectedFlow by remember { mutableStateOf("Sedang") }
    var selectedMucus by remember { mutableStateOf("Putih Telur") }
    val symptoms = listOf("Kram Pelvis", "Sakit Pinggang", "Payudara Sensitif", "Sakit Kepala / Migrain", "Perut Kembung", "Mood Sensitif")
    val selectedSymptoms = remember { mutableStateListOf("Kram Pelvis", "Sakit Pinggang") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDarkMode) DarkCardSurface else Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Jurnal Kondisi Hari Ini", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text(formatIdDate(LocalDate.now()), fontSize = 11.sp, color = Slate500)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            // Flow Pills
            Text("Pendarahan Menstruasi (Flow)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Tidak", "Bercak", "Ringan", "Sedang", "Deras").forEach { flow ->
                    val isSelected = flow == selectedFlow
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) CoralDeep else Slate100)
                            .clickable { selectedFlow = flow }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            flow,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Slate700
                        )
                    }
                }
            }

            // Functional Impact Rating
            val num = vasScore.toInt()
            val (badgeText, badgeBg, descText, cardBg, textCol) = when (num) {
                0 -> Tuple5("Bebas Nyeri", Color(0xFFD1FAE5), "Bebas Nyeri • Nyaman beraktivitas", Color(0xFFECFDF5), Color(0xFF047857))
                in 1..3 -> Tuple5("Ringan", Slate100, "Nyeri Ringan • Terasa pegal, aktivitas normal", Slate100, Slate700)
                in 4..6 -> Tuple5("Perlu Pantauan", Color(0xFFFEF3C7), "Nyeri Sedang • Mengganggu, butuh jeda istirahat", Color(0xFFFFFBEB), Color(0xFFB45309))
                in 7..8 -> Tuple5("Perhatian SpOG", Color(0xFFFECDD3), "Nyeri Berat • Membatasi gerak, butuh pereda nyeri", Color(0xFFFFF1F2), Color(0xFFBE123C))
                else -> Tuple5("Konsultasi Segera", Color(0xFFE11D48), "Sangat Hebat • Tirah baring total / tidak bisa bangun", Color(0xFFFEE2E2), Color(0xFF991B1B))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(1.dp, badgeBg, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tingkat Nyeri (Skala 0–10)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (num >= 9) Color.White else textCol
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$num / 10",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = textCol
                        )
                        Text(
                            text = descText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textCol,
                            textAlign = TextAlign.End
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

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
            Text("Gejala Tubuh Hari Ini (Pilih Cepat)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                symptoms.forEach { sym ->
                    val isChecked = selectedSymptoms.contains(sym)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isChecked) CoralBackground else Slate100)
                            .border(1.dp, if (isChecked) CoralDeep else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable {
                                if (isChecked) selectedSymptoms.remove(sym) else selectedSymptoms.add(sym)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                sym,
                                fontSize = 11.sp,
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                color = if (isChecked) CoralDeep else Slate700
                            )
                            if (isChecked) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Check, contentDescription = null, tint = CoralDeep, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onSave(
                        DailyLogEntity(
                            date = LocalDate.now(),
                            flow = flowFromLabel(selectedFlow),
                            basalBodyTempCelsius = null,
                            cervicalMucus = mucusFromLabel(selectedMucus),
                            painVasScore = vasScore.toInt(),
                            painLocation = null,
                            takenAnalgesic = false,
                            notes = if (selectedSymptoms.isEmpty()) null else selectedSymptoms.joinToString(", ")
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralDeep),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Simpan Catatan Hari Ini", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)

private fun flowFromLabel(label: String): FlowIntensity = when (label) {
    "Tidak" -> FlowIntensity.NONE
    "Bercak" -> FlowIntensity.SPOTTING
    "Ringan" -> FlowIntensity.LIGHT
    "Sedang" -> FlowIntensity.MEDIUM
    "Deras" -> FlowIntensity.HEAVY
    else -> FlowIntensity.NONE
}

private fun mucusFromLabel(label: String): CervicalMucusType = when (label) {
    "Kering" -> CervicalMucusType.DRY
    "Krim" -> CervicalMucusType.CREAMY
    "Cair" -> CervicalMucusType.WATERY
    "Putih Telur" -> CervicalMucusType.EGG_WHITE
    else -> CervicalMucusType.NONE
}

// ===== Helper untuk membangun strip 7 hari dari data nyata (data layer) =====
private fun DailyLogEntity.toDayLog(latestCycle: CycleEntity?, fp: FertilePrediction?): DayLog {
    val cycleDay = latestCycle?.let { (ChronoUnit.DAYS.between(it.startDate, this.date) + 1).toInt() }?.coerceAtLeast(1) ?: 1
    val phaseType = cyclePhaseFor(cycleDay, this, fp)
    return DayLog(
        dayOfMonth = date.dayOfMonth,
        dayLabel = shortDayName(date),
        phaseName = phaseNameFor(phaseType, cycleDay),
        bbtString = basalBodyTempCelsius?.let { "%.2f °C".format(it) } ?: "-- °C",
        painVas = painVasScore,
        painDesc = painDescFor(painVasScore),
        mucus = mucusLabelFor(cervicalMucus),
        phaseType = phaseType,
        date = date,
        bbtValue = basalBodyTempCelsius
    )
}

private fun cyclePhaseFor(cycleDay: Int, log: DailyLogEntity, fp: FertilePrediction?): CyclePhaseType {
    if (log.flow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY)) return CyclePhaseType.MENSTRUATION
    if (fp != null) {
        val d = log.date
        if (d == fp.predictedOvulationDate) return CyclePhaseType.OVULATION
        if (!d.isBefore(fp.fertileWindowStart) && !d.isAfter(fp.fertileWindowEnd)) return CyclePhaseType.FERTILE
        if (d.isAfter(fp.fertileWindowEnd)) return CyclePhaseType.LUTEAL
    }
    return CyclePhaseType.FOLLICULAR
}

private fun phaseNameFor(pt: CyclePhaseType, cycleDay: Int): String = when (pt) {
    CyclePhaseType.MENSTRUATION -> if (cycleDay <= 1) "Haid" else "Haid Berlangsung"
    CyclePhaseType.FOLLICULAR -> "Fase Folikuler"
    CyclePhaseType.FERTILE -> "Jendela Subur"
    CyclePhaseType.OVULATION -> "Puncak Ovulasi"
    CyclePhaseType.LUTEAL -> "Fase Luteal"
}

private fun shortDayName(date: LocalDate): String = when (date.dayOfWeek) {
    DayOfWeek.MONDAY -> "Sen"
    DayOfWeek.TUESDAY -> "Sel"
    DayOfWeek.WEDNESDAY -> "Rab"
    DayOfWeek.THURSDAY -> "Kam"
    DayOfWeek.FRIDAY -> "Jum"
    DayOfWeek.SATURDAY -> "Sab"
    DayOfWeek.SUNDAY -> "Min"
}

private val idMonths = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")

private fun fullDayName(date: LocalDate): String = when (date.dayOfWeek) {
    DayOfWeek.MONDAY -> "Senin"
    DayOfWeek.TUESDAY -> "Selasa"
    DayOfWeek.WEDNESDAY -> "Rabu"
    DayOfWeek.THURSDAY -> "Kamis"
    DayOfWeek.FRIDAY -> "Jumat"
    DayOfWeek.SATURDAY -> "Sabtu"
    DayOfWeek.SUNDAY -> "Minggu"
}

private fun formatIdDate(date: LocalDate): String =
    "${fullDayName(date)}, ${date.dayOfMonth} ${idMonths[date.monthValue - 1]} ${date.year}"

private fun painDescFor(score: Int): String = when {
    score == 0 -> "Bebas Nyeri"
    score <= 3 -> "Nyeri Ringan"
    score <= 6 -> "Nyeri Sedang"
    score <= 8 -> "Nyeri Berat"
    else -> "Sangat Hebat"
}

private fun mucusLabelFor(m: CervicalMucusType): String = when (m) {
    CervicalMucusType.NONE -> "Tidak ada"
    CervicalMucusType.DRY -> "Kering"
    CervicalMucusType.STICKY -> "Lengket"
    CervicalMucusType.CREAMY -> "Krim"
    CervicalMucusType.WATERY -> "Cair"
    CervicalMucusType.EGG_WHITE -> "Putih Telur"
}

@Composable
fun CycleBottomNavBar(
    currentScreen: AppScreen,
    isDarkMode: Boolean,
    onScreenSelect: (AppScreen) -> Unit,
    onFabClick: () -> Unit
) {
    val navBg = if (isDarkMode) DarkCardSurface.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f)
    val borderCol = if (isDarkMode) DarkBorderColor else Slate100

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .background(navBg)
            .border(1.dp, borderCol)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Home,
                label = "Beranda",
                isSelected = currentScreen == AppScreen.DASHBOARD,
                onClick = { onScreenSelect(AppScreen.DASHBOARD) }
            )

            NavItem(
                icon = Icons.Default.DateRange,
                label = "Kalender",
                isSelected = currentScreen == AppScreen.CALENDAR,
                onClick = { onScreenSelect(AppScreen.CALENDAR) }
            )

            Spacer(modifier = Modifier.size(52.dp))

            NavItem(
                icon = Icons.Default.Description,
                label = "Laporan",
                isSelected = currentScreen == AppScreen.REPORT,
                onClick = { onScreenSelect(AppScreen.REPORT) }
            )

            NavItem(
                icon = Icons.Default.Settings,
                label = "Pengaturan",
                isSelected = currentScreen == AppScreen.SETTINGS,
                onClick = { onScreenSelect(AppScreen.SETTINGS) }
            )
        }

        // FAB mengambang sebagai overlay terpisah — tidak lagi terpotong garis nav
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-22).dp)
                .size(54.dp)
                .shadow(14.dp, RoundedCornerShape(18.dp), ambientColor = CoralDeep, spotColor = CoralDeep)
                .border(3.dp, Color.White, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(CoralPinkGradient)
                .clickable { onFabClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Isi Jurnal", tint = Color.White, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) CoralDeep else Slate400,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CoralDeep else Slate400
        )
    }
}