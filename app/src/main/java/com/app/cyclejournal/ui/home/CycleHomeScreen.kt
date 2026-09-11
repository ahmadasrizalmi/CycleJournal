package com.app.cyclejournal.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.ui.components.CycleCalendarView
import com.app.cyclejournal.ui.components.DailyLogInputSheet
import com.app.cyclejournal.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleHomeScreen(
    viewModel: CycleViewModel,
    onNavigateToReport: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var isBottomSheetOpen by remember { mutableStateOf(false) }

    val latestCycle by viewModel.latestCycleFlow.collectAsState()
    val periodDates by viewModel.periodDatesFlow.collectAsState()
    val prediction by viewModel.fertilePredictionFlow.collectAsState()
    val selectedDayLog by viewModel.getLogForDate(selectedDate).collectAsState(initial = null)

    val currentCycleDay = viewModel.getCurrentCycleDay(latestCycle?.startDate)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_pdf_header),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                actions = {
                    // Actions moved to bottom navigation bar or top quick actions
                    IconButton(onClick = onNavigateToReport) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = "Analisis Siklus", tint = PrimaryPink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        bottomBar = {
            Surface(
                color = BackgroundWhite,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tab 1: Beranda
                    IconButton(onClick = {}) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.CalendarMonth, contentDescription = "Beranda", tint = PrimaryPink, modifier = Modifier.size(22.dp))
                            Text("Beranda", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryPink)
                        }
                    }

                    // Center Elevated FAB (Catat Gejala)
                    Box(
                        modifier = Modifier
                            .offset(y = (-12).dp)
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(CoralPinkGradient)
                            .clickable { isBottomSheetOpen = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.EditCalendar,
                            contentDescription = "Catat Gejala",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Tab 2: Laporan SpOG
                    IconButton(onClick = onNavigateToReport) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.PictureAsPdf, contentDescription = "Laporan", tint = TextSecondary, modifier = Modifier.size(22.dp))
                            Text("Laporan", fontSize = 10.sp, color = TextSecondary)
                        }
                    }

                    // Tab 3: Pengaturan
                    IconButton(onClick = onNavigateToSettings) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Pengaturan", tint = TextSecondary, modifier = Modifier.size(22.dp))
                            Text("Pengaturan", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            }
        },
        containerColor = BackgroundWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Status Card (Coral-to-Pink Gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CoralPinkGradient)
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.cycle_day_format, currentCycleDay),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Phase Name & Countdown
                    val isTodayPeriod = periodDates.contains(LocalDate.now())
                    val phaseName = when {
                        isTodayPeriod -> stringResource(R.string.phase_menstruation)
                        prediction != null && !LocalDate.now().isBefore(prediction!!.fertileWindowStart) && !LocalDate.now().isAfter(prediction!!.fertileWindowEnd) -> stringResource(R.string.phase_fertile_window)
                        else -> stringResource(R.string.phase_follicular)
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            text = phaseName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    if (prediction != null) {
                        val daysToOvulation = ChronoUnit.DAYS.between(LocalDate.now(), prediction!!.predictedOvulationDate)
                        val subtitle = if (daysToOvulation > 0) {
                            stringResource(R.string.ovulation_countdown_format, daysToOvulation)
                        } else if (daysToOvulation == 0L) {
                            stringResource(R.string.ovulation_today)
                        } else {
                            val daysToPeriod = ChronoUnit.DAYS.between(LocalDate.now(), prediction!!.predictedNextPeriodDate)
                            stringResource(R.string.next_period_countdown_format, maxOf(1L, daysToPeriod))
                        }
                        Text(
                            text = subtitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Monthly Calendar Grid
            CycleCalendarView(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                menstruationDates = periodDates,
                prediction = prediction,
                onDateSelected = { date ->
                    selectedDate = date
                    isBottomSheetOpen = true
                },
                onMonthChanged = { month -> currentMonth = month }
            )
        }
    }

    // Modal Bottom Sheet for Daily Symptom Logging
    if (isBottomSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isBottomSheetOpen = false },
            containerColor = BackgroundWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            DailyLogInputSheet(
                selectedDate = selectedDate,
                initialLog = selectedDayLog,
                onSaveLog = { updatedLog ->
                    viewModel.saveDailyLog(updatedLog)
                    isBottomSheetOpen = false
                },
                onDismiss = { isBottomSheetOpen = false }
            )
        }
    }
}
