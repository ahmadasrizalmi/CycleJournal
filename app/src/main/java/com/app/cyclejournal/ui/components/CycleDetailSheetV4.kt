package com.app.cyclejournal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.ui.theme.AlertBrown
import com.app.cyclejournal.ui.theme.AppType
import com.app.cyclejournal.ui.theme.BrandEnd
import com.app.cyclejournal.ui.theme.BrandGradient
import com.app.cyclejournal.ui.theme.CanvasSoft
import com.app.cyclejournal.ui.theme.Dimens
import com.app.cyclejournal.ui.theme.HeroBottom
import com.app.cyclejournal.ui.theme.HeroStroke
import com.app.cyclejournal.ui.theme.HeroTop
import com.app.cyclejournal.ui.theme.Ink
import com.app.cyclejournal.ui.theme.Ink2
import com.app.cyclejournal.ui.theme.Ink3
import com.app.cyclejournal.ui.theme.Line
import com.app.cyclejournal.ui.theme.OkGreen
import com.app.cyclejournal.ui.theme.OkTint
import com.app.cyclejournal.ui.theme.OnBrand
import com.app.cyclejournal.ui.theme.Paper
import com.app.cyclejournal.ui.theme.RoseMuted
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.max

private const val LUTEAL_DAYS = 14
private const val FERTILE_DAYS_BEFORE_OVULATION = 6

/**
 * Detail sheet for one cycle, opened by tapping its row in the Analysis timeline.
 *
 * Mirrors the 1.1.3 metrics: cycle length, bleeding range, fertile window, estimated ovulation,
 * average basal temperature, peak pain and the symptoms recorded inside the cycle.
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun CycleDetailSheetV4(
    cycle: CycleEntity,
    allLogs: List<DailyLogEntity>,
    onDismiss: () -> Unit,
    onOpenCalendar: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    val isOngoing = cycle.endDate == null
    val cycleLength = if (isOngoing) {
        max(28, (ChronoUnit.DAYS.between(cycle.startDate, today) + 1).toInt())
    } else {
        cycle.cycleLengthDays ?: 28
    }
    val periodEnd = cycle.startDate.plusDays((cycle.periodDurationDays - 1).coerceAtLeast(0).toLong())
    val ovulationDay = (cycleLength - LUTEAL_DAYS).coerceIn(1, cycleLength)
    val ovulationDate = cycle.startDate.plusDays((ovulationDay - 1).toLong())
    val fertileStart = cycle.startDate.plusDays((ovulationDay - FERTILE_DAYS_BEFORE_OVULATION).coerceAtLeast(0).toLong())

    val cycleLogs = remember(cycle, allLogs) {
        allLogs.filter {
            !it.date.isBefore(cycle.startDate) && (cycle.endDate == null || !it.date.isAfter(cycle.endDate))
        }
    }
    val bbtValues = cycleLogs.mapNotNull { it.basalBodyTempCelsius }
    val avgBbt = if (bbtValues.isNotEmpty()) {
        String.format(Locale.US, "%.2f °C", bbtValues.average())
    } else {
        stringResource(R.string.report_value_not_recorded)
    }
    val peakPain = cycleLogs.maxOfOrNull { it.painVasScore } ?: 0
    val symptoms = remember(cycleLogs) {
        cycleLogs.mapNotNull { it.painLocation }
            .flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    val dayMonth = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    val rangeText = if (cycle.endDate == null) {
        stringResource(R.string.v4_an_ongoing)
    } else {
        stringResource(
            R.string.v4_an_cycle_range,
            cycle.startDate.format(dayMonth),
            cycle.endDate.format(dayMonth)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Paper,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = Dimens.SheetRadius, topEnd = Dimens.SheetRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // The sheet renders in its own window, so it re-declares the resource-id opt-in.
                .semantics { testTagsAsResourceId = true }
                .testTag("sheet_cycle_detail")
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Line)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(
                            if (isOngoing) R.string.report_metric_current_cycle else R.string.detail_title_cycle_history
                        ),
                        style = AppType.Display.let {
                            androidx.compose.ui.text.TextStyle(fontFamily = AppType.Heading, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        },
                        color = Ink
                    )
                    Text(text = rangeText, style = AppType.caption.copy(fontSize = 13.sp), color = Ink3)
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CanvasSoft)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.app_close), tint = Ink2, modifier = Modifier.size(17.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // cycle length summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.CardRadiusLg))
                        .background(Brush.verticalGradient(listOf(HeroTop, HeroBottom)))
                        .border(1.dp, HeroStroke, RoundedCornerShape(Dimens.CardRadiusLg))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.report_metric_current_cycle),
                            style = AppType.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
                            color = BrandEnd
                        )
                        Text(
                            text = stringResource(R.string.detail_cycle_length_format, cycleLength),
                            style = androidx.compose.ui.text.TextStyle(fontFamily = AppType.Heading, fontSize = 28.sp, fontWeight = FontWeight.Bold),
                            color = Ink
                        )
                    }
                    if (isOngoing) {
                        Box(
                            modifier = Modifier
                                .height(22.dp)
                                .clip(CircleShape)
                                .background(OkTint)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.detail_status_ongoing),
                                style = AppType.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = OkGreen
                            )
                        }
                    }
                }

                SoftCard(modifier = Modifier.fillMaxWidth(), radius = Dimens.CardRadius, fill = Paper, border = Line) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                        DetailRow(
                            label = stringResource(R.string.report_metric_period_length),
                            value = stringResource(
                                R.string.detail_period_duration_format,
                                cycle.periodDurationDays,
                                cycle.startDate.dayOfMonth,
                                periodEnd.dayOfMonth,
                                periodEnd.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))
                            )
                        )
                        HairLine()
                        DetailRow(
                            label = stringResource(R.string.detail_metric_fertile_window),
                            value = stringResource(
                                R.string.v4_an_cycle_range,
                                fertileStart.format(dayMonth),
                                ovulationDate.plusDays(1).format(dayMonth)
                            )
                        )
                        HairLine()
                        DetailRow(
                            label = stringResource(R.string.detail_metric_ovulation_peak_estimate),
                            value = stringResource(
                                R.string.detail_ovulation_day_format,
                                ovulationDay,
                                ovulationDate.dayOfMonth,
                                ovulationDate.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))
                            )
                        )
                        HairLine()
                        DetailRow(label = stringResource(R.string.detail_metric_average_temperature), value = avgBbt)
                        HairLine()
                        DetailRow(
                            label = stringResource(R.string.detail_metric_peak_pain_scale),
                            value = stringResource(R.string.v4_an_pain_format, peakPain),
                            valueColor = if (peakPain >= 7) AlertBrown else Ink
                        )
                    }
                }

                if (symptoms.isNotEmpty()) {
                    SoftCard(modifier = Modifier.fillMaxWidth(), radius = Dimens.CardRadius, fill = Paper, border = Line) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.detail_symptoms_recorded),
                                style = AppType.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                                color = Ink3
                            )
                            Text(
                                text = symptoms.joinToString(" · "),
                                style = AppType.body.copy(fontSize = 13.sp),
                                color = Ink2
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(CircleShape)
                        .background(BrandGradient)
                        .clickable { onOpenCalendar(cycle.startDate) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(
                            R.string.detail_open_in_calendar,
                            cycle.startDate.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
                        ),
                        style = AppType.cardTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                        color = OnBrand
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = Ink) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = label, style = AppType.listRow, color = Ink2, modifier = Modifier.weight(1f))
        Text(text = value, style = AppType.listValue, color = valueColor)
    }
}
