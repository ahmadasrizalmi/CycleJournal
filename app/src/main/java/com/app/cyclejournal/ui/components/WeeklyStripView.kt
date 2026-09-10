package com.app.cyclejournal.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.data.local.entity.CervicalMucusType
import com.app.cyclejournal.data.local.entity.FlowIntensity
import com.app.cyclejournal.ui.CyclePhase
import com.app.cyclejournal.ui.DayStripItem
import com.app.cyclejournal.ui.theme.*

/**
 * Ergonomic 7-day horizontal strip view.
 * Displays dynamic entry dots strictly conditioned on Room DB state:
 * - Shows a small 4.dp accent dot if a daily log exists for the date.
 * - Renders an empty 4.dp placeholder spacer if no log entry exists, preventing layout shift without ghost dots.
 */
@Composable
fun WeeklyStripView(
    weekDays: List<DayStripItem>,
    selectedDay: DayStripItem,
    isDarkMode: Boolean,
    onSelectDay: (DayStripItem) -> Unit,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkMode) DarkCardBackground else Color.White
    val borderCol = if (isDarkMode) DarkBorder else Slate100
    val textPrimary = if (isDarkMode) Color.White else Slate900

    Surface(
        modifier = modifier,
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
                        text = "Minggu Ini",
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
                    val log = item.log
                    val hasLogEntry = log != null && (
                        log.basalBodyTempCelsius != null ||
                        log.painVasScore > 0 ||
                        log.cervicalMucus != CervicalMucusType.NONE ||
                        log.flow != FlowIntensity.NONE ||
                        !log.notes.isNullOrBlank()
                    )

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

                        // Dynamic entry dot: only render dot if log exists in Room DB
                        if (hasLogEntry) {
                            val dotColor = if (item.phase == CyclePhase.FERTILE || item.phase == CyclePhase.OVULATION) MedicalCyan else PrimaryCoral
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        } else {
                            // Empty placeholder so layout never shifts, but no ghost dot appears
                            Spacer(modifier = Modifier.size(4.dp))
                        }
                    }
                }
            }
        }
    }
}
