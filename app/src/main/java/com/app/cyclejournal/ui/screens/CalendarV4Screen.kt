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
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Egg
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.domain.model.CycleStats
import com.app.cyclejournal.domain.model.FertilePrediction
import com.app.cyclejournal.ui.components.SoftCard
import com.app.cyclejournal.ui.theme.AppType
import com.app.cyclejournal.ui.theme.BrandEnd
import com.app.cyclejournal.ui.theme.BrandGradient
import com.app.cyclejournal.ui.theme.BrandTint
import com.app.cyclejournal.ui.theme.BrandTint2
import com.app.cyclejournal.ui.theme.Dimens
import com.app.cyclejournal.ui.theme.HeroBottom
import com.app.cyclejournal.ui.theme.HeroTop
import com.app.cyclejournal.ui.theme.Ink
import com.app.cyclejournal.ui.theme.Ink2
import com.app.cyclejournal.ui.theme.Ink3
import com.app.cyclejournal.ui.theme.Lavender
import com.app.cyclejournal.ui.theme.LavenderInk
import com.app.cyclejournal.ui.theme.Line
import com.app.cyclejournal.ui.theme.OnBrand
import com.app.cyclejournal.ui.theme.OutOfMonthInk
import com.app.cyclejournal.ui.theme.Paper
import com.app.cyclejournal.ui.theme.PmsDot
import com.app.cyclejournal.ui.theme.RoseMuted
import com.app.cyclejournal.ui.theme.ShadowCard
import com.app.cyclejournal.ui.theme.Teal
import com.app.cyclejournal.ui.theme.TealMid
import com.app.cyclejournal.ui.theme.TealTint
import com.app.cyclejournal.ui.theme.TrackSoft
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale


/** PMS is the tail of the luteal phase: the five days before the predicted period. */
private const val PMS_DAYS_BEFORE_PERIOD = 5

/**
 * Calendar — CycleJournal v4 design (`Kalender · ID v13/v14/v15`).
 *
 * The month card is a real Monday-first grid driven by the cycle engine; tapping a
 * day selects it (tapping it again opens that day's log sheet), and the timeline
 * card mirrors the next period / fertile / ovulation events.
 */
@Composable
fun CalendarV4Screen(
    isDiscreet: Boolean,
    periodDates: Set<LocalDate>,
    fertilePrediction: FertilePrediction?,
    allLogs: List<DailyLogEntity>,
    latestCycle: CycleEntity?,
    completedCycles: List<CycleEntity>,
    cycleStats: CycleStats?,
    selectedCalendarDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onOpenLog: (LocalDate) -> Unit,
    onToast: (String) -> Unit
) {
    val today = remember { LocalDate.now() }
    var monthCursor by remember { mutableStateOf(YearMonth.from(selectedCalendarDate)) }
    LaunchedEffect(selectedCalendarDate) { monthCursor = YearMonth.from(selectedCalendarDate) }

    val monthLabel = remember(monthCursor) {
        monthCursor.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    }
    val todayLabel = stringResource(R.string.v4_cal_today)
    val seeAllLabel = stringResource(R.string.v4_cal_see_all)
    val timelineTitleStyle = androidx.compose.ui.text.TextStyle(
        fontFamily = AppType.Heading,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )

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
        // ---------------------------------------------------- month card ----
        item {
            SoftCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(26.dp, RoundedCornerShape(26.dp), ambientColor = ShadowCard, spotColor = ShadowCard),
                radius = 26.dp,
                fill = Paper,
                border = Color.Transparent,
                borderWidth = 0.dp,
                shadow = false
            ) {
                MonthHeader(
                    monthLabel = monthLabel,
                    onPrevious = { monthCursor = monthCursor.minusMonths(1) },
                    onNext = { monthCursor = monthCursor.plusMonths(1) },
                    onToday = {
                        onSelectDate(today)
                        monthCursor = YearMonth.from(today)
                        onToast(todayLabel)
                    }
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WeekdayHeader()
                    MonthGrid(
                        month = monthCursor,
                        selected = selectedCalendarDate,
                        today = today,
                        periodDates = periodDates,
                        completedCycles = completedCycles,
                        latestCycle = latestCycle,
                        fertilePrediction = fertilePrediction,
                        onDayClick = { date ->
                            if (date == selectedCalendarDate) onOpenLog(date) else onSelectDate(date)
                        }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .height(1.dp)
                            .background(Line)
                    )
                    CalendarLegend()
                }
            }
        }

        // ------------------------------------------------- timeline title ---
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.v4_cal_timeline),
                    style = timelineTitleStyle,
                    color = Ink,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = seeAllLabel,
                    style = AppType.cardTitle.copy(fontWeight = FontWeight.SemiBold),
                    color = BrandEnd,
                    modifier = Modifier.clickable { onToast(seeAllLabel) }
                )
            }
        }

        // ------------------------------------------------- timeline card ----
        item {
            val entries = buildTimeline(latestCycle, fertilePrediction, completedCycles, today)
            SoftCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(20.dp, RoundedCornerShape(24.dp), ambientColor = ShadowCard, spotColor = ShadowCard),
                radius = 24.dp,
                fill = Paper,
                border = Color.Transparent,
                borderWidth = 0.dp,
                shadow = false
            ) {
                if (entries.isEmpty()) {
                    Text(
                        text = stringResource(R.string.v4_cal_no_entries),
                        style = AppType.body.copy(fontSize = 12.sp),
                        color = Ink3,
                        modifier = Modifier.padding(20.dp)
                    )
                } else {
                    TimelineEntries(entries = entries, onOpenLog = onOpenLog)
                }
            }
        }
    }
}

// ------------------------------------------------------------ month card ---

@Composable
private fun MonthHeader(
    monthLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(HeroTop, HeroBottom)))
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Rounded.ChevronLeft,
            contentDescription = null,
            tint = BrandEnd,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onPrevious)
        )
        Text(
            text = monthLabel,
            style = AppType.cardTitle.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
            color = Ink
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .height(28.dp)
                .shadow(10.dp, CircleShape, ambientColor = Color(0x33FF5E7D), spotColor = Color(0x33FF5E7D))
                .clip(CircleShape)
                .background(BrandGradient)
                .clickable(onClick = onToday)
                .padding(horizontal = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.v4_cal_today),
                style = AppType.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                color = OnBrand
            )
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = BrandEnd,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onNext)
        )
    }
}

@Composable
private fun WeekdayHeader() {
    val labels = DayOfWeek.entries
        .sortedBy { it.value }
        .map { it.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault()) }
    Row(modifier = Modifier.fillMaxWidth()) {
        labels.forEach { label ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    style = AppType.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                    color = Ink3
                )
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selected: LocalDate,
    today: LocalDate,
    periodDates: Set<LocalDate>,
    completedCycles: List<CycleEntity>,
    latestCycle: CycleEntity?,
    fertilePrediction: FertilePrediction?,
    onDayClick: (LocalDate) -> Unit
) {
    val firstOfMonth = month.atDay(1)
    val leading = firstOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value
    val gridStart = firstOfMonth.minusDays(leading.toLong())
    val rows = ((leading + month.lengthOfMonth()) + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { column ->
                    val date = gridStart.plusDays((row * 7 + column).toLong())
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        DayCell(
                            date = date,
                            inMonth = YearMonth.from(date) == month,
                            isSelected = date == selected,
                            isToday = date == today,
                            isPeriod = isPeriodDay(date, periodDates, completedCycles, latestCycle),
                            isPms = isPmsDay(date, fertilePrediction),
                            isFertile = isFertileDay(date, fertilePrediction),
                            isOvulation = fertilePrediction?.predictedOvulationDate == date,
                            isPredictedPeriod = fertilePrediction?.predictedNextPeriodDate == date,
                            onClick = { onDayClick(date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    isPeriod: Boolean,
    isPms: Boolean,
    isFertile: Boolean,
    isOvulation: Boolean,
    isPredictedPeriod: Boolean,
    onClick: () -> Unit
) {
    val fill: Brush? = when {
        isToday || (isSelected && inMonth) -> BrandGradient
        !inMonth -> null
        isOvulation -> Brush.linearGradient(listOf(Teal, Teal))
        isPeriod || isPredictedPeriod -> Brush.linearGradient(listOf(BrandTint, BrandTint))
        isPms -> Brush.linearGradient(listOf(Lavender, Lavender))
        isFertile -> Brush.linearGradient(listOf(TealTint, TealTint))
        else -> null
    }
    val textColor = when {
        isToday || (isSelected && inMonth) -> OnBrand
        !inMonth -> OutOfMonthInk
        isOvulation -> OnBrand
        isPeriod || isPredictedPeriod -> BrandEnd
        isPms -> LavenderInk
        isFertile -> Teal
        else -> Ink
    }
    val dotColor = when {
        isToday || (isSelected && inMonth) -> Color.White
        !inMonth -> Color.Transparent
        isOvulation -> Color.White
        isPeriod || isPredictedPeriod -> BrandEnd
        isPms -> PmsDot
        isFertile -> TealMid
        else -> Color.Transparent
    }

    Column(
        modifier = Modifier
            .height(40.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .then(if (fill != null) Modifier.background(fill) else Modifier)
                .then(
                    if (isSelected && !isToday) Modifier.border(1.5.dp, BrandEnd, CircleShape) else Modifier
                )
                .then(
                    if (isPredictedPeriod && !isPeriod && !isToday && inMonth) {
                        Modifier.border(1.dp, BrandTint2, CircleShape)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = AppType.cardTitle.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                color = textColor
            )
        }
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}

@Composable
private fun CalendarLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(BrandEnd, stringResource(R.string.v4_cal_legend_period))
        LegendItem(PmsDot, stringResource(R.string.v4_cal_legend_pms))
        LegendItem(TealMid, stringResource(R.string.v4_cal_legend_fertile))
        LegendItem(Teal, stringResource(R.string.v4_cal_legend_ovulation))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = AppType.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            color = RoseMuted
        )
    }
}

// -------------------------------------------------------------- timeline ---

private data class TimelineEntry(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val title: String,
    val subtitle: String,
    val time: String,
    val date: LocalDate
)

@Composable
private fun buildTimeline(
    latestCycle: CycleEntity?,
    fertilePrediction: FertilePrediction?,
    completedCycles: List<CycleEntity>,
    today: LocalDate
): List<TimelineEntry> {
    val entries = mutableListOf<TimelineEntry>()
    val fmt = remember { DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()) }
    val periodTitle = stringResource(R.string.v4_cal_period_started)
    val fertileTitle = stringResource(R.string.v4_cal_fertile_window)
    val fertileSub = stringResource(R.string.v4_cal_high_chance)
    val ovulationTitle = stringResource(R.string.v4_cal_expected_ovulation)
    val ovulationSub = stringResource(R.string.v4_cal_peak_fertility)
    val bbtTitle = stringResource(R.string.v4_cal_log_bbt)
    val bbtSub = stringResource(R.string.v4_cal_morning_reminder)
    val todayLabel = stringResource(R.string.v4_cal_today)
    val expectedToday = stringResource(R.string.v4_cal_expected_today)

    val latestPeriodStart = latestCycle?.startDate
        ?: completedCycles.maxByOrNull { it.startDate }?.startDate
    if (latestPeriodStart != null) {
        entries += TimelineEntry(
            icon = Icons.Rounded.WaterDrop,
            iconTint = Ink,
            iconBackground = BrandTint,
            title = periodTitle,
            subtitle = if (latestPeriodStart == today) expectedToday else periodTitle,
            time = if (latestPeriodStart == today) todayLabel else latestPeriodStart.format(fmt),
            date = latestPeriodStart
        )
    }
    fertilePrediction?.let { prediction ->
        entries += TimelineEntry(
            icon = Icons.Rounded.Spa,
            iconTint = Ink,
            iconBackground = TealTint,
            title = fertileTitle,
            subtitle = fertileSub,
            time = prediction.fertileWindowStart.format(fmt),
            date = prediction.fertileWindowStart
        )
        entries += TimelineEntry(
            icon = Icons.Rounded.Egg,
            iconTint = Ink,
            iconBackground = Lavender,
            title = ovulationTitle,
            subtitle = ovulationSub,
            time = prediction.predictedOvulationDate.format(fmt),
            date = prediction.predictedOvulationDate
        )
    }
    entries += TimelineEntry(
        icon = Icons.Rounded.Thermostat,
        iconTint = Ink,
        iconBackground = Color(0xFFF7F7FA),
        title = bbtTitle,
        subtitle = bbtSub,
        time = "07:00",
        date = today
    )
    return entries
}

@Composable
private fun TimelineEntries(entries: List<TimelineEntry>, onOpenLog: (LocalDate) -> Unit) {
    val railColor = TrackSoft
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val x = 18.dp.toPx()
                val top = 20.dp.toPx()
                drawRoundRect(
                    color = railColor,
                    topLeft = Offset(x, top),
                    size = Size(2.dp.toPx(), (size.height - top - top).coerceAtLeast(0f)),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
            }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        entries.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable { onOpenLog(entry.date) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(entry.iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(entry.icon, contentDescription = null, tint = entry.iconTint, modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = entry.title,
                        style = AppType.cardTitle.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                        color = Ink
                    )
                    Text(text = entry.subtitle, style = AppType.caption, color = Ink3)
                }
                Text(
                    text = entry.time,
                    style = AppType.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = if (entry.date == LocalDate.now()) BrandEnd else Ink3,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

// --------------------------------------------------------------- helpers ---

private fun isPeriodDay(
    date: LocalDate,
    periodDates: Set<LocalDate>,
    completedCycles: List<CycleEntity>,
    latestCycle: CycleEntity?
): Boolean {
    if (date in periodDates) return true
    val cycles = completedCycles + listOfNotNull(latestCycle)
    return cycles.any { cycle ->
        val duration = cycle.periodDurationDays.takeIf { it > 0 } ?: 5
        !date.isBefore(cycle.startDate) && date.isBefore(cycle.startDate.plusDays(duration.toLong()))
    }
}

private fun isFertileDay(date: LocalDate, prediction: FertilePrediction?): Boolean =
    prediction != null && !date.isBefore(prediction.fertileWindowStart) && !date.isAfter(prediction.fertileWindowEnd)

private fun isPmsDay(date: LocalDate, prediction: FertilePrediction?): Boolean {
    if (prediction == null) return false
    val start = prediction.predictedNextPeriodDate.minusDays(PMS_DAYS_BEFORE_PERIOD.toLong())
    return !date.isBefore(start) && date.isBefore(prediction.predictedNextPeriodDate)
}
