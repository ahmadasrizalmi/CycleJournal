package com.app.cyclejournal.export.pdf

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.app.cyclejournal.R
import com.app.cyclejournal.export.ReportUris
import java.io.File

/**
 * Utility helper saving generated PDF reports directly to the public Download folder
 * and providing Open & Share intents.
 */
object PdfShareHelper {

    data class SaveResult(
        val publicUri: Uri?,
        val localFile: File,
        val fileName: String,
        val savedToPublicDownload: Boolean
    )

    /**
     * Saves generated PDF file into public Download/CycleJournal/ folder via MediaStore (Android 10+)
     * or standard Downloads directory (Android 8-9).
     */
    fun saveToDownloads(context: Context, sourcePdfFile: File, displayName: String): SaveResult {
        var publicUri: Uri? = null
        var savedPublic = false

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CycleJournal")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        sourcePdfFile.inputStream().use { input ->
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
                    sourcePdfFile.copyTo(destFile, overwrite = true)
                    publicUri = Uri.fromFile(destFile)
                    savedPublic = true
                } catch (e: Exception) {
                    val fallbackDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "CycleJournal")
                    fallbackDir.mkdirs()
                    val destFile = File(fallbackDir, displayName)
                    sourcePdfFile.copyTo(destFile, overwrite = true)
                    publicUri = Uri.fromFile(destFile)
                    savedPublic = false // app-private folder, not the public Downloads
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return SaveResult(
            publicUri = publicUri,
            localFile = sourcePdfFile,
            fileName = displayName,
            savedToPublicDownload = savedPublic
        )
    }

    /**
     * Directly opens the PDF in an external viewer.
     */
    fun openPdf(context: Context, saveResult: SaveResult) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            if (saveResult.publicUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setDataAndType(saveResult.publicUri, "application/pdf")
            } else {
                val contentUri = ReportUris.contentUri(context, saveResult.localFile)
                if (contentUri == null) {
                    Toast.makeText(context, context.getString(R.string.export_pdf_empty_reader_toast), Toast.LENGTH_SHORT).show()
                    return
                }
                setDataAndType(contentUri, "application/pdf")
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.export_pdf_empty_reader_toast), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Dispatches an Android Sharesheet Intent granting transient read permissions to recipient apps.
     */
    fun sharePdf(context: Context, pdfFile: File, title: String? = null) {
        if (!pdfFile.exists()) return

        val shareTitle = title ?: context.getString(R.string.export_pdf_share_title)
        val contentUri = ReportUris.contentUri(context, pdfFile) ?: return

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, shareTitle)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // ClipData ensures permission persistence on Android 10+
            clipData = ClipData.newRawUri(shareTitle, contentUri)
        }

        val chooser = Intent.createChooser(shareIntent, context.getString(R.string.export_pdf_share_chooser_title))
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Directly opens the PDF in an external viewer if available.
     */
    fun viewPdfDirectly(context: Context, pdfFile: File) {
        if (!pdfFile.exists()) return

        val contentUri = ReportUris.contentUri(context, pdfFile) ?: return

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(viewIntent)
    }
}
