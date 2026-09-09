package com.app.cyclejournal.export.csv

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility generating standard RFC 4180 CSV exports prefixed with a UTF-8 Byte Order Mark (\uFEFF)
 * for seamless compatibility with Microsoft Excel and Google Sheets.
 */
object CsvExportHelper {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun generateCsvFile(
        context: Context,
        cycles: List<CycleEntity>,
        logs: List<DailyLogEntity>
    ): File = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "reports")
        cacheDir.mkdirs()
        val csvFile = File(cacheDir, "CycleJournal_Data_${System.currentTimeMillis()}.csv")

        FileOutputStream(csvFile).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                // Write UTF-8 Byte Order Mark (BOM)
                writer.write("\uFEFF")

                // Section 1: Cycle Records
                writer.write("# RIWAYAT SIKLUS MENSTRUASI\n")
                writer.write("ID,Tanggal Mulai,Tanggal Akhir,Panjang Siklus (Hari),Durasi Haid (Hari),Estimasi Ovulasi\n")
                for (cycle in cycles) {
                    val row = listOf(
                        cycle.id.toString(),
                        cycle.startDate.format(dateFormatter),
                        cycle.endDate?.format(dateFormatter) ?: "Berjalan",
                        cycle.cycleLengthDays?.toString() ?: "-",
                        cycle.periodDurationDays.toString(),
                        cycle.confirmedOvulationDate?.format(dateFormatter) ?: "Tidak tercatat"
                    ).joinToString(",") { escapeCsv(it) }
                    writer.write(row + "\n")
                }

                writer.write("\n")

                // Section 2: Daily Biomarker Logs
                writer.write("# LOG HARIAN BIOMARKER & GEJALA\n")
                writer.write("Tanggal,Intensitas Aliran (Flow),Suhu Basal BBT (°C),Karakteristik Lendir Serviks,Skala Nyeri (VAS),Lokasi Nyeri,Konsumsi Analgesik,Catatan Tambahan\n")
                for (log in logs) {
                    val row = listOf(
                        log.date.format(dateFormatter),
                        log.flow.name,
                        log.basalBodyTempCelsius?.let { String.format(Locale.US, "%.2f", it) } ?: "-",
                        log.cervicalMucus.name,
                        log.painVasScore.toString(),
                        log.painLocation ?: "-",
                        if (log.takenAnalgesic) "Ya" else "Tidak",
                        log.notes ?: ""
                    ).joinToString(",") { escapeCsv(it) }
                    writer.write(row + "\n")
                }
            }
        }

        csvFile
    }

    fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    suspend fun exportAndShareCsv(
        context: Context,
        cycles: List<CycleEntity>,
        logs: List<DailyLogEntity>
    ) {
        val file = generateCsvFile(context, cycles, logs)
        withContext(Dispatchers.Main) {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Ekspor Data CycleJournal (CSV)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newRawUri("CycleJournal CSV", uri)
            }

            val chooser = Intent.createChooser(shareIntent, "Bagikan Berkas CSV via...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }
}
