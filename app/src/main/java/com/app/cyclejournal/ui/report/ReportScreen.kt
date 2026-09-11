package com.app.cyclejournal.ui.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.ui.home.CycleFilter
import com.app.cyclejournal.ui.home.CycleViewModel
import com.app.cyclejournal.ui.settings.SettingsViewModel
import com.app.cyclejournal.ui.theme.PrimaryCoral
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter

// ── Local palette (consumer-friendly, no clinical branding) ──────────────────
private val PageBg       = Color(0xFFFFF7F9)
private val PinkButton   = Color(0xFFFF5A85)
private val SoftPinkBg   = Color(0xFFFDE8EC)
private val HotPinkText  = Color(0xFFFF2A66)
private val PinkSub      = Color(0xFFD81B60)
private val SoftCreamBg  = Color(0xFFFFF8E7)
private val AmberBorder  = Color(0xFFFFA000)
private val OrangeText   = Color(0xFFE65100)
private val OrangeSub    = Color(0xFFF57C00)
private val SelectedPink = Color(0xFFFF5A85)
private val ChipBg       = Color(0xFFF1F3F4)
private val TextPrimary  = Color(0xDE000000)
private val TextGray     = Color(0x99000000)
private val AlertBg      = Color(0xFFFFF3CD)
private val AlertBorder  = Color(0xFFFF9800)
private val AlertText    = Color(0xFF6D3A00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    cycleViewModel: CycleViewModel,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val stats by cycleViewModel.cycleStatsFlow.collectAsState()
    val completedCycles by cycleViewModel.completedCyclesFlow.collectAsState()
    val latestCycle by cycleViewModel.latestCycleFlow.collectAsState()
    val anomalies by cycleViewModel.anomaliesFlow.collectAsState()
    val activeFilter by cycleViewModel.activeFilter.collectAsState()

    // Summarize from last 6 completed cycles (matches brief spec)
    val last6 = completedCycles.take(6)
    val avgPeriod = if (last6.isEmpty()) 0
    else (last6.sumOf { it.periodDurationDays }.toDouble() / last6.size).toInt()
    val avgCycle = if (last6.isEmpty()) 0
    else (last6.sumOf { it.cycleLengthDays ?: 28 }.toDouble() / last6.size).toInt()

    // Total history to display: completed (newest first) + active if exists
    val totalCycleCount = completedCycles.size + (if (latestCycle?.endDate == null && latestCycle != null) 1 else 0)

    // Active cycle elapsed days
    val elapsedDays = latestCycle?.let {
        ChronoUnit.DAYS.between(it.startDate, LocalDate.now()).toInt() + 1
    } ?: 0

    // Year groups for section headers — newest first
    val cyclesByYear: Map<Int, List<com.app.cyclejournal.data.local.entity.CycleEntity>> =
        completedCycles.groupBy { it.startDate.year }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analisis Siklus",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->

        // ── Empty state ──────────────────────────────────────────────────────
        if (completedCycles.isEmpty() && latestCycle == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada riwayat siklus.\nCatat hari pertama haid untuk memulai.",
                    textAlign = TextAlign.Center,
                    color = TextGray,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(32.dp)
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── 1. Summary card ───────────────────────────────────────────────
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Ringkasan Siklus",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Berdasarkan ${last6.size} siklus terakhir",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Rata-rata haid card
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                value = "$avgPeriod Hari",
                                label = "Rata-rata haid",
                                bgColor = SoftPinkBg,
                                valueColor = HotPinkText,
                                labelColor = PinkSub
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(PinkButton),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.WaterDrop,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Rata-rata siklus card
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                value = "$avgCycle Hari",
                                label = "Rata-rata siklus",
                                bgColor = SoftCreamBg,
                                valueColor = OrangeText,
                                labelColor = OrangeSub
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .border(3.dp, AmberBorder, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            // ── 2. Pemberitahuan (anomalies, tanpa klaim medis) ──────────────
            if (anomalies.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Perhatian",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        anomalies.forEach { alert ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = AlertBg,
                                border = BorderStroke(1.dp, AlertBorder)
                            ) {
                                Text(
                                    text = "⚠ ${alert.type.description}",
                                    fontSize = 12.sp,
                                    color = AlertText,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                        Text(
                            "Catatan ini hanya untuk pemantauan pribadi. Konsultasikan dengan dokter jika ada kekhawatiran.",
                            fontSize = 11.sp,
                            color = TextGray,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // ── 3. Riwayat header + filter chips ─────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Riwayat",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "$totalCycleCount siklus tercatat",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CycleFilter.entries.forEach { filter ->
                        val isSelected = activeFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { cycleViewModel.setFilter(filter) },
                            label = {
                                Text(
                                    text = filter.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextPrimary
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SelectedPink,
                                containerColor = ChipBg
                            ),
                            border = null
                        )
                    }
                }
            }

            // ── 4. Siklus aktif (jika ada) ────────────────────────────────────
            if (latestCycle != null && latestCycle!!.endDate == null) {
                item {
                    Text(
                        "${LocalDate.now().year}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                item {
                    CycleHistoryRow(
                        cycle = latestCycle!!,
                        activeFilter = activeFilter,
                        elapsedDaysIfActive = elapsedDays
                    )
                }
            }

            // ── 5. Siklus selesai, dikelompokkan per tahun ────────────────────
            cyclesByYear.keys.sortedDescending().forEach { year ->
                val cyclesInYear = cyclesByYear[year] ?: return@forEach

                item(key = "year_$year") {
                    Text(
                        "$year",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(
                    items = cyclesInYear,
                    key = { it.id }
                ) { cycle ->
                    CycleHistoryRow(
                        cycle = cycle,
                        activeFilter = activeFilter
                    )
                }
            }

            // ── 6. Tombol ekspor ──────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { cycleViewModel.exportAndSharePdfReport(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PinkButton),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Unduh Rekap Siklus (PDF)",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = { settingsViewModel.exportAndShareCsv(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.TableChart,
                            contentDescription = null,
                            tint = TextGray
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Ekspor Data Mentah (.CSV)",
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        "Ingin ekspor tanpa iklan? Buka Lisensi Pro",
                        fontSize = 12.sp,
                        color = PrimaryCoral,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// ── Private helper composable ─────────────────────────────────────────────────

@Composable
private fun MetricCard(
    value: String,
    label: String,
    bgColor: Color,
    valueColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) { icon() }

        Spacer(Modifier.height(8.dp))

        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = labelColor
        )
    }
}
