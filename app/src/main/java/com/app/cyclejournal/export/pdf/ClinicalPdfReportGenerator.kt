package com.app.cyclejournal.export.pdf

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.app.cyclejournal.R
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.domain.model.AnomalyAlert
import com.app.cyclejournal.domain.model.CycleStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Native Android Canvas and PdfDocument generator for official 1-page A4 SpOG-ready clinical reports.
 * Zero external heavy library dependencies.
 */
class ClinicalPdfReportGenerator(private val context: Context) {

    companion object {
        // Standard A4 dimensions at 72 DPI (Points)
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val MARGIN_HORIZONTAL = 40f
        const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_HORIZONTAL * 2)

        // Clinical Monochrome Palette with High-Contrast Red Accents
        private val COLOR_TEXT_PRIMARY = Color.rgb(17, 24, 39) // Slate 900
        private val COLOR_TEXT_MUTED = Color.rgb(75, 85, 99) // Slate 600
        private val COLOR_BORDER = Color.rgb(229, 231, 235) // Light Divider
        private val COLOR_BG_LIGHT = Color.rgb(249, 250, 251) // Slate 50 Card BG
        private val COLOR_ALERT_BG = Color.rgb(254, 242, 242) // Red 50
        private val COLOR_ALERT_BORDER = Color.rgb(239, 68, 68) // Red 500
        private val COLOR_ALERT_TEXT = Color.rgb(153, 27, 27) // Red 800
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    // Initializing Paint Styles
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_TEXT_PRIMARY
        textSize = 8.5f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    private val boldTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_TEXT_PRIMARY
        textSize = 8.5f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_TEXT_PRIMARY
        textSize = 14f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val sectionHeadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_TEXT_PRIMARY
        textSize = 10f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_BORDER
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    /**
     * Generates and writes an A4 clinical PDF document to [outputFile] strictly on [Dispatchers.IO].
     */
    suspend fun generateReport(
        outputFile: File,
        patientIdentifier: String,
        stats: CycleStats?,
        cycles: List<CycleEntity>,
        anomalies: List<AnomalyAlert>,
        logs: List<DailyLogEntity> = emptyList(),
        ongoingCycle: CycleEntity? = null
    ): File = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var currentY = 40f

        // 1. Header with Official Logo Emblem & Patient Identifier
        currentY = drawHeader(canvas, currentY, patientIdentifier)

        // 2. FIGO Cycle Metrics Summary Grid Box + LMP / current cycle day
        currentY = drawMetricsSummary(canvas, currentY, stats, ongoingCycle)

        // 3. Clinical Red Flags & Anomalies Section
        currentY = drawAnomaliesSection(canvas, currentY, anomalies)

        // 4. Basal temperature curve: the chart a gynaecologist actually reads
        currentY = drawBbtChart(canvas, currentY, logs)

        // 5. Historical Cycle Log Table (Up to 6 completed cycles + the running one)
        currentY = drawCycleHistoryTable(canvas, currentY, cycles.take(6), ongoingCycle)

        // 6. Doctor's Clinical Verification & Stamp Section
        drawDoctorNotesSection(canvas, currentY)

        document.finishPage(page)

        // 7. Appendix page: the daily logs every number above is derived from
        if (logs.isNotEmpty()) {
            val appendixInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
            val appendix = document.startPage(appendixInfo)
            drawDailyLogAppendix(appendix.canvas, logs)
            document.finishPage(appendix)
        }

        // Ensure parent directories exist
        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { stream ->
            document.writeTo(stream)
        }
        document.close()

        outputFile
    }

    private fun drawHeader(canvas: Canvas, startY: Float, patientId: String): Float {
        var y = startY

        // Draw Official Emblem Logo Bitmap if available
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo_pdf_header)
            if (logoBitmap != null) {
                val logoSize = 36
                val destRect = Rect(
                    MARGIN_HORIZONTAL.toInt(),
                    y.toInt(),
                    (MARGIN_HORIZONTAL + logoSize).toInt(),
                    (y + logoSize).toInt()
                )
                canvas.drawBitmap(logoBitmap, null, destRect, null)
            }
        } catch (e: Exception) {
            // Fallback gracefully without crash if resource is omitted in mock tests
        }

        val textStartX = MARGIN_HORIZONTAL + 44f
        canvas.drawText(context.getString(R.string.pdf_title), textStartX, y + 14f, titlePaint)

        textPaint.color = COLOR_TEXT_MUTED
        textPaint.textSize = 8f
        canvas.drawText(context.getString(R.string.pdf_subtitle), textStartX, y + 26f, textPaint)

        y += 44f
        // Horizontal divider line
        canvas.drawLine(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y, strokePaint)
        y += 14f

        textPaint.color = COLOR_TEXT_PRIMARY
        textPaint.textSize = 8.5f
        canvas.drawText(context.getString(R.string.pdf_patient_id, patientId), MARGIN_HORIZONTAL, y, boldTextPaint)

        val dateText = context.getString(R.string.pdf_export_date, LocalDate.now().format(dateFormatter))
        val dateWidth = textPaint.measureText(dateText)
        canvas.drawText(dateText, (MARGIN_HORIZONTAL + CONTENT_WIDTH) - dateWidth, y, textPaint)

        return y + 18f
    }

    private fun drawMetricsSummary(
        canvas: Canvas,
        startY: Float,
        stats: CycleStats?,
        ongoingCycle: CycleEntity?
    ): Float {
        var y = startY
        canvas.drawText(context.getString(R.string.pdf_section_metrics), MARGIN_HORIZONTAL, y, sectionHeadingPaint)
        y += 8f

        val boxHeight = 44f
        val rect = RectF(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y + boxHeight)
        fillPaint.color = COLOR_BG_LIGHT
        canvas.drawRoundRect(rect, 4f, 4f, fillPaint)
        canvas.drawRoundRect(rect, 4f, 4f, strokePaint)

        val colWidth = CONTENT_WIDTH / 4f
        val textY = y + 18f
        val subTextY = y + 34f

        val metricLabels = listOf(
            context.getString(R.string.pdf_metric_avg_cycle) to
                context.getString(R.string.pdf_value_days, String.format(Locale.US, "%.1f", stats?.averageLength ?: 0.0)),
            context.getString(R.string.pdf_metric_variability) to
                context.getString(R.string.pdf_value_days_plusminus, String.format(Locale.US, "%.1f", stats?.standardDeviation ?: 0.0)),
            context.getString(R.string.pdf_metric_range) to
                context.getString(R.string.pdf_value_range_days, stats?.minLength ?: 0, stats?.maxLength ?: 0),
            context.getString(R.string.pdf_metric_avg_period) to
                context.getString(R.string.pdf_value_days, String.format(Locale.US, "%.1f", stats?.averagePeriodDuration ?: 0.0))
        )

        metricLabels.forEachIndexed { i, pair ->
            val colX = MARGIN_HORIZONTAL + (i * colWidth) + 10f
            textPaint.color = COLOR_TEXT_MUTED
            textPaint.textSize = 7.5f
            canvas.drawText(pair.first, colX, textY, textPaint)

            boldTextPaint.textSize = 10f
            boldTextPaint.color = COLOR_TEXT_PRIMARY
            canvas.drawText(pair.second, colX, subTextY, boldTextPaint)
        }

        y += boxHeight + 6f

        // LMP and the current cycle day: the two facts a clinician asks for first.
        val cycleDay = ongoingCycle?.let {
            java.time.temporal.ChronoUnit.DAYS.between(it.startDate, LocalDate.now()).toInt() + 1
        }
        val secondRect = RectF(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y + 30f)
        fillPaint.color = COLOR_BG_LIGHT
        canvas.drawRoundRect(secondRect, 4f, 4f, fillPaint)
        canvas.drawRoundRect(secondRect, 4f, 4f, strokePaint)

        val secondLabels = listOf(
            context.getString(R.string.pdf_metric_lmp) to
                (ongoingCycle?.startDate?.format(dateFormatter) ?: context.getString(R.string.report_value_not_recorded)),
            context.getString(R.string.pdf_metric_current_day) to
                (cycleDay?.let { context.getString(R.string.pdf_value_day_n, it) }
                    ?: context.getString(R.string.report_value_not_recorded))
        )
        secondLabels.forEachIndexed { i, pair ->
            val colX = MARGIN_HORIZONTAL + (i * (CONTENT_WIDTH / 2f)) + 10f
            textPaint.color = COLOR_TEXT_MUTED
            textPaint.textSize = 7.5f
            canvas.drawText(pair.first, colX, y + 12f, textPaint)
            boldTextPaint.textSize = 10f
            boldTextPaint.color = COLOR_TEXT_PRIMARY
            canvas.drawText(pair.second, colX, y + 25f, boldTextPaint)
        }

        return y + 30f + 16f
    }

    private fun drawAnomaliesSection(canvas: Canvas, startY: Float, anomalies: List<AnomalyAlert>): Float {
        var y = startY
        canvas.drawText(context.getString(R.string.pdf_section_anomalies), MARGIN_HORIZONTAL, y, sectionHeadingPaint)
        y += 8f

        if (anomalies.isEmpty()) {
            val emptyRect = RectF(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y + 24f)
            fillPaint.color = COLOR_BG_LIGHT
            canvas.drawRoundRect(emptyRect, 4f, 4f, fillPaint)
            canvas.drawRoundRect(emptyRect, 4f, 4f, strokePaint)

            textPaint.color = COLOR_TEXT_MUTED
            textPaint.textSize = 8f
            canvas.drawText(context.getString(R.string.pdf_anomalies_none), MARGIN_HORIZONTAL + 10f, y + 15f, textPaint)
            return y + 36f
        }

        anomalies.take(3).forEach { alert ->
            val alertRect = RectF(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y + 26f)
            fillPaint.color = COLOR_ALERT_BG
            canvas.drawRoundRect(alertRect, 4f, 4f, fillPaint)
            strokePaint.color = COLOR_ALERT_BORDER
            canvas.drawRoundRect(alertRect, 4f, 4f, strokePaint)
            strokePaint.color = COLOR_BORDER // Reset

            boldTextPaint.color = COLOR_ALERT_TEXT
            boldTextPaint.textSize = 8f
            canvas.drawText(context.getString(R.string.pdf_anomaly_line, alert.type.code, context.getString(alert.type.descriptionRes)), MARGIN_HORIZONTAL + 8f, y + 11f, boldTextPaint)

            textPaint.color = COLOR_ALERT_TEXT
            textPaint.textSize = 7.5f
            canvas.drawText(context.getString(R.string.pdf_anomaly_detail_line, alert.localizedDetail(context.resources), alert.detectedDate.format(dateFormatter)), MARGIN_HORIZONTAL + 8f, y + 21f, textPaint)

            y += 30f
        }

        return y + 6f
    }

    private fun drawCycleHistoryTable(
        canvas: Canvas,
        startY: Float,
        cycles: List<CycleEntity>,
        ongoingCycle: CycleEntity?
    ): Float {
        var y = startY
        canvas.drawText(context.getString(R.string.pdf_section_history), MARGIN_HORIZONTAL, y, sectionHeadingPaint)
        y += 10f

        val rowHeight = 18f
        fillPaint.color = COLOR_BG_LIGHT
        val headerRect = RectF(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y + rowHeight)
        canvas.drawRect(headerRect, fillPaint)
        canvas.drawRect(headerRect, strokePaint)

        val colX = floatArrayOf(
            MARGIN_HORIZONTAL + 8f,
            MARGIN_HORIZONTAL + 100f,
            MARGIN_HORIZONTAL + 200f,
            MARGIN_HORIZONTAL + 290f,
            MARGIN_HORIZONTAL + 380f
        )

        boldTextPaint.color = COLOR_TEXT_PRIMARY
        boldTextPaint.textSize = 7.5f
        canvas.drawText(context.getString(R.string.pdf_col_start), colX[0], y + 12f, boldTextPaint)
        canvas.drawText(context.getString(R.string.pdf_col_end), colX[1], y + 12f, boldTextPaint)
        canvas.drawText(context.getString(R.string.pdf_col_length), colX[2], y + 12f, boldTextPaint)
        canvas.drawText(context.getString(R.string.pdf_col_period), colX[3], y + 12f, boldTextPaint)
        canvas.drawText(context.getString(R.string.pdf_col_ovulation), colX[4], y + 12f, boldTextPaint)

        y += rowHeight

        textPaint.textSize = 8f
        textPaint.color = COLOR_TEXT_PRIMARY

        (listOfNotNull(ongoingCycle) + cycles).distinctBy { it.startDate }.forEach { cycle ->
            val rowRect = RectF(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y + rowHeight)
            canvas.drawRect(rowRect, strokePaint)

            canvas.drawText(cycle.startDate.format(dateFormatter), colX[0], y + 12f, textPaint)
            canvas.drawText(cycle.endDate?.format(dateFormatter) ?: context.getString(R.string.pdf_value_ongoing), colX[1], y + 12f, textPaint)
            canvas.drawText(context.getString(R.string.pdf_value_days, (cycle.cycleLengthDays ?: "-").toString()), colX[2], y + 12f, textPaint)
            canvas.drawText(context.getString(R.string.pdf_value_days, cycle.periodDurationDays.toString()), colX[3], y + 12f, textPaint)
            canvas.drawText(cycle.confirmedOvulationDate?.format(dateFormatter) ?: context.getString(R.string.report_value_not_recorded), colX[4], y + 12f, textPaint)

            y += rowHeight
        }

        return y + 16f
    }

    /**
     * Line chart of the recent basal temperatures with the follicular coverline, so the biphasic
     * shift is visible at a glance instead of having to be read out of a table.
     */
    private fun drawBbtChart(canvas: Canvas, startY: Float, logs: List<DailyLogEntity>): Float {
        val readings = logs.filter { it.basalBodyTempCelsius != null }.takeLast(28)
        if (readings.size < 3) return startY

        var y = startY
        canvas.drawText(context.getString(R.string.pdf_section_bbt_chart), MARGIN_HORIZONTAL, y, sectionHeadingPaint)
        y += 8f

        val chartHeight = 84f
        val rect = RectF(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y + chartHeight)
        fillPaint.color = COLOR_BG_LIGHT
        canvas.drawRoundRect(rect, 4f, 4f, fillPaint)
        canvas.drawRoundRect(rect, 4f, 4f, strokePaint)

        val values = readings.mapNotNull { it.basalBodyTempCelsius }
        val min = (values.minOrNull() ?: 36.0) - 0.05
        val max = (values.maxOrNull() ?: 37.0) + 0.05
        val span = (max - min).coerceAtLeast(0.1)
        val innerLeft = rect.left + 8f
        val innerWidth = rect.width() - 16f
        val innerTop = rect.top + 8f
        val innerHeight = chartHeight - 20f

        fun xOf(index: Int): Float =
            innerLeft + if (readings.size <= 1) 0f else innerWidth * index / (readings.size - 1)
        fun yOf(value: Double): Float = innerTop + (innerHeight * ((max - value) / span)).toFloat()

        // coverline = mean of the follicular (cooler) half of the window
        val coverline = values.sorted().take((values.size / 2).coerceAtLeast(1)).average()
        val dash = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_ALERT_BORDER
            strokeWidth = 1f
            style = Paint.Style.STROKE
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(4f, 3f), 0f)
        }
        canvas.drawLine(innerLeft, yOf(coverline), innerLeft + innerWidth, yOf(coverline), dash)

        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            strokeWidth = 1.4f
            style = Paint.Style.STROKE
        }
        fillPaint.color = COLOR_TEXT_PRIMARY
        readings.forEachIndexed { index, log ->
            val value = log.basalBodyTempCelsius ?: return@forEachIndexed
            val x = xOf(index)
            val pointY = yOf(value)
            if (index > 0) {
                val previous = readings.take(index).lastOrNull { it.basalBodyTempCelsius != null }?.basalBodyTempCelsius
                if (previous != null) {
                    canvas.drawLine(xOf(index - 1), yOf(previous), x, pointY, line)
                }
            }
            canvas.drawCircle(x, pointY, 1.6f, fillPaint)
        }

        textPaint.color = COLOR_TEXT_MUTED
        textPaint.textSize = 7f
        canvas.drawText(
            context.getString(R.string.pdf_btb_caption, String.format(Locale.US, "%.2f", coverline), readings.size),
            innerLeft,
            rect.bottom - 5f,
            textPaint
        )
        textPaint.color = COLOR_TEXT_PRIMARY

        return y + chartHeight + 16f
    }

    /** Second page: the daily logs behind every number above (most recent 30 days). */
    private fun drawDailyLogAppendix(canvas: Canvas, logs: List<DailyLogEntity>) {
        var y = 40f
        canvas.drawText(context.getString(R.string.pdf_title), MARGIN_HORIZONTAL, y, titlePaint)
        y += 16f
        canvas.drawText(context.getString(R.string.pdf_section_daily_logs), MARGIN_HORIZONTAL, y, sectionHeadingPaint)
        y += 6f
        textPaint.color = COLOR_TEXT_MUTED
        textPaint.textSize = 7.5f
        canvas.drawText(context.getString(R.string.pdf_daily_logs_note), MARGIN_HORIZONTAL, y + 6f, textPaint)
        textPaint.color = COLOR_TEXT_PRIMARY
        y += 14f

        val columns = floatArrayOf(
            MARGIN_HORIZONTAL + 8f,
            MARGIN_HORIZONTAL + 110f,
            MARGIN_HORIZONTAL + 210f,
            MARGIN_HORIZONTAL + 320f,
            MARGIN_HORIZONTAL + 420f
        )
        val rowHeight = 16f

        fillPaint.color = COLOR_BG_LIGHT
        val header = RectF(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y + rowHeight)
        canvas.drawRect(header, fillPaint)
        canvas.drawRect(header, strokePaint)
        boldTextPaint.textSize = 7.5f
        boldTextPaint.color = COLOR_TEXT_PRIMARY
        canvas.drawText(context.getString(R.string.pdf_col_date), columns[0], y + 11f, boldTextPaint)
        canvas.drawText(context.getString(R.string.pdf_col_flow), columns[1], y + 11f, boldTextPaint)
        canvas.drawText(context.getString(R.string.pdf_col_temp), columns[2], y + 11f, boldTextPaint)
        canvas.drawText(context.getString(R.string.pdf_col_pain), columns[3], y + 11f, boldTextPaint)
        canvas.drawText(context.getString(R.string.pdf_column_mucus), columns[4], y + 11f, boldTextPaint)
        y += rowHeight

        textPaint.textSize = 8f
        textPaint.color = COLOR_TEXT_PRIMARY
        logs.takeLast(30).reversed().forEach { log ->
            val row = RectF(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y + rowHeight)
            canvas.drawRect(row, strokePaint)
            canvas.drawText(log.date.format(dateFormatter), columns[0], y + 11f, textPaint)
            canvas.drawText(flowLabel(log.flow), columns[1], y + 11f, textPaint)
            canvas.drawText(
                log.basalBodyTempCelsius?.let { String.format(Locale.US, "%.2f", it) }
                    ?: "-",
                columns[2], y + 11f, textPaint
            )
            canvas.drawText("${log.painVasScore}/10", columns[3], y + 11f, textPaint)
            canvas.drawText(mucusLabel(log.cervicalMucus), columns[4], y + 11f, textPaint)
            y += rowHeight
        }
    }

    private fun flowLabel(flow: com.app.cyclejournal.data.local.entity.FlowIntensity): String =
        context.getString(
            when (flow) {
                com.app.cyclejournal.data.local.entity.FlowIntensity.NONE -> R.string.v4_log_flow_none
                com.app.cyclejournal.data.local.entity.FlowIntensity.SPOTTING -> R.string.v4_log_flow_spotting
                com.app.cyclejournal.data.local.entity.FlowIntensity.LIGHT -> R.string.v4_log_flow_light
                com.app.cyclejournal.data.local.entity.FlowIntensity.MEDIUM -> R.string.v4_log_flow_medium
                com.app.cyclejournal.data.local.entity.FlowIntensity.HEAVY -> R.string.v4_log_flow_heavy
            }
        )

    private fun mucusLabel(mucus: com.app.cyclejournal.data.local.entity.CervicalMucusType): String =
        context.getString(
            when (mucus) {
                com.app.cyclejournal.data.local.entity.CervicalMucusType.NONE -> R.string.v4_log_mucus_none
                com.app.cyclejournal.data.local.entity.CervicalMucusType.DRY -> R.string.v4_log_mucus_dry
                com.app.cyclejournal.data.local.entity.CervicalMucusType.STICKY -> R.string.v4_log_mucus_sticky
                com.app.cyclejournal.data.local.entity.CervicalMucusType.CREAMY -> R.string.v4_log_mucus_creamy
                com.app.cyclejournal.data.local.entity.CervicalMucusType.WATERY -> R.string.v4_log_mucus_watery
                com.app.cyclejournal.data.local.entity.CervicalMucusType.EGG_WHITE -> R.string.v4_log_mucus_egg
            }
        )

    private fun drawDoctorNotesSection(canvas: Canvas, startY: Float) {
        var y = startY
        canvas.drawText(context.getString(R.string.pdf_section_doctor_notes), MARGIN_HORIZONTAL, y, sectionHeadingPaint)
        y += 8f

        val boxHeight = 90f
        val rect = RectF(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y + boxHeight)
        canvas.drawRoundRect(rect, 4f, 4f, strokePaint)

        textPaint.color = COLOR_TEXT_MUTED
        textPaint.textSize = 7.5f
        canvas.drawText(context.getString(R.string.pdf_doctor_notes_prompt), MARGIN_HORIZONTAL + 10f, y + 16f, textPaint)

        // Signature line in bottom right corner
        val sigLineStartX = MARGIN_HORIZONTAL + CONTENT_WIDTH - 150f
        val sigLineEndX = MARGIN_HORIZONTAL + CONTENT_WIDTH - 20f
        val sigLineY = y + boxHeight - 24f
        canvas.drawLine(sigLineStartX, sigLineY, sigLineEndX, sigLineY, strokePaint)

        val sigText = context.getString(R.string.pdf_doctor_signature)
        val textWidth = textPaint.measureText(sigText)
        val textStartX = sigLineStartX + ((sigLineEndX - sigLineStartX - textWidth) / 2)
        canvas.drawText(sigText, textStartX, sigLineY + 14f, textPaint)
    }
}
