package com.app.cyclejournal.export.csv

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.app.cyclejournal.R
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.export.pdf.PdfShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility generating standard RFC 4180 CSV exports prefixed with a UTF-8 Byte Order Mark (\uFEFF)
 * for seamless compatibility with Microsoft Excel and Google Sheets, saved directly to Downloads.
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
        val fileName = "Data_CycleJournal_${LocalDate.now().toString().replace("-", "")}_${System.currentTimeMillis().toString().takeLast(4)}.csv"
        val csvFile = File(cacheDir, fileName)

        FileOutputStream(csvFile).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                // Write UTF-8 Byte Order Mark (BOM)
                writer.write("\uFEFF")

                // Section 1: Cycle Records
                writer.write(context.getString(R.string.export_csv_section_cycles) + "\n")
                writer.write(context.getString(R.string.export_csv_header_cycles) + "\n")
                for (cycle in cycles) {
                    val row = listOf(
                        cycle.id.toString(),
                        cycle.startDate.format(dateFormatter),
                        cycle.endDate?.format(dateFormatter) ?: context.getString(R.string.export_csv_cell_ongoing),
                        cycle.cycleLengthDays?.toString() ?: "-",
                        cycle.periodDurationDays.toString(),
                        cycle.confirmedOvulationDate?.format(dateFormatter) ?: context.getString(R.string.export_csv_cell_not_recorded)
                    ).joinToString(",") { escapeCsv(it) }
                    writer.write(row + "\n")
                }

                writer.write("\n")

                // Section 2: Daily Biomarker Logs
                writer.write(context.getString(R.string.export_csv_section_logs) + "\n")
                writer.write(context.getString(R.string.export_csv_header_logs) + "\n")
                for (log in logs) {
                    val row = listOf(
                        log.date.format(dateFormatter),
                        log.flow.name,
                        log.basalBodyTempCelsius?.let { String.format(Locale.US, "%.2f", it) } ?: "-",
                        log.cervicalMucus.name,
                        log.painVasScore.toString(),
                        log.painLocation ?: "-",
                        if (log.takenAnalgesic) context.getString(R.string.export_csv_cell_yes) else context.getString(R.string.export_csv_cell_no),
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

    fun saveCsvToDownloads(context: Context, sourceCsvFile: File): PdfShareHelper.SaveResult {
        var publicUri: Uri? = null
        var savedPublic = false
        val displayName = sourceCsvFile.name

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CycleJournal")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        sourceCsvFile.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                    publicUri = uri
                    savedPublic = true
                }
            } else {
                try {
                    val downloadDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "CycleJournal")
                    downloadDir.mkdirs()
                    val destFile = File(downloadDir, displayName)
                    sourceCsvFile.copyTo(destFile, overwrite = true)
                    publicUri = Uri.fromFile(destFile)
                    savedPublic = true
                } catch (e: Exception) {
                    val fallbackDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "CycleJournal")
                    fallbackDir.mkdirs()
                    val destFile = File(fallbackDir, displayName)
                    sourceCsvFile.copyTo(destFile, overwrite = true)
                    publicUri = Uri.fromFile(destFile)
                    savedPublic = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return PdfShareHelper.SaveResult(
            publicUri = publicUri,
            localFile = sourceCsvFile,
            fileName = displayName,
            savedToPublicDownload = savedPublic
        )
    }

    fun openCsv(context: Context, saveResult: PdfShareHelper.SaveResult) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            if (saveResult.publicUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setDataAndType(saveResult.publicUri, "text/csv")
            } else {
                val authority = "${context.packageName}.fileprovider"
                val contentUri = FileProvider.getUriForFile(context, authority, saveResult.localFile)
                setDataAndType(contentUri, "text/csv")
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.export_csv_empty_reader_toast), Toast.LENGTH_SHORT).show()
        }
    }

    fun shareCsv(context: Context, saveResult: PdfShareHelper.SaveResult, title: String? = null) {
        val shareTitle = title ?: context.getString(R.string.export_csv_share_title)
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, saveResult.localFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, shareTitle)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newRawUri(shareTitle, contentUri)
        }

        val chooser = Intent.createChooser(shareIntent, context.getString(R.string.export_csv_share_chooser_title))
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    suspend fun exportAndShareCsv(
        context: Context,
        cycles: List<CycleEntity>,
        logs: List<DailyLogEntity>
    ) {
        val file = generateCsvFile(context, cycles, logs)
        val saveResult = saveCsvToDownloads(context, file)
        withContext(Dispatchers.Main) {
            shareCsv(context, saveResult)
        }
    }
}
