import re

with open('app/src/main/java/com/app/cyclejournal/ui/CycleJournalApp.kt', 'r', encoding='utf-8') as f:
    text = f.read()

# 1. Update Dashboard Hero Card calculations and empty state handling
old_dashboard_calc = '''    val today = remember { LocalDate.now() }
    val currentCycleDay = latestCycle?.let {
        (ChronoUnit.DAYS.between(it.startDate, today) + 1L).coerceAtLeast(1L)
    } ?: 14L

    val avgCycleDays = cycleStats?.averageLength ?: 28.0
    val stdDev = cycleStats?.standardDeviation ?: 1.5

    val isBleedingToday = selectedDay.phase == CyclePhase.MENSTRUATION
    val isFertileToday = selectedDay.phase == CyclePhase.FERTILE || selectedDay.phase == CyclePhase.OVULATION

    val phaseBadge = when {
        isDiscreet -> "FASE 02"
        isBleedingToday -> "MENSTRUASI"
        selectedDay.phase == CyclePhase.OVULATION -> "PUNCAK OVULASI"
        isFertileToday -> "JENDELA SUBUR"
        else -> "FASE FOLIKULER"
    }

    val phaseTitle = when {
        isDiscreet -> "Periode Tengah"
        isBleedingToday -> "Fase Menstruasi"
        selectedDay.phase == CyclePhase.OVULATION -> "Puncak Ovulasi"
        isFertileToday -> "Fase Folikuler"
        else -> "Fase Folikuler"
    }

    val ovulationCountdown = fertilePrediction?.let {
        ChronoUnit.DAYS.between(today, it.predictedOvulationDate).toInt()
    }
    val subtitleText = when {
        isDiscreet -> "Pencatatan normal berlangsung"
        ovulationCountdown != null && ovulationCountdown > 0 -> "Ovulasi dalam $ovulationCountdown hari ke depan"
        ovulationCountdown == 0 -> "Ovulasi berlangsung hari ini!"
        else -> "Pencatatan normal berlangsung"
    }

    val conceptionChance = when {
        selectedDay.phase == CyclePhase.OVULATION -> "Maksimal (95%)"
        isFertileToday -> "Tinggi (85%)"
        isBleedingToday -> "Sangat Rendah (<5%)"
        else -> "Rendah (15%)"
    }

    val progressRatio = (currentCycleDay.toFloat() / avgCycleDays.toFloat()).coerceIn(0f, 1f)'''

new_dashboard_calc = '''    val today = remember { LocalDate.now() }
    val hasActiveCycle = latestCycle != null

    val currentCycleDay = latestCycle?.let {
        (ChronoUnit.DAYS.between(it.startDate, today) + 1L).coerceAtLeast(1L)
    }

    val avgCycleDays = cycleStats?.averageLength
    val stdDev = cycleStats?.standardDeviation

    val isBleedingToday = selectedDay.phase == CyclePhase.MENSTRUATION
    val isFertileToday = selectedDay.phase == CyclePhase.FERTILE || selectedDay.phase == CyclePhase.OVULATION

    val phaseBadge = when {
        isDiscreet -> if (hasActiveCycle) "FASE 02" else "FASE 00"
        !hasActiveCycle -> "MEMULAI SIKLUS"
        isBleedingToday -> "MENSTRUASI"
        selectedDay.phase == CyclePhase.OVULATION -> "PUNCAK OVULASI"
        isFertileToday -> "JENDELA SUBUR"
        else -> "FASE FOLIKULER"
    }

    val phaseTitle = when {
        isDiscreet -> if (hasActiveCycle) "Periode Tengah" else "Mulai Jurnal"
        !hasActiveCycle -> "Mulai Jurnal Anda"
        isBleedingToday -> "Fase Menstruasi"
        selectedDay.phase == CyclePhase.OVULATION -> "Puncak Ovulasi"
        isFertileToday -> "Jendela Subur"
        else -> "Fase Folikuler"
    }

    val ovulationCountdown = fertilePrediction?.let {
        ChronoUnit.DAYS.between(today, it.predictedOvulationDate).toInt()
    }
    val subtitleText = when {
        isDiscreet -> "Pencatatan normal berlangsung"
        !hasActiveCycle -> "Catat hari pertama haid untuk mengaktifkan kalkulasi otomatis FIGO."
        ovulationCountdown != null && ovulationCountdown > 0 -> "Ovulasi dalam $ovulationCountdown hari ke depan"
        ovulationCountdown == 0 -> "Ovulasi berlangsung hari ini!"
        else -> "Pencatatan siklus normal berlangsung"
    }

    val conceptionChance = when {
        !hasActiveCycle -> "--"
        selectedDay.phase == CyclePhase.OVULATION -> "Maksimal (95%)"
        isFertileToday -> "Tinggi (85%)"
        isBleedingToday -> "Sangat Rendah (<5%)"
        else -> "Rendah (15%)"
    }

    val progressRatio = if (currentCycleDay != null && avgCycleDays != null && avgCycleDays > 0) {
        (currentCycleDay.toFloat() / avgCycleDays.toFloat()).coerceIn(0.05f, 1f)
    } else 0f'''

assert old_dashboard_calc in text, 'old_dashboard_calc not found'
text = text.replace(old_dashboard_calc, new_dashboard_calc)

# Update the Hero Card metric pills text
old_hero_pills_text = '''                                    Text(
                                        text = String.format(Locale.US, "%.0f Hari (±%.1f)", avgCycleDays, stdDev),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )'''

new_hero_pills_text = '''                                    Text(
                                        text = if (avgCycleDays != null) String.format(Locale.US, "%.0f Hari (±%.1f)", avgCycleDays, stdDev ?: 1.5) else "-- Hari",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )'''

assert old_hero_pills_text in text, 'old_hero_pills_text not found'
text = text.replace(old_hero_pills_text, new_hero_pills_text)

# Update Circular Progress Ring text when no active cycle
old_ring_text = '''                            Text(
                                text = "$currentCycleDay",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                lineHeight = 28.sp
                            )
                            Text(
                                text = "dari ${avgCycleDays.toInt()}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFFE4E6)
                            )'''

new_ring_text = '''                            Text(
                                text = if (currentCycleDay != null) "$currentCycleDay" else "--",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                lineHeight = 28.sp
                            )
                            Text(
                                text = if (avgCycleDays != null) "dari ${avgCycleDays.toInt()}" else "dari --",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFFE4E6)
                            )'''

assert old_ring_text in text, 'old_ring_text not found'
text = text.replace(old_ring_text, new_ring_text)

# 2. Update BBT Sparkline Card: remove fake 7-point curve fallback
old_bbt_block = '''                        Surface(
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
                        }'''

new_bbt_block = '''                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (hasRealBbt) Color(0xFFECFDF5) else Slate100,
                            border = BorderStroke(1.dp, if (hasRealBbt) Color(0xFFA7F3D0) else Slate200)
                        ) {
                            Text(
                                text = if (hasRealBbt) "Normal" else "Belum Ada Data",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasRealBbt) Color(0xFF065F46) else Slate500,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }'''

assert old_bbt_block in text, 'old_bbt_block not found'
text = text.replace(old_bbt_block, new_bbt_block)

old_bbt_canvas_logic = '''                    Canvas(
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

                        val pts = if (hasRealBbt && logsWithBbt.size >= 2) {
                            val minT = 36.0
                            val maxT = 37.0
                            logsWithBbt.mapIndexed { idx, item ->
                                val x = w * (idx.toFloat() / (logsWithBbt.size - 1).coerceAtLeast(1).toFloat())
                                val temp = item.basalBodyTempCelsius ?: 36.4
                                val y = h * (1f - ((temp - minT) / (maxT - minT)).toFloat().coerceIn(0.1f, 0.9f))
                                Offset(x, y)
                            }
                        } else {
                            listOf(
                                Offset(w * 0.05f, h * 0.72f),
                                Offset(w * 0.20f, h * 0.68f),
                                Offset(w * 0.36f, h * 0.60f),
                                Offset(w * 0.52f, h * 0.64f),
                                Offset(w * 0.68f, h * 0.44f),
                                Offset(w * 0.84f, h * 0.26f),
                                Offset(w * 0.95f, h * 0.18f)
                            )
                        }

                        val curvePath = Path().apply {
                            moveTo(pts.first().x, pts.first().y)
                            for (i in 1 until pts.size) {
                                val prev = pts[i - 1]
                                val curr = pts[i]
                                val midX = (prev.x + curr.x) / 2f
                                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                        }

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

                        drawPath(
                            path = curvePath,
                            color = Coral600,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        pts.forEachIndexed { idx, pt ->
                            val isSelectedPoint = idx == (pts.size / 2)
                            val dotColor = if (isSelectedPoint) Slate900 else if (idx > pts.size / 2) MedicalCyan else Coral600
                            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                            drawCircle(color = dotColor, radius = 3.5.dp.toPx(), center = pt)
                        }
                    }'''

new_bbt_canvas_logic = '''                    if (hasRealBbt && logsWithBbt.size >= 2) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp)
                        ) {
                            val w = size.width
                            val h = size.height

                            val coverlineY = h * 0.58f
                            drawLine(
                                color = Color(0xFFCBD5E1),
                                start = Offset(0f, coverlineY),
                                end = Offset(w, coverlineY),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )

                            val minT = 36.0
                            val maxT = 37.0
                            val pts = logsWithBbt.mapIndexed { idx, item ->
                                val x = w * (idx.toFloat() / (logsWithBbt.size - 1).coerceAtLeast(1).toFloat())
                                val temp = item.basalBodyTempCelsius ?: 36.4
                                val y = h * (1f - ((temp - minT) / (maxT - minT)).toFloat().coerceIn(0.1f, 0.9f))
                                Offset(x, y)
                            }

                            val curvePath = Path().apply {
                                moveTo(pts.first().x, pts.first().y)
                                for (i in 1 until pts.size) {
                                    val prev = pts[i - 1]
                                    val curr = pts[i]
                                    val midX = (prev.x + curr.x) / 2f
                                    cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                                }
                            }

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

                            drawPath(
                                path = curvePath,
                                color = Coral600,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            pts.forEachIndexed { idx, pt ->
                                val isSelectedPoint = idx == (pts.size - 1)
                                val dotColor = if (isSelectedPoint) Slate900 else Coral600
                                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                                drawCircle(color = dotColor, radius = 3.5.dp.toPx(), center = pt)
                            }
                        }
                    } else {
                        // Clean empty state when no BBT recorded yet
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Belum ada rekaman suhu BBT harian.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Ukur suhu basal pagi hari sebelum beranjak dari tempat tidur untuk memantau pergeseran ovulasi (Aturan 3-over-6 FIGO).",
                                fontSize = 10.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                        }
                    }'''

assert old_bbt_canvas_logic in text, 'old_bbt_canvas_logic not found'
text = text.replace(old_bbt_canvas_logic, new_bbt_canvas_logic)

# 3. Update Quick Daily Log Summary Card: remove fake VAS 7 fallback
old_vas_calc = '''                    // Real VAS Logic: Green Comfortable if 0 or Bebas Nyeri, Red Alert ONLY if >= 7
                    val vasScore = log?.painVasScore ?: if (selectedDay.pain.contains("7")) 7 else 0
                    val isPainFree = vasScore == 0 || selectedDay.pain.contains("Bebas Nyeri", ignoreCase = true)
                    val isPainAlert = vasScore >= 7 || selectedDay.pain.contains("Sedang") || selectedDay.pain.contains("Berat")'''

new_vas_calc = '''                    // Real VAS Logic: Green Comfortable if 0 or Bebas Nyeri, Red Alert ONLY if >= 7
                    val vasScore = log?.painVasScore ?: 0
                    val isPainFree = vasScore == 0
                    val isPainAlert = vasScore >= 7'''

assert old_vas_calc in text, 'old_vas_calc not found'
text = text.replace(old_vas_calc, new_vas_calc)

old_bbt_boxes = '''                                Text("Suhu Basal (BBT)", fontSize = 9.sp, color = textSecondary)
                                Text(selectedDay.bbt, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, color = textPrimary)'''

new_bbt_boxes = '''                                Text("Suhu Basal (BBT)", fontSize = 9.sp, color = textSecondary)
                                Text(if (log?.basalBodyTempCelsius != null) String.format(Locale.US, "%.2f °C", log.basalBodyTempCelsius) else "-- °C", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, color = textPrimary)'''

assert old_bbt_boxes in text, 'old_bbt_boxes not found'
text = text.replace(old_bbt_boxes, new_bbt_boxes)

old_mucus_boxes = '''                                Text("Lendir Serviks", fontSize = 9.sp, color = textSecondary)
                                Text(selectedDay.mucus, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)'''

new_mucus_boxes = '''                                Text("Lendir Serviks", fontSize = 9.sp, color = textSecondary)
                                Text(if (log != null && log.cervicalMucus != CervicalMucusType.NONE) mucusLabelFor(log.cervicalMucus) else "--", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)'''

assert old_mucus_boxes in text, 'old_mucus_boxes not found'
text = text.replace(old_mucus_boxes, new_mucus_boxes)

# 4. In SpOgReportScreenView: remove fake 3-cycle table fallback
old_spog_table = '''                            val displayCycles = if (completedCycles.isNotEmpty()) {
                                completedCycles.takeLast(5).map { c ->
                                    val startStr = "${c.startDate.dayOfMonth} ${c.startDate.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}"
                                    val lenStr = "${c.cycleLengthDays ?: 28} Hari"
                                    val durStr = "${c.periodDurationDays} Hari"
                                    val ovStr = "${c.startDate.plusDays(14).dayOfMonth} ${c.startDate.plusDays(14).month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}"
                                    listOf(startStr, lenStr, durStr, ovStr)
                                }
                            } else {
                                listOf(
                                    listOf("01 Jan 26", "26 Hari", "5 Hari", "15 Jan"),
                                    listOf("27 Jan 26", "28 Hari", "5 Hari", "11 Feb"),
                                    listOf("24 Feb 26", "30 Hari", "5 Hari", "13 Mar")
                                )
                            }

                            displayCycles.forEach { row ->
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
                            }'''

new_spog_table = '''                            if (completedCycles.isNotEmpty()) {
                                completedCycles.takeLast(5).forEach { c ->
                                    val startStr = "${c.startDate.dayOfMonth} ${c.startDate.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}"
                                    val lenStr = "${c.cycleLengthDays ?: 28} Hari"
                                    val durStr = "${c.periodDurationDays} Hari"
                                    val ovStr = "${c.startDate.plusDays(14).dayOfMonth} ${c.startDate.plusDays(14).month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(startStr, fontSize = 10.sp, color = textPrimary)
                                        Text(lenStr, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                        Text(durStr, fontSize = 10.sp, color = textPrimary)
                                        Text(ovStr, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MedicalCyan)
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Belum ada riwayat siklus lengkap yang tercatat.\\nCatat dan selesaikan siklus pertama Anda untuk menampilkan log historis medis SpOG.",
                                        fontSize = 10.sp,
                                        color = textSecondary,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 14.sp
                                    )
                                }
                            }'''

assert old_spog_table in text, 'old_spog_table not found'
text = text.replace(old_spog_table, new_spog_table)

# In SpOgReportScreenView: remove fake 28.0 Hari / ±1.5 fallback when cycleStats is null
old_stats_boxes = '''                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ParameterBox("Rata-rata", String.format(Locale.US, "%.1f Hari", avgLen), Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox("Variasi Siklus", String.format(Locale.US, "±%.1f Hari", stdDev), Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox("Lama Haid", String.format(Locale.US, "%.1f Hari", avgPeriod), Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                    }'''

new_stats_boxes = '''                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val rStr = if (cycleStats != null) String.format(Locale.US, "%.1f Hari", cycleStats.averageLength) else "-- Hari"
                        val vStr = if (cycleStats != null) String.format(Locale.US, "±%.1f Hari", cycleStats.standardDeviation) else "-- Hari"
                        val dStr = if (cycleStats != null) String.format(Locale.US, "%.1f Hari", cycleStats.averagePeriodDuration) else "-- Hari"
                        ParameterBox("Rata-rata", rStr, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox("Variasi Siklus", vStr, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                        ParameterBox("Lama Haid", dStr, Modifier.weight(1f), isDarkMode, textPrimary, textSecondary)
                    }'''

assert old_stats_boxes in text, 'old_stats_boxes not found'
text = text.replace(old_stats_boxes, new_stats_boxes)

# 5. In CyclePredictionInsightCard: handle empty state when fertilePrediction == null
old_pred_card_calc = '''    val today = remember { LocalDate.now() }
    val nextDate = fertilePrediction?.predictedNextPeriodDate ?: today.plusDays(22)
    val daysToNextPeriod = ChronoUnit.DAYS.between(today, nextDate).coerceAtLeast(0)

    val ovulationDate = fertilePrediction?.predictedOvulationDate ?: today.plusDays(8)
    val daysToOvulation = ChronoUnit.DAYS.between(today, ovulationDate)

    val fertileStart = fertilePrediction?.fertileWindowStart ?: ovulationDate.minusDays(5)
    val fertileEnd = fertilePrediction?.fertileWindowEnd ?: ovulationDate.plusDays(1)'''

new_pred_card_calc = '''    val today = remember { LocalDate.now() }
    val hasPrediction = fertilePrediction != null

    val nextDate = fertilePrediction?.predictedNextPeriodDate
    val daysToNextPeriod = nextDate?.let { ChronoUnit.DAYS.between(today, it).coerceAtLeast(0) }

    val ovulationDate = fertilePrediction?.predictedOvulationDate
    val daysToOvulation = ovulationDate?.let { ChronoUnit.DAYS.between(today, it) }

    val fertileStart = fertilePrediction?.fertileWindowStart
    val fertileEnd = fertilePrediction?.fertileWindowEnd'''

assert old_pred_card_calc in text, 'old_pred_card_calc not found'
text = text.replace(old_pred_card_calc, new_pred_card_calc)

old_badge_text = 'text = "$daysToNextPeriod Hari Lagi",'
new_badge_text = 'text = if (daysToNextPeriod != null) "$daysToNextPeriod Hari Lagi" else "Siap Dihitung",'
assert old_badge_text in text
text = text.replace(old_badge_text, new_badge_text)

old_tile1_val = 'text = "${nextDate.dayOfMonth} ${nextDate.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}",'
new_tile1_val = 'text = if (nextDate != null) "${nextDate.dayOfMonth} ${nextDate.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}" else "--",'
assert old_tile1_val in text
text = text.replace(old_tile1_val, new_tile1_val)

old_tile2_val = 'text = "${ovulationDate.dayOfMonth} ${ovulationDate.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}",'
new_tile2_val = 'text = if (ovulationDate != null) "${ovulationDate.dayOfMonth} ${ovulationDate.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}" else "--",'
assert old_tile2_val in text
text = text.replace(old_tile2_val, new_tile2_val)

old_tile3_val = 'text = "${fertileStart.dayOfMonth}-${fertileEnd.dayOfMonth} ${fertileStart.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}",'
new_tile3_val = 'text = if (fertileStart != null && fertileEnd != null) "${fertileStart.dayOfMonth}-${fertileEnd.dayOfMonth} ${fertileStart.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }}" else "--",'
assert old_tile3_val in text
text = text.replace(old_tile3_val, new_tile3_val)

with open('app/src/main/java/com/app/cyclejournal/ui/CycleJournalApp.kt', 'w', encoding='utf-8') as f:
    f.write(text)

print('Successfully purged all mock data and fallbacks from CycleJournalApp.kt!')
