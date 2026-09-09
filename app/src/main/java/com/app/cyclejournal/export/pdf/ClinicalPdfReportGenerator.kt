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
        anomalies: List<AnomalyAlert>
    ): File = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var currentY = 40f

        // 1. Header with Official Logo Emblem & Patient Identifier
        currentY = drawHeader(canvas, currentY, patientIdentifier)

        // 2. FIGO Cycle Metrics Summary Grid Box
        currentY = drawMetricsSummary(canvas, currentY, stats)

        // 3. Clinical Red Flags & Anomalies Section
        currentY = drawAnomaliesSection(canvas, currentY, anomalies)

        // 4. Historical Cycle Log Table (Up to 6 completed cycles)
        currentY = drawCycleHistoryTable(canvas, currentY, cycles.take(6))

        // 5. Doctor's Clinical Verification & Stamp Section
        drawDoctorNotesSection(canvas, currentY)

        document.finishPage(page)

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
        canvas.drawText("LAPORAN KLINIS SIKLUS MENSTRUASI & BIOMARKER", textStartX, y + 14f, titlePaint)

        textPaint.color = COLOR_TEXT_MUTED
        textPaint.textSize = 8f
        canvas.drawText("Dokumen rekam mandiri untuk evaluasi ginekologi & rujukan SpOG (FIGO Standard)", textStartX, y + 26f, textPaint)

        y += 44f
        // Horizontal divider line
        canvas.drawLine(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y, strokePaint)
        y += 14f

        textPaint.color = COLOR_TEXT_PRIMARY
        textPaint.textSize = 8.5f
        canvas.drawText("ID Pasien / Anonim: $patientId", MARGIN_HORIZONTAL, y, boldTextPaint)

        val dateText = "Tanggal Ekspor: ${LocalDate.now().format(dateFormatter)}"
        val dateWidth = textPaint.measureText(dateText)
        canvas.drawText(dateText, (MARGIN_HORIZONTAL + CONTENT_WIDTH) - dateWidth, y, textPaint)

        return y + 18f
    }

    private fun drawMetricsSummary(canvas: Canvas, startY: Float, stats: CycleStats?): Float {
        var y = startY
        canvas.drawText("1. RINGKASAN METRIK SIKLUS (FIGO STANDARD)", MARGIN_HORIZONTAL, y, sectionHeadingPaint)
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
            "Rata-rata Siklus" to "${String.format(Locale.US, "%.1f", stats?.averageLength ?: 0.0)} Hari",
            "Variabilitas (SD σ)" to "±${String.format(Locale.US, "%.1f", stats?.standardDeviation ?: 0.0)} Hari",
            "Rentang (Min-Max)" to "${stats?.minLength ?: 0} - ${stats?.maxLength ?: 0} Hari",
            "Rata-rata Durasi Haid" to "${String.format(Locale.US, "%.1f", stats?.averagePeriodDuration ?: 0.0)} Hari"
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

        return y + boxHeight + 16f
    }

    private fun drawAnomaliesSection(canvas: Canvas, startY: Float, anomalies: List<AnomalyAlert>): Float {
        var y = startY
        canvas.drawText("2. INDIKASI ANOMALI & RED FLAGS KLINIS", MARGIN_HORIZONTAL, y, sectionHeadingPaint)
        y += 8f

        if (anomalies.isEmpty()) {
            val emptyRect = RectF(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y + 24f)
            fillPaint.color = COLOR_BG_LIGHT
            canvas.drawRoundRect(emptyRect, 4f, 4f, fillPaint)
            canvas.drawRoundRect(emptyRect, 4f, 4f, strokePaint)

            textPaint.color = COLOR_TEXT_MUTED
            textPaint.textSize = 8f
            canvas.drawText("Tidak ada anomali atau deviasi signifikan yang terdeteksi pada periode ini.", MARGIN_HORIZONTAL + 10f, y + 15f, textPaint)
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
            canvas.drawText("[!] ${alert.type.code} - ${alert.type.description}", MARGIN_HORIZONTAL + 8f, y + 11f, boldTextPaint)

            textPaint.color = COLOR_ALERT_TEXT
            textPaint.textSize = 7.5f
            canvas.drawText("Detail: ${alert.details} (${alert.detectedDate.format(dateFormatter)})", MARGIN_HORIZONTAL + 8f, y + 21f, textPaint)

            y += 30f
        }

        return y + 6f
    }

    private fun drawCycleHistoryTable(canvas: Canvas, startY: Float, cycles: List<CycleEntity>): Float {
        var y = startY
        canvas.drawText("3. LOG HISTORIS SIKLUS (MAKS. 6 SIKLUS TERAKHIR)", MARGIN_HORIZONTAL, y, sectionHeadingPaint)
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
        canvas.drawText("TANGGAL AWAL", colX[0], y + 12f, boldTextPaint)
        canvas.drawText("TANGGAL AKHIR", colX[1], y + 12f, boldTextPaint)
        canvas.drawText("PANJANG SIKLUS", colX[2], y + 12f, boldTextPaint)
        canvas.drawText("DURASI HAID", colX[3], y + 12f, boldTextPaint)
        canvas.drawText("ESTIMASI OVULASI", colX[4], y + 12f, boldTextPaint)

        y += rowHeight

        textPaint.textSize = 8f
        textPaint.color = COLOR_TEXT_PRIMARY

        cycles.forEach { cycle ->
            val rowRect = RectF(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y + rowHeight)
            canvas.drawRect(rowRect, strokePaint)

            canvas.drawText(cycle.startDate.format(dateFormatter), colX[0], y + 12f, textPaint)
            canvas.drawText(cycle.endDate?.format(dateFormatter) ?: "Berjalan", colX[1], y + 12f, textPaint)
            canvas.drawText("${cycle.cycleLengthDays ?: "-"} Hari", colX[2], y + 12f, textPaint)
            canvas.drawText("${cycle.periodDurationDays} Hari", colX[3], y + 12f, textPaint)
            canvas.drawText(cycle.confirmedOvulationDate?.format(dateFormatter) ?: "Tidak tercatat", colX[4], y + 12f, textPaint)

            y += rowHeight
        }

        return y + 16f
    }

    private fun drawDoctorNotesSection(canvas: Canvas, startY: Float) {
        var y = startY
        canvas.drawText("4. CATATAN & VERIFIKASI KLINIS DOKTER (SpOG)", MARGIN_HORIZONTAL, y, sectionHeadingPaint)
        y += 8f

        val boxHeight = 90f
        val rect = RectF(MARGIN_HORIZONTAL, y, MARGIN_HORIZONTAL + CONTENT_WIDTH, y + boxHeight)
        canvas.drawRoundRect(rect, 4f, 4f, strokePaint)

        textPaint.color = COLOR_TEXT_MUTED
        textPaint.textSize = 7.5f
        canvas.drawText("Diagnosa Medis / Rekomendasi Terapi:", MARGIN_HORIZONTAL + 10f, y + 16f, textPaint)

        // Signature line in bottom right corner
        val sigLineStartX = MARGIN_HORIZONTAL + CONTENT_WIDTH - 150f
        val sigLineEndX = MARGIN_HORIZONTAL + CONTENT_WIDTH - 20f
        val sigLineY = y + boxHeight - 24f
        canvas.drawLine(sigLineStartX, sigLineY, sigLineEndX, sigLineY, strokePaint)

        val sigText = "Tanda Tangan & Cap Dokter"
        val textWidth = textPaint.measureText(sigText)
        val textStartX = sigLineStartX + ((sigLineEndX - sigLineStartX - textWidth) / 2)
        canvas.drawText(sigText, textStartX, sigLineY + 14f, textPaint)
    }
}
