package com.app.cyclejournal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.domain.model.FertilePrediction
import com.app.cyclejournal.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CycleCalendarView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    menstruationDates: Set<LocalDate>,
    prediction: FertilePrediction?,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundWhite)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Month Navigator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChanged(currentMonth.minusMonths(1)) }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Bulan Lalu")
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = { onMonthChanged(currentMonth.plusMonths(1)) }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Bulan Depan")
            }
        }

        // 2. Day of Week Header (Mon to Sun)
        Row(modifier = Modifier.fillMaxWidth()) {
            val dayHeaders = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
            dayHeaders.forEach { name ->
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        // 3. Calendar Day Cells Grid (Monday-first offset)
        val firstOfMonth = currentMonth.atDay(1)
        val daysInMonth = currentMonth.lengthOfMonth()
        val startOffset = (firstOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
        val totalCells = daysInMonth + startOffset
        val rows = (totalCells + 6) / 7

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (r in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (c in 0 until 7) {
                        val cellIndex = (r * 7) + c
                        val dayNumber = cellIndex - startOffset + 1
                        if (dayNumber in 1..daysInMonth) {
                            val cellDate = currentMonth.atDay(dayNumber)
                            val isSelected = cellDate == selectedDate
                            val isPeriod = menstruationDates.contains(cellDate)
                            val isOvulation = prediction?.predictedOvulationDate == cellDate
                            val isFertile = prediction != null &&
                                    !cellDate.isBefore(prediction.fertileWindowStart) &&
                                    !cellDate.isAfter(prediction.fertileWindowEnd)

                            DayCell(
                                date = cellDate,
                                isSelected = isSelected,
                                isPeriod = isPeriod,
                                isFertile = isFertile,
                                isOvulation = isOvulation,
                                onClick = { onDateSelected(cellDate) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 4. Clinical Phase Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendDot(color = MenstruationBg, border = MenstruationText, label = stringResource(R.string.legend_period))
            LegendDot(color = FertileBg, border = FertileText, label = stringResource(R.string.legend_fertile))
            LegendDot(color = OvulationIndicator, border = FertileText, label = stringResource(R.string.legend_ovulation), isRing = true)
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isPeriod: Boolean,
    isFertile: Boolean,
    isOvulation: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = date == LocalDate.now()

    val bgColor = when {
        isPeriod -> MenstruationBg
        isFertile -> FertileBg
        else -> Color.Transparent
    }

    val textColor = when {
        isPeriod -> MenstruationText
        isFertile -> FertileText
        else -> TextPrimary
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .then(
                when {
                    isOvulation -> Modifier.border(1.5.dp, FertileText, CircleShape)
                    isSelected -> Modifier.border(2.dp, PrimaryCoral, CircleShape)
                    isToday -> Modifier.border(1.dp, TextDisabled, CircleShape)
                    else -> Modifier
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 12.sp,
                fontWeight = if (isSelected || isToday || isPeriod) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (isOvulation) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(OvulationIndicator)
                )
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, border: Color, label: String, isRing: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isRing) Color.Transparent else color)
                .border(1.dp, border, CircleShape)
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
