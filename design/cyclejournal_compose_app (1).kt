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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.delay

// Exact Figma/Tailwind Tokens from HTML prototype
val Coral300 = Color(0xFFFFB2A1)
val Coral400 = Color(0xFFFF8A71)
val Coral500 = Color(0xFFFF6F61)
val Coral600 = Color(0xFFFF5E7D)
val Coral700 = Color(0xFFE64264)

val MedicalCyan = Color(0xFF06B6D4)
val MedicalTeal = Color(0xFF0D9488)
val MedicalRose = Color(0xFFF43F5E)
val MedicalAmber = Color(0xFFF59E0B)
val MedicalSlate = Color(0xFF0F172A)

val LightBackground = Color(0xFFFBFBFC)
val DarkBackground = Color(0xFF0B0F19)
val DarkCardBackground = Color(0xFF151D2E)
val DarkBorder = Color(0xFF243048)

val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

// Gradients
val CoralLinearGradient = Brush.linearGradient(
    colors = listOf(Coral400, Coral600),
    start = Offset(0f, 0f),
    end = Offset.Infinite
)

val SoftCoralGradient = Brush.linearGradient(
    colors = listOf(Coral400.copy(alpha = 0.12f), Coral600.copy(alpha = 0.12f))
)

val AmberBadgeGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF59E0B), Color(0xFFEA580C))
)

enum class AppScreen {
    SPLASH, DASHBOARD, CALENDAR, REPORT, SETTINGS
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
    val dotColor: Color
)

@Composable
fun CycleJournalApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var isDarkMode by remember { mutableStateOf(false) }
    var isDiscreetMode by remember { mutableStateOf(false) }
    var isPinModalOpen by remember { mutableStateOf(false) }
    var isLogModalOpen by remember { mutableStateOf(false) }
    var isProLicenseActive by remember { mutableStateOf(false) }
    var pinStatus by remember { mutableStateOf("Belum diatur (Opsional)") }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Predefined 7 days matching the HTML prototype exactly
    val weekDays = remember {
        listOf(
            DayStripItem(11, "Kam", "Selesai Haid", "36.30 °C", "Bebas Nyeri", "Flek Coklat", CyclePhase.MENSTRUATION, Color(0xFFFB7185)),
            DayStripItem(12, "Jum", "Fase Folikuler", "36.32 °C", "Bebas Nyeri", "Kering", CyclePhase.FOLLICULAR, Color(0xFFCBD5E1)),
            DayStripItem(13, "Sab", "Fase Pra-Subur", "36.38 °C", "Bebas Nyeri", "Krim", CyclePhase.FOLLICULAR, Color(0xFF67E8F9)),
            DayStripItem(14, "Min", "Jendela Subur", "36.50 °C", "Sedang (VAS 7)", "Putih Telur", CyclePhase.FERTILE, Coral500),
            DayStripItem(15, "Sen", "Masa Subur Aktif", "36.42 °C", "Bebas Nyeri", "Cair Basah", CyclePhase.FERTILE, Color(0xFF22D3EE)),
            DayStripItem(16, "Sel", "Peluang Konsepsi Tinggi", "36.45 °C", "Nyeri Ringan", "Putih Telur", CyclePhase.FERTILE, Color(0xFF22D3EE)),
            DayStripItem(17, "Rab", "Puncak Ovulasi", "36.48 °C", "Mittelschmerz", "Putih Telur", CyclePhase.OVULATION, MedicalTeal)
        )
    }

    var selectedDay by remember { mutableStateOf(weekDays[3]) } // 14 Min (Today)

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
        when (currentScreen) {
            AppScreen.SPLASH -> {
                SplashScreenView(
                    onEnterApp = { currentScreen = AppScreen.DASHBOARD }
                )
            }
            else -> {
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
                            onOpenFab = { isLogModalOpen = true }
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
                                onSelectDay = {
                                    selectedDay = it
                                    showToast("Menampilkan data ${it.dayOfMonth} Sep 2026")
                                },
                                onOpenCalendar = { currentScreen = AppScreen.CALENDAR },
                                onOpenLog = { isLogModalOpen = true }
                            )
                            AppScreen.CALENDAR -> CalendarScreenView(
                                isDarkMode = isDarkMode,
                                onOpenLog = { isLogModalOpen = true },
                                onToast = showToast
                            )
                            AppScreen.REPORT -> SpOgReportScreenView(
                                isDarkMode = isDarkMode,
                                isPro = isProLicenseActive,
                                onBuyPro = {
                                    isProLicenseActive = true
                                    showToast("Google Play Billing: Lisensi Pro Aktif Selamanya!")
                                },
                                onToast = showToast
                            )
                            AppScreen.SETTINGS -> SettingsScreenView(
                                isDarkMode = isDarkMode,
                                isDiscreet = isDiscreetMode,
                                isPro = isProLicenseActive,
                                pinStatus = pinStatus,
                                onToggleDiscreet = { isDiscreetMode = !isDiscreetMode },
                                onToggleDark = { isDarkMode = !isDarkMode },
                                onOpenPin = { isPinModalOpen = true },
                                onBuyPro = {
                                    isProLicenseActive = true
                                    showToast("Google Play Billing: Berhasil Upgrade ke Lifetime Pro!")
                                },
                                onNukeData = {
                                    showToast("Seluruh data lokal & cloud berhasil dibersihkan")
                                    currentScreen = AppScreen.SPLASH
                                },
                                onToast = showToast
                            )
                            else -> Unit
                        }
                    }
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
                onSave = {
                    pinStatus = "Aktif (PIN 4-Digit)"
                    isPinModalOpen = false
                    showToast("PIN Keamanan Berhasil Diaktifkan")
                }
            )
        }

        // Daily Log Bottom Sheet
        if (isLogModalOpen) {
            DailyLogBottomSheet(
                isDarkMode = isDarkMode,
                onDismiss = { isLogModalOpen = false },
                onSave = {
                    isLogModalOpen = false
                    showToast("Jurnal Hari Ini Berhasil Disimpan")
                }
            )
        }
    }
}

@Composable
fun SplashScreenView(onEnterApp: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF5F3), Color.White, Color(0xFFFFF0F3))
                )
            )
    ) {
        // Soft radial aura blobs replacing hard clipped circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Top-left aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Coral300.copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = size.width * 0.75f
                )
            )
            // Bottom-right aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Coral600.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(size.width, size.height),
                    radius = size.width * 0.85f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF1F2).copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, Color(0xFFFFE4E6))
                ) {
                    Text(
                        text = "FIGO STANDARD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Coral600,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }
                Text("v1.0", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Bold)
            }

            // Center Pearl Logo & Identity
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer ambient blur glow
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Coral500.copy(alpha = 0.5f), Color.Transparent)
                                )
                            )
                    )

                    // Coral shell
                    Box(
                        modifier = Modifier
                            .size(118.dp)
                            .scale(scale)
                            .shadow(20.dp, CircleShape, spotColor = Coral600)
                            .clip(CircleShape)
                            .background(CoralLinearGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        // Pearl core
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color.White, Color(0xFFFEE2E2), Color(0xFFCBD5E1))
                                    )
                                )
                                .border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cycle", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Slate900)
                    Text("Journal", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Coral600)
                }

                Text(
                    text = "PRIVASI PENUH • STANDAR DOKTER KANDUNGAN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 1.1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Frosted Glass Trust Capsule
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color.White),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(13.dp)
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
            }

            // Bottom CTA
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEnterApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = Coral600),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral500)
                ) {
                    Text("Masuk Aplikasi", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Text("Mulai Gratis • Tanpa Pendaftaran Akun", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Medium)
            }
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

@Composable
fun DashboardScreenView(
    isDarkMode: Boolean,
    isDiscreet: Boolean,
    weekDays: List<DayStripItem>,
    selectedDay: DayStripItem,
    onSelectDay: (DayStripItem) -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenLog: () -> Unit
) {
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorder else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
    ) {
        // 1. HERO CARD: DYNAMIC HORMONAL PHASE
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = Coral600.copy(alpha = 0.35f),
                        spotColor = Coral600.copy(alpha = 0.45f)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(CoralLinearGradient)
                    .padding(20.dp)
            ) {
                // Background Decorative Wave (White radial glow blob from HTML)
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                            center = Offset(size.width + 30f, size.height + 30f),
                            radius = size.width * 0.55f
                        ),
                        center = Offset(size.width + 30f, size.height + 30f),
                        radius = size.width * 0.55f
                    )
                }

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
                                text = if (isDiscreet) "FASE 02" else "JENDELA SUBUR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isDiscreet) "Periode Tengah" else "Fase Folikuler",
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
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDiscreet) "Pencatatan normal berlangsung" else "Ovulasi dalam 2 hari ke depan",
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
                                color = Color.Black.copy(alpha = 0.15f)
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
                                        text = "28 Hari (±1.5)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.15f)
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
                                        text = "Tinggi (85%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFEF08A) // Gold yellow from prototype
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Circular Progress: Day 14 of 28 (Exact 50% half-arc)
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
                            // Active 50% Progress Arc (Day 14/28 = 180 deg)
                            drawArc(
                                color = Color.White,
                                startAngle = -90f,
                                sweepAngle = 180f,
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
                                text = "14",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                lineHeight = 28.sp
                            )
                            Text(
                                text = "dari 28",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFFE4E6)
                            )
                        }
                    }
                }
            }
        }

        // 2. ERGONOMIC 7-DAY HORIZONTAL WEEK STRIP (Fixing single day bug)
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
                            Text("Minggu Ini • Sep 2026", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
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

                    // All 7 columns evenly distributed in Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        weekDays.forEach { item ->
                            val isSelected = item.dayOfMonth == selectedDay.dayOfMonth
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when {
                                            isSelected -> Slate900
                                            item.dayOfMonth == 17 -> Color(0xFFECFEFF)
                                            else -> if (isDarkMode) DarkBackground else Slate50
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else if (item.dayOfMonth == 17) 1.dp else 1.dp,
                                        color = if (isSelected) Coral400 else if (item.dayOfMonth == 17) Color(0xFFA5F3FC) else Slate200.copy(alpha = 0.5f),
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
                                    color = if (isSelected) Color(0xFFFCA5A5) else if (item.dayOfMonth == 17) MedicalTeal else Slate400
                                )
                                Text(
                                    text = "${item.dayOfMonth}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
        // 3. BBT BIPHASIC SPARKLINE TREND CARD (Fixing disappearing curve & adding Coverline label)
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
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Coral600, modifier = Modifier.size(16.dp))
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

                    // Coverline Indicator Label row aligned above the dashed line
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

                    // Solid Canvas Sparkline Curve with calibrated coordinates matching HTML SVG
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

                        // Data Points mapped across 7 days
                        val pts = listOf(
                            Offset(w * 0.05f, h * 0.72f), // Day 11 (36.30)
                            Offset(w * 0.20f, h * 0.68f), // Day 12 (36.32)
                            Offset(w * 0.36f, h * 0.60f), // Day 13 (36.38)
                            Offset(w * 0.52f, h * 0.64f), // Day 14 (36.50 Follicular/pre-shift)
                            Offset(w * 0.68f, h * 0.44f), // Day 15 (shift starts)
                            Offset(w * 0.84f, h * 0.26f), // Day 16
                            Offset(w * 0.95f, h * 0.18f)  // Day 17 (Peak / Sustained)
                        )

                        // Smooth bezier curve path
                        val curvePath = Path().apply {
                            moveTo(pts.first().x, pts.first().y)
                            for (i in 1 until pts.size) {
                                val prev = pts[i - 1]
                                val curr = pts[i]
                                val midX = (prev.x + curr.x) / 2f
                                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                        }

                        // Gradient fill under the curve
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

                        // Stroke the curve
                        drawPath(
                            path = curvePath,
                            color = Coral600,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw points
                        pts.forEachIndexed { idx, pt ->
                            val isSelectedPoint = idx == 3 // Today (Day 14)
                            val dotColor = if (isSelectedPoint) Slate900 else if (idx > 3) MedicalCyan else Coral600
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

        // 4. QUICK DAILY LOG SUMMARY CARD (Fixing VAS 0/10 SpOG alert bug)
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Coral500))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Catatan Hari Ini (${selectedDay.dayOfMonth} Sep)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
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

                    // Dynamic VAS Display - Red Alert only when VAS >= 7
                    val isPainAlert = selectedDay.pain.contains("7") || selectedDay.pain.contains("8") || selectedDay.pain.contains("Sedang") || selectedDay.pain.contains("Berat")
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPainAlert) Color(0xFFFFF1F2) else Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, if (isPainAlert) Color(0xFFFFE4E6) else Color(0xFFA7F3D0))
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
                                    color = if (isPainAlert) MedicalRose else Color(0xFF047857)
                                )
                                Text(
                                    text = if (isPainAlert) "7 / 10 • Nyeri Pelvis & Pinggang" else "0 / 10 • Bebas Nyeri",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPainAlert) Color(0xFF9F1239) else Color(0xFF065F46)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isPainAlert) MedicalRose else Color(0xFF10B981)
                            ) {
                                Text(
                                    text = if (isPainAlert) "SpOG Alert" else "Nyaman",
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

@Composable
fun CalendarScreenView(
    isDarkMode: Boolean,
    onOpenLog: () -> Unit,
    onToast: (String) -> Unit
) {
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorder else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500

    var selectedCalendarDay by remember { mutableStateOf(14) }

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
                        selectedCalendarDay = 14
                        onToast("Memeriksa data 14 September 2026")
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF1F2),
                    border = BorderStroke(1.dp, Color(0xFFFFE4E6))
                ) {
                    Text("Hari Ini", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Coral600, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }

        // Full 5-Week Calendar Card
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = textSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("September 2026", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textPrimary)
                            Text("Siklus #8 • Rata-rata 28 Hari", fontSize = 10.sp, color = textSecondary)
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

                    val days = (1..30).toList()
                    val weeks = days.chunked(7)
                    weeks.forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            week.forEach { d ->
                                val isHaid = d in 8..12
                                val isFertile = d in 17..21 && d != 20
                                val isPeak = d == 20
                                val isSelected = d == selectedCalendarDay

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
                                            selectedCalendarDay = d
                                            onToast("Memeriksa data $d September 2026")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$d",
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
                            }
                            if (week.size < 7) {
                                repeat(7 - week.size) {
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
                                Text("$selectedCalendarDay September 2026", fontSize = 13.sp, fontWeight = FontWeight.Black, color = textPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFCFFAFE)
                                ) {
                                    Text(
                                        text = if (selectedCalendarDay in 17..21) "Masa Subur" else if (selectedCalendarDay in 8..12) "Menstruasi" else "Fase Folikuler",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0E7490),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (selectedCalendarDay == 14) "Hari Ini • Hari ke-14 Siklus" else "Hari ke-$selectedCalendarDay Siklus",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }

                        Button(
                            onClick = onOpenLog,
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

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ParameterBox("Suhu Basal", "36.50 °C", Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox("Lendir Serviks", "Putih Telur", Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox("Skala Nyeri", "Sedang (VAS 7)", Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                    }
                }
            }
        }

        // Cycle Prediction Insight Card (Matching HTML Prototype)
        item {
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .shadow(2.dp, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Coral600, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Prediksi Haid Berikutnya", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("Sekitar 6 Oktober 2026 (±1 hari)", fontSize = 10.sp, color = textSecondary)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFFFE4E6))
                    ) {
                        Text(
                            text = "22 Hari Lagi",
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

@Composable
fun SpOgReportScreenView(
    isDarkMode: Boolean,
    isPro: Boolean,
    onBuyPro: () -> Unit,
    onToast: (String) -> Unit
) {
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorder else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900
    val textSecondary = if (isDarkMode) Slate400 else Slate500

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
                            Text("ID Anonim: px-7f9a2b1c4e0d", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = textSecondary)
                        }
                        Text("14 Sep 2026", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ParameterBox("Rata-rata", "28.0 Hari", Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox("Variasi Siklus", "±1.5 Hari", Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox("Lama Haid", "5.0 Hari", Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
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

                    // Anomaly Alert Box
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
                                Text("Perhatian: Nyeri Haid Cukup Intens", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedicalRose)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tercatat skala nyeri 7/10 disertai konsumsi obat pereda nyeri. Riwayat ini siap dibahas saat konsultasi dengan dokter kandungan Anda.",
                                fontSize = 10.sp,
                                color = Color(0xFF9F1239),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Clinical History Table (FIGO Standard 3-Cycle Log)
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
                            Divider(color = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.5f))
                            listOf(
                                listOf("01 Jan 26", "26 Hari", "5 Hari", "15 Jan"),
                                listOf("27 Jan 26", "28 Hari", "5 Hari", "11 Feb"),
                                listOf("24 Feb 26", "30 Hari", "5 Hari", "13 Mar")
                            ).forEach { row ->
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
                    onClick = { onToast(if (isPro) "Mengunduh PDF Medis Instan" else "Menonton 1 Iklan Singkat... PDF Medis Siap!") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral500)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPro) "Unduh PDF Medis (Pro)" else "Unduh PDF Medis (Tonton 1 Iklan)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onToast("Mengekspor Berkas CSV Mentah") },
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

@Composable
fun SettingsScreenView(
    isDarkMode: Boolean,
    isDiscreet: Boolean,
    isPro: Boolean,
    pinStatus: String,
    onToggleDiscreet: () -> Unit,
    onToggleDark: () -> Unit,
    onOpenPin: () -> Unit,
    onBuyPro: () -> Unit,
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

                    Divider(color = borderCol)

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

                    Divider(color = borderCol)

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
                            modifier = Modifier.clickable { onToast("Recovery Key Disalin ke Clipboard") }
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDarkMode) DarkBackground else Slate50,
                        border = BorderStroke(1.dp, if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "px-7f9a2b1c4e0d",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = textSecondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Text("Gunakan kode rahasia ini jika Anda berganti perangkat baru.", fontSize = 10.sp, color = textSecondary)

                    Divider(color = borderCol)

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
                            onClick = { onToast("Enkripsi & Cadangan Cloud Berhasil") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Coral600, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cadangkan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onToast("Data Arsip Berhasil Dipulihkan") },
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
                    Divider(color = borderCol)
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
                    Divider(color = borderCol)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLogBottomSheet(
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var vasScore by remember { mutableStateOf(7f) }
    var selectedFlow by remember { mutableStateOf("Sedang") }
    var selectedMucus by remember { mutableStateOf("Putih Telur") }
    var hasTakenAnalgesic by remember { mutableStateOf(true) }
    var bbtInputText by remember { mutableStateOf("36.50") }

    val symptoms = listOf("Kram Pelvis", "Sakit Pinggang", "Payudara Sensitif", "Sakit Kepala", "Perut Kembung", "Mood Sensitif")
    val selectedSymptoms = remember { mutableStateListOf("Kram Pelvis", "Sakit Pinggang") }

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
                    Text("Senin, 14 September 2026", fontSize = 11.sp, color = Slate400)
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
                onClick = onSave,
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

@Composable
fun AppBottomNavigation(
    currentScreen: AppScreen,
    isDarkMode: Boolean,
    onSelect: (AppScreen) -> Unit,
    onOpenFab: () -> Unit
) {
    val navBg = if (isDarkMode) DarkCardBackground.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.96f)
    val borderCol = if (isDarkMode) DarkBorder else Slate200.copy(alpha = 0.6f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = navBg,
        border = BorderStroke(1.dp, borderCol),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavButton(Icons.Default.Home, "Beranda", currentScreen == AppScreen.DASHBOARD) { onSelect(AppScreen.DASHBOARD) }
            NavButton(Icons.Default.DateRange, "Kalender", currentScreen == AppScreen.CALENDAR) { onSelect(AppScreen.CALENDAR) }

            // Floating Central Add Action
            Box(
                modifier = Modifier
                    .offset(y = (-14).dp)
                    .size(50.dp)
                    .shadow(12.dp, CircleShape, spotColor = Coral600)
                    .clip(CircleShape)
                    .background(CoralLinearGradient)
                    .clickable { onOpenFab() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Catat Harian", tint = Color.White, modifier = Modifier.size(28.dp))
            }

            NavButton(Icons.Default.Description, "Laporan", currentScreen == AppScreen.REPORT) { onSelect(AppScreen.REPORT) }
            NavButton(Icons.Default.Settings, "Pengaturan", currentScreen == AppScreen.SETTINGS) { onSelect(AppScreen.SETTINGS) }
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
    onSave: () -> Unit
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
                                        Icon(Icons.Default.Backspace, contentDescription = null, tint = textPrimary, modifier = Modifier.size(16.dp))
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
                    onClick = onSave,
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