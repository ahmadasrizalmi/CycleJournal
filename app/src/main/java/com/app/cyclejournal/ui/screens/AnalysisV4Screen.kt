package com.app.cyclejournal.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.domain.model.AnomalyAlert
import com.app.cyclejournal.domain.model.CycleStats
import com.app.cyclejournal.domain.model.FertilePrediction
import com.app.cyclejournal.ui.components.CycleTimelineRow
import com.app.cyclejournal.ui.components.HairLine
import com.app.cyclejournal.ui.components.MiniBarChart
import com.app.cyclejournal.ui.components.PillChip
import com.app.cyclejournal.ui.components.RingProgress
import com.app.cyclejournal.ui.components.SoftCard
import com.app.cyclejournal.ui.components.SettingsListRow
import com.app.cyclejournal.ui.theme.AlertBrown
import com.app.cyclejournal.ui.theme.AlertBrownSoft
import com.app.cyclejournal.ui.theme.AlertTint
import com.app.cyclejournal.ui.theme.AppType
import com.app.cyclejournal.ui.theme.BrandEnd
import com.app.cyclejournal.ui.theme.BrandGradient
import com.app.cyclejournal.ui.theme.BrandTint
import com.app.cyclejournal.ui.theme.Dimens
import com.app.cyclejournal.ui.theme.HeroBottom
import com.app.cyclejournal.ui.theme.HeroStroke
import com.app.cyclejournal.ui.theme.HeroTop
import com.app.cyclejournal.ui.theme.Ink
import com.app.cyclejournal.ui.theme.Ink2
import com.app.cyclejournal.ui.theme.Ink3
import com.app.cyclejournal.ui.theme.Lavender
import com.app.cyclejournal.ui.theme.LavenderInk
import com.app.cyclejournal.ui.theme.Line
import com.app.cyclejournal.ui.theme.OkGreen
import com.app.cyclejournal.ui.theme.OkTint
import com.app.cyclejournal.ui.theme.OnBrand
import com.app.cyclejournal.ui.theme.Paper
import com.app.cyclejournal.ui.theme.RoseMuted
import com.app.cyclejournal.ui.theme.ShadowBrandSoft
import com.app.cyclejournal.ui.theme.Teal
import com.app.cyclejournal.ui.theme.TealGradient
import com.app.cyclejournal.ui.theme.TealMid
import com.app.cyclejournal.ui.theme.TipsInk
import com.app.cyclejournal.ui.theme.TrackSoft
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

private const val LUTEAL_PHASE_DAYS = 14
private const val MAX_CHART_CYCLES = 6
private const val MAX_TIMELINE_CYCLES = 3
private const val MAX_LOG_ROWS = 5
private const val SEVERE_PAIN_THRESHOLD = 7
private val SEVERE_PAIN_WINDOW_DAYS = 30L
private val PageTitleStyle = TextStyle(fontFamily = AppType.Heading, fontSize = 20.sp, fontWeight = FontWeight.Bold)
private val SummaryValueStyle = TextStyle(fontFamily = AppType.Heading, fontSize = 28.sp, fontWeight = FontWeight.Bold)
private val EyebrowStyle = TextStyle(fontFamily = AppType.Ui, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)

/**
 * Analysis — CycleJournal v4 design: `Analisis · ID v5 (ringkasan klinis)` and
 * `Analisis · ID v6 (timeline & catatan)`.
 *
 * The two design frames are one tab with two views; the segmented pill in the
 * summary header is what switches between them (v6 has no control of its own, so
 * without it the timeline view would be unreachable).
 */
@Composable
fun AnalysisV4Screen(
    isPro: Boolean,
    cycleStats: CycleStats?,
    completedCycles: List<CycleEntity>,
    anomalies: List<AnomalyAlert>,
    allLogs: List<DailyLogEntity>,
    latestCycle: CycleEntity?,
    fertilePrediction: FertilePrediction?,
    onSharePdf: (() -> Unit)?,
    onExportCsv: (() -> Unit)?,
    onBuyPro: () -> Unit,
    onNavigateToCalendar: (LocalDate) -> Unit,
    onToast: (String) -> Unit
) {
    var showTimeline by remember { mutableStateOf(false) }
    val summaryLabel = stringResource(R.string.v4_an_pill_summary)
    val timelineLabel = stringResource(R.string.v4_an_title_timeline)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenPadding,
            end = Dimens.ScreenPadding,
            top = 14.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionGap)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(if (showTimeline) R.string.v4_an_title_timeline else R.string.v4_an_title_summary),
                    style = PageTitleStyle,
                    color = Ink,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PillChip(
                        text = summaryLabel,
                        selected = !showTimeline,
                        onClick = { showTimeline = false },
                        height = 28.dp,
                        fontSize = 11
                    )
                    PillChip(
                        text = timelineLabel,
                        selected = showTimeline,
                        onClick = { showTimeline = true },
                        height = 28.dp,
                        fontSize = 11
                    )
                }
            }
        }

        if (showTimeline) {
            timelineItems(
                completedCycles = completedCycles,
                latestCycle = latestCycle,
                fertilePrediction = fertilePrediction,
                cycleStats = cycleStats,
                allLogs = allLogs,
                onNavigateToCalendar = onNavigateToCalendar,
                onToast = onToast
            )
        } else {
            summaryItems(
                isPro = isPro,
                cycleStats = cycleStats,
                completedCycles = completedCycles,
                anomalies = anomalies,
                allLogs = allLogs,
                latestCycle = latestCycle,
                fertilePrediction = fertilePrediction,
                onSharePdf = onSharePdf,
                onExportCsv = onExportCsv,
                onBuyPro = onBuyPro,
                onToast = onToast
            )
        }
    }
}

// --------------------------------------------------------------- summary ---

private fun androidx.compose.foundation.lazy.LazyListScope.summaryItems(
    isPro: Boolean,
    cycleStats: CycleStats?,
    completedCycles: List<CycleEntity>,
    anomalies: List<AnomalyAlert>,
    allLogs: List<DailyLogEntity>,
    latestCycle: CycleEntity?,
    fertilePrediction: FertilePrediction?,
    onSharePdf: (() -> Unit)?,
    onExportCsv: (() -> Unit)?,
    onBuyPro: () -> Unit,
    onToast: (String) -> Unit
) {
    item {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.CardRadius))
                .border(1.dp, Line, RoundedCornerShape(Dimens.CardRadius))
                .background(Paper)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.v4_an_summary_header), style = EyebrowStyle, color = Ink3)
                Spacer(Modifier.weight(1f))
                Text(
                    text = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())) },
                    style = EyebrowStyle,
                    color = Ink2
                )
            }

            // Clinical statistics need two completed cycles. Until then the ongoing cycle plus the
            // running prediction still describe where the user is today, so the summary is shown in
            // estimate mode instead of an empty state.
            val estimate = estimateCycleLength(latestCycle, fertilePrediction)
            val average = cycleStats?.averageLength?.roundToInt() ?: estimate
            if (average != null && latestCycle != null) {
                AverageCycleCard(
                    average = average,
                    isRegular = cycleStats?.let { it.standardDeviation <= 2.0 },
                    latestCycle = latestCycle
                )
                if (completedCycles.isNotEmpty()) {
                    CycleLengthChartCard(completedCycles = completedCycles)
                }
                PhaseBreakdownCard(
                    average = average,
                    periodDays = (cycleStats?.averagePeriodDuration?.roundToInt()
                        ?: latestCycle.periodDurationDays).coerceAtLeast(0),
                    fertileDays = fertilePrediction?.let {
                        (ChronoUnit.DAYS.between(it.fertileWindowStart, it.fertileWindowEnd) + 1).toInt().coerceAtLeast(0)
                    } ?: 0
                )
                SeverePainAlert(anomalies = anomalies, allLogs = allLogs)
            } else {
                EmptySummaryCard()
            }
        }
    }

    item {
        ReportCard(
            isPro = isPro,
            onSharePdf = onSharePdf,
            onExportCsv = onExportCsv,
            onBuyPro = onBuyPro,
            onToast = onToast
        )
    }
}

@Composable
private fun EmptySummaryCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.CardRadiusLg))
            .background(Brush.verticalGradient(listOf(HeroTop, HeroBottom)))
            .border(1.dp, HeroStroke, RoundedCornerShape(Dimens.CardRadiusLg))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.v4_an_no_data),
            style = AppType.cardTitle.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
            color = Ink
        )
        Text(stringResource(R.string.v4_an_no_data_sub), style = AppType.body.copy(fontSize = 12.sp), color = RoseMuted)
    }
}

@Composable
private fun AverageCycleCard(
    average: Int,
    isRegular: Boolean?,
    latestCycle: CycleEntity
) {
    val dayInCycle = (ChronoUnit.DAYS.between(latestCycle.startDate, LocalDate.now()) + 1)
        .toInt()
        .coerceAtLeast(1)
    val progress = if (average > 0) (dayInCycle.toFloat() / average).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.CardRadiusLg))
            .background(Brush.verticalGradient(listOf(HeroTop, HeroBottom)))
            .border(1.dp, HeroStroke, RoundedCornerShape(Dimens.CardRadiusLg))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.v4_an_avg_eyebrow),
                        style = TextStyle(fontFamily = AppType.Ui, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
                        color = BrandEnd
                    )
                    Spacer(Modifier.weight(1f))
                    // FIGO-style regularity: a standard deviation within 2 days reads as regular.
                    // Unknown (estimate mode) gets its own badge instead of pretending.
                    val badgeRes = when (isRegular) {
                        null -> R.string.v4_an_badge_estimate
                        true -> R.string.v4_an_badge_regular
                        false -> R.string.v4_an_badge_irregular
                    }
                    val badgeFill = when (isRegular) {
                        null -> Lavender
                        true -> OkTint
                        false -> AlertTint
                    }
                    val badgeInk = when (isRegular) {
                        null -> LavenderInk
                        true -> OkGreen
                        false -> AlertBrown
                    }
                    Box(
                        modifier = Modifier
                            .height(22.dp)
                            .clip(CircleShape)
                            .background(badgeFill)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(badgeRes),
                            style = AppType.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = badgeInk
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.v4_an_days_count, average),
                    style = SummaryValueStyle,
                    color = Ink
                )
                Text(
                    text = if (isRegular == null) {
                        stringResource(R.string.v4_an_day_of_estimate, dayInCycle, average)
                    } else {
                        stringResource(R.string.v4_an_day_of, dayInCycle, average)
                    },
                    style = AppType.body.copy(fontSize = 12.sp),
                    color = RoseMuted
                )
            }
            RingProgress(
                progress = progress,
                size = 68.dp,
                trackColor = Color(0xFFFFE1E5),
                label = "${(progress * 100).roundToInt()}%"
            )
        }
    }
}

@Composable
private fun CycleLengthChartCard(completedCycles: List<CycleEntity>) {
    val cycles = completedCycles.sortedBy { it.startDate }.takeLast(MAX_CHART_CYCLES)
    val monthFormat = remember { DateTimeFormatter.ofPattern("MMM", Locale.getDefault()) }
    val values = cycles.map { (it.cycleLengthDays ?: 0).toFloat() }
    val labels = cycles.map { it.startDate.format(monthFormat) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.CardRadius))
            .border(1.dp, Line, RoundedCornerShape(Dimens.CardRadius))
            .background(Paper)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.v4_an_chart_title),
                style = AppType.cardTitle.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                color = Ink
            )
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.v4_an_chart_sub), style = AppType.caption, color = Ink3)
        }
        if (values.isEmpty()) {
            Text(stringResource(R.string.v4_an_no_data), style = AppType.body.copy(fontSize = 12.sp), color = Ink3)
        } else {
            MiniBarChart(values = values, labels = labels, barHeight = 62.dp)
        }
    }
}

@Composable
private fun PhaseBreakdownCard(
    average: Int,
    periodDays: Int,
    fertileDays: Int
) {
    val safeAverage = average.coerceAtLeast(1)
    val lutealDays = (safeAverage - periodDays - fertileDays).coerceAtLeast(0)

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PhaseDonut(
            periodFraction = periodDays.toFloat() / safeAverage,
            fertileFraction = fertileDays.toFloat() / safeAverage
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            PhaseLegendRow(BrandEnd, stringResource(R.string.v4_an_phase_period), periodDays)
            PhaseLegendRow(Teal, stringResource(R.string.v4_an_phase_fertile), fertileDays)
            PhaseLegendRow(TrackSoft, stringResource(R.string.v4_an_phase_luteal), lutealDays)
        }
    }
}

@Composable
private fun PhaseLegendRow(color: Color, label: String, days: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, style = AppType.caption, color = Ink2)
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.v4_an_days_count, days),
            style = AppType.caption.copy(fontWeight = FontWeight.Bold),
            color = Ink
        )
    }
}

/** Three-arc donut: period (brand), fertile window (teal), luteal (track). */
@Composable
private fun PhaseDonut(periodFraction: Float, fertileFraction: Float) {
    val periodColor = BrandEnd
    val fertileColor = Teal
    val lutealColor = TrackSoft
    Canvas(modifier = Modifier.size(64.dp)) {
        val stroke = 10.dp.toPx()
        val inset = stroke / 2f
        val arcSize = Size(size.width - stroke, size.height - stroke)
        var start = -90f
        fun sweep(fraction: Float) = (360f * fraction.coerceIn(0f, 1f))
        val periodSweep = sweep(periodFraction)
        val fertileSweep = sweep(fertileFraction)
        val lutealSweep = (360f - periodSweep - fertileSweep).coerceAtLeast(0f)
        drawArc(
            color = periodColor,
            startAngle = start,
            sweepAngle = periodSweep,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        start += periodSweep
        drawArc(
            color = fertileColor,
            startAngle = start,
            sweepAngle = fertileSweep,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        start += fertileSweep
        drawArc(
            color = lutealColor,
            startAngle = start,
            sweepAngle = lutealSweep,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
    }
}

@Composable
private fun SeverePainAlert(anomalies: List<AnomalyAlert>, allLogs: List<DailyLogEntity>) {
    val resources = androidx.compose.ui.platform.LocalContext.current.resources
    val cutoff = LocalDate.now().minusDays(SEVERE_PAIN_WINDOW_DAYS)
    val worst = allLogs
        .filter { !it.date.isBefore(cutoff) && it.painVasScore >= SEVERE_PAIN_THRESHOLD }
        .maxByOrNull { it.painVasScore }
    val detail = when {
        worst != null -> stringResource(
            R.string.v4_an_warning_body,
            worst.painVasScore,
            worst.date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
        )
        anomalies.isNotEmpty() -> anomalies.first().localizedDetail(resources)
        else -> null
    } ?: return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AlertTint)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = AlertBrown, modifier = Modifier.size(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.v4_an_warning_title),
                style = AppType.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                color = AlertBrown
            )
            Text(text = detail, style = AppType.caption, color = AlertBrownSoft)
        }
    }
}

@Composable
private fun ReportCard(
    isPro: Boolean,
    onSharePdf: (() -> Unit)?,
    onExportCsv: (() -> Unit)?,
    onBuyPro: () -> Unit,
    onToast: (String) -> Unit
) {
    val pdfUnavailable = stringResource(R.string.report_export_preparing_summary)
    val csvUnavailable = stringResource(R.string.app_export_exporting_raw_csv)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.CardRadius))
            .border(1.dp, Line, RoundedCornerShape(Dimens.CardRadius))
            .background(Paper)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.v4_an_report_header), style = EyebrowStyle, color = Ink3)
                Text(stringResource(R.string.v4_an_report_sub), style = AppType.micro, color = Ink3)
            }
            ReportPill(
                label = "PDF",
                brush = BrandGradient,
                shadowColor = ShadowBrandSoft,
                onClick = { onSharePdf?.invoke() ?: onToast(pdfUnavailable) }
            )
            ReportPill(
                label = "CSV",
                brush = TealGradient,
                shadowColor = Color(0x3D0E7C7B),
                onClick = { onExportCsv?.invoke() ?: onToast(csvUnavailable) }
            )
        }
        if (!isPro) {
            HairLine()
            SettingsListRow(
                title = stringResource(R.string.v4_profile_go_pro),
                showChevron = true,
                onClick = onBuyPro
            )
        }
    }
}

@Composable
private fun ReportPill(
    label: String,
    brush: Brush,
    shadowColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(42.dp)
            .shadow(14.dp, CircleShape, ambientColor = shadowColor, spotColor = shadowColor)
            .clip(CircleShape)
            .background(brush)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Rounded.Download, contentDescription = null, tint = OnBrand, modifier = Modifier.size(15.dp))
        Text(label, style = AppType.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = OnBrand)
    }
}

// -------------------------------------------------------------- timeline ---

private enum class CycleFilter { ALL, PERIOD, OVULATION, FERTILE }

private fun androidx.compose.foundation.lazy.LazyListScope.timelineItems(
    completedCycles: List<CycleEntity>,
    latestCycle: CycleEntity?,
    fertilePrediction: FertilePrediction?,
    cycleStats: CycleStats?,
    allLogs: List<DailyLogEntity>,
    onNavigateToCalendar: (LocalDate) -> Unit,
    onToast: (String) -> Unit
) {
    val periodLength = (cycleStats?.averagePeriodDuration?.roundToInt() ?: 5).coerceAtLeast(1)
    val listedCycles = (completedCycles + listOfNotNull(latestCycle))
        .distinctBy { it.id }
        .sortedByDescending { it.startDate }
        .take(MAX_TIMELINE_CYCLES)

    item {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.CardRadius))
                .border(1.dp, Line, RoundedCornerShape(Dimens.CardRadius))
                .background(Paper)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            var filter by remember { mutableStateOf(CycleFilter.ALL) }
            val filtered = listedCycles.filter { cycle ->
                when (filter) {
                    CycleFilter.ALL -> true
                    CycleFilter.PERIOD -> cycle.periodDurationDays > 0
                    CycleFilter.OVULATION -> cycle.confirmedOvulationDate != null
                    CycleFilter.FERTILE -> cycle.cycleLengthDays != null
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                CycleFilter.entries.forEach { option ->
                    PillChip(
                        text = stringResource(
                            when (option) {
                                CycleFilter.ALL -> R.string.v4_an_filter_all
                                CycleFilter.PERIOD -> R.string.v4_an_filter_period
                                CycleFilter.OVULATION -> R.string.v4_an_filter_ovulation
                                CycleFilter.FERTILE -> R.string.v4_an_filter_fertile
                            }
                        ),
                        selected = filter == option,
                        onClick = { filter = option },
                        modifier = Modifier.weight(1f),
                        height = 34.dp,
                        fontSize = 12
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                TimelineLegendDot(BrandEnd, stringResource(R.string.v4_an_filter_period))
                TimelineLegendDot(TealMid, stringResource(R.string.v4_an_filter_fertile))
                TimelineLegendDot(Teal, stringResource(R.string.v4_an_filter_ovulation))
                TimelineLegendDot(Ink, stringResource(R.string.v4_cal_today))
            }
            if (filtered.isEmpty()) {
                Text(stringResource(R.string.v4_an_no_data), style = AppType.body.copy(fontSize = 12.sp), color = Ink3)
            } else {
                filtered.forEach { cycle ->
                    val length = cycle.cycleLengthDays ?: cycleStats?.averageLength?.roundToInt() ?: 28
                    val ovulationDay = cycle.confirmedOvulationDate?.let {
                        (ChronoUnit.DAYS.between(cycle.startDate, it) + 1).toInt()
                    } ?: (length - LUTEAL_PHASE_DAYS)
                    val isOngoing = cycle.endDate == null
                    val rangeLabel = if (cycle.endDate == null) {
                        stringResource(R.string.v4_an_ongoing)
                    } else {
                        stringResource(
                            R.string.v4_an_cycle_range,
                            cycle.startDate.format(shortDateFormat()),
                            cycle.endDate.format(shortDateFormat())
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CycleTimelineRow(
                            label = rangeLabel,
                            rightLabel = if (isOngoing) {
                                stringResource(
                                    R.string.v4_an_day_n_ongoing,
                                    (ChronoUnit.DAYS.between(cycle.startDate, LocalDate.now()) + 1).toInt().coerceAtLeast(1)
                                )
                            } else {
                                stringResource(R.string.v4_an_days_count, length)
                            },
                            cycleLengthDays = length,
                            periodDays = cycle.periodDurationDays.takeIf { it > 0 } ?: periodLength,
                            fertileStartDay = (ovulationDay - 5).coerceAtLeast(1),
                            fertileEndDay = (ovulationDay + 1).coerceAtMost(length),
                            ovulationDay = ovulationDay.coerceIn(1, length),
                            todayDay = if (isOngoing) {
                                (ChronoUnit.DAYS.between(cycle.startDate, LocalDate.now()) + 1).toInt().coerceIn(1, length)
                            } else null,
                            onLabelClick = { onNavigateToCalendar(cycle.startDate) }
                        )
                    }
                }
            }
        }
    }

    item {
        val recentLogs = allLogs.sortedByDescending { it.date }.take(MAX_LOG_ROWS)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.CardRadius))
                .border(1.dp, Line, RoundedCornerShape(Dimens.CardRadius))
                .background(Paper)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val seeAll = stringResource(R.string.v4_an_see_all)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.v4_an_logs_header), style = EyebrowStyle, color = Ink3)
                Spacer(Modifier.weight(1f))
                Text(
                    text = seeAll,
                    style = AppType.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = BrandEnd,
                    modifier = Modifier.clickable { onToast(seeAll) }
                )
            }
            LogTableHeader()
            if (recentLogs.isEmpty()) {
                Text(stringResource(R.string.v4_an_no_data), style = AppType.body.copy(fontSize = 12.sp), color = Ink3)
            } else {
                recentLogs.forEach { log ->
                    LogTableRow(log = log, onClick = { onNavigateToCalendar(log.date) })
                }
            }
        }
    }

    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BrandTint)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Rounded.Thermostat, contentDescription = null, tint = BrandEnd, modifier = Modifier.size(18.dp))
            Text(
                text = stringResource(R.string.v4_an_tip),
                style = AppType.micro.copy(fontWeight = FontWeight.Medium),
                color = TipsInk
            )
        }
    }
}

@Composable
private fun TimelineLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, style = AppType.caption.copy(fontWeight = FontWeight.SemiBold), color = Ink2)
    }
}

@Composable
private fun LogTableHeader() {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TableCell(stringResource(R.string.v4_an_col_date), weight = 1f, bold = true, align = TextAlign.Start)
        TableCell(stringResource(R.string.v4_an_col_flow), width = 54.dp, bold = true)
        TableCell(stringResource(R.string.v4_an_col_temp), width = 52.dp, bold = true)
        TableCell(stringResource(R.string.v4_an_col_pain), width = 44.dp, bold = true)
    }
}

@Composable
private fun LogTableRow(log: DailyLogEntity, onClick: () -> Unit) {
    val flowLabel = stringResource(
        when (log.flow) {
            com.app.cyclejournal.data.local.entity.FlowIntensity.NONE -> R.string.v4_log_flow_none
            com.app.cyclejournal.data.local.entity.FlowIntensity.SPOTTING -> R.string.v4_log_flow_spotting
            com.app.cyclejournal.data.local.entity.FlowIntensity.LIGHT -> R.string.v4_log_flow_light
            com.app.cyclejournal.data.local.entity.FlowIntensity.MEDIUM -> R.string.v4_log_flow_medium
            com.app.cyclejournal.data.local.entity.FlowIntensity.HEAVY -> R.string.v4_log_flow_heavy
        }
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(log.date.format(shortDateFormat()), weight = 1f, color = Ink, semibold = true, align = TextAlign.Start)
        TableCell(flowLabel, width = 54.dp, color = BrandEnd, semibold = true)
        TableCell(
            log.basalBodyTempCelsius?.let { String.format(Locale.US, "%.2f", it) } ?: "—",
            width = 52.dp,
            color = Ink2
        )
        TableCell(
            if (log.painVasScore > 0) stringResource(R.string.v4_an_pain_format, log.painVasScore) else "—",
            width = 44.dp,
            color = Ink2
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TableCell(
    text: String,
    weight: Float? = null,
    width: androidx.compose.ui.unit.Dp? = null,
    bold: Boolean = false,
    semibold: Boolean = false,
    color: Color = Ink3,
    align: TextAlign = TextAlign.Center
) {
    val base = AppType.caption.copy(
        fontSize = 11.sp,
        fontWeight = when {
            bold -> FontWeight.Bold
            semibold -> FontWeight.SemiBold
            else -> FontWeight.Normal
        }
    )
    Box(
        modifier = (if (weight != null) Modifier.weight(weight) else Modifier.width(width ?: 0.dp)),
        contentAlignment = when (align) {
            TextAlign.Start -> Alignment.CenterStart
            else -> Alignment.Center
        }
    ) {
        Text(text = text, style = base, color = color, maxLines = 1)
    }
}

// --------------------------------------------------------------- helpers ---

/** Cycle length implied by the running prediction, until two cycles have actually completed. */
private fun estimateCycleLength(latestCycle: CycleEntity?, prediction: FertilePrediction?): Int? {
    if (latestCycle == null || prediction == null) return null
    val days = ChronoUnit.DAYS.between(latestCycle.startDate, prediction.predictedNextPeriodDate).toInt()
    return days.takeIf { it in 15..60 }
}

private fun shortDateFormat(): DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
