package com.app.cyclejournal.ui.report

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.ui.home.CycleFilter
import java.time.format.DateTimeFormatter

private val TextPrimary   = Color(0xDE000000) // 87% black
private val TextSecondary = Color(0x99000000) // 60% black

/**
 * One row in the cycle history list: date range + length label + visual timeline bar.
 *
 * For an ongoing (active) cycle [cycle.endDate] is null; the right side of the date
 * label shows "Aktif" and cycleLength falls back to elapsed days.
 */
@Composable
fun CycleHistoryRow(
    cycle: CycleEntity,
    activeFilter: CycleFilter,
    elapsedDaysIfActive: Int = 0,
    expectedCycleLength: Int = 28,
    textColor: Color = TextPrimary,
    subTextColor: Color = TextSecondary,
    trackColor: Color = Color(0xFFF2ECEE),
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val fmt = DateTimeFormatter.ofPattern("d MMM")
    val isOngoing = cycle.endDate == null

    val cycleLength = if (isOngoing) {
        maxOf(expectedCycleLength, elapsedDaysIfActive)
    } else {
        cycle.cycleLengthDays ?: expectedCycleLength
    }

    val ovulationDay = (cycleLength - 14).coerceIn(1, cycleLength)

    val periodEnd = cycle.startDate.plusDays((cycle.periodDurationDays - 1).toLong().coerceAtLeast(0L))
    val startStr = cycle.startDate.format(fmt)
    val periodTitle = if (isOngoing) {
        stringResource(R.string.cycle_row_range_ongoing, startStr)
    } else {
        "$startStr – ${periodEnd.format(fmt)}"
    }
    val lengthLabel = if (isOngoing) {
        stringResource(R.string.cycle_row_cycle_day_active, elapsedDaysIfActive)
    } else {
        stringResource(R.string.cycle_row_length_days, cycleLength)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onClick() }
                        .padding(vertical = 4.dp)
                } else Modifier
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = periodTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                if (onClick != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("›", fontSize = 15.sp, color = subTextColor, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = lengthLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = subTextColor
            )
        }
        Spacer(modifier = Modifier.height(5.dp))

        CycleTimelineCanvas(
            cycleLength = cycleLength,
            periodDays = cycle.periodDurationDays,
            ovulationDay = ovulationDay,
            activeFilter = activeFilter,
            todayDay = if (isOngoing) elapsedDaysIfActive else null,
            trackColor = trackColor,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
