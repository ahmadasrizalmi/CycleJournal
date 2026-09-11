package com.app.cyclejournal.ui.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.app.cyclejournal.ui.home.CycleFilter

private val PinkHot       = Color(0xFFFF5A85)
private val FertileCyan   = Color(0xFF67E8F9) // Cyan Muda (SSOT Kalender)
private val OvulationTeal = Color(0xFF0891B2) // Teal / Cyan Pekat (SSOT Kalender)
private val TrackBg       = Color(0xFFF2ECEE)

/**
 * Canvas-based horizontal timeline bar for a single cycle.
 *
 * Draws three overlaid layers:
 *  1. Gray track (full width = cycleLength days)
 *  2. Pink bar  = menstrual bleeding days (from day 0)
 *  3. Gold bar  = fertile window (ovulationDay-5 to ovulationDay, clamped ≥ 0)
 *  4. Orange dot = ovulation point (clamped within track bounds)
 *
 * Visibility of each layer is controlled by [activeFilter].
 *
 * @param cycleLength  Total days in this cycle (denominator for proportional widths).
 * @param periodDays   Consecutive bleeding days at cycle start.
 * @param ovulationDay Day number within cycle where ovulation is estimated (1-indexed).
 */
@Composable
fun CycleTimelineCanvas(
    cycleLength: Int,
    periodDays: Int,
    ovulationDay: Int,
    activeFilter: CycleFilter,
    todayDay: Int? = null,
    trackColor: Color = TrackBg,
    modifier: Modifier = Modifier
) {
    val safeLength = cycleLength.coerceAtLeast(1)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
    ) {
        val totalWidth = size.width
        val dayWidth = totalWidth / safeLength
        val barHeight = size.height
        val radius = CornerRadius(barHeight / 2, barHeight / 2)

        // 1. Track background
        drawRoundRect(
            color = trackColor,
            topLeft = Offset.Zero,
            size = Size(totalWidth, barHeight),
            cornerRadius = radius
        )

        // 2. Menstrual bleeding bar (pink) — starts at Day 1 (0f)
        if (activeFilter == CycleFilter.ALL || activeFilter == CycleFilter.PERIOD) {
            val safePeriod = periodDays.coerceIn(1, safeLength)
            val w = (safePeriod * dayWidth).coerceIn(0f, totalWidth)
            if (w > 0f) {
                drawRoundRect(
                    color = PinkHot,
                    topLeft = Offset.Zero,
                    size = Size(w, barHeight),
                    cornerRadius = radius
                )
            }
        }

        // 3. Fertile window bar (gold) — 6 days ending on ovulationDay
        // Day 1-indexed: starts at (ovulationDay - 5), ends at ovulationDay
        if (activeFilter == CycleFilter.ALL || activeFilter == CycleFilter.FERTILE) {
            val fertileStartDay = (ovulationDay - 5).coerceAtLeast(1)
            val fertileEndDay = ovulationDay.coerceIn(fertileStartDay, safeLength)
            val left = ((fertileStartDay - 1) * dayWidth).coerceIn(0f, totalWidth)
            val right = (fertileEndDay * dayWidth).coerceIn(left, totalWidth)
            val w = (right - left).coerceAtLeast(0f)
            if (w > 0f) {
                drawRoundRect(
                    color = FertileCyan,
                    topLeft = Offset(left, 0f),
                    size = Size(w, barHeight),
                    cornerRadius = radius
                )
            }
        }

        // 4. Ovulation dot (white fill + orange stroke) — centered on ovulationDay
        if (activeFilter == CycleFilter.ALL || activeFilter == CycleFilter.OVULATION) {
            val safeOvDay = ovulationDay.coerceIn(1, safeLength)
            val cx = ((safeOvDay - 0.5f) * dayWidth).coerceIn(7f, totalWidth - 7f)
            val cy = barHeight / 2
            val dotRadius = (barHeight / 2.2f).coerceAtLeast(5f)

            drawCircle(
                color = Color.White,
                radius = dotRadius,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = OvulationTeal,
                radius = dotRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 2.5f)
            )
        }

        // 5. Active Cycle: Today marker (indicator line on current day)
        if (todayDay != null && todayDay in 1..safeLength) {
            val todayX = ((todayDay - 0.5f) * dayWidth).coerceIn(2f, totalWidth - 2f)
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(todayX, -2.dp.toPx()),
                end = Offset(todayX, barHeight + 2.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
