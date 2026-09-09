package com.app.cyclejournal.export.pdf

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Utility helper converting generated PDF files to content:// URIs and launching the Android Sharesheet.
 */
object PdfShareHelper {

    /**
     * Dispatches an Android Sharesheet Intent granting transient read permissions to recipient apps.
     */
    fun sharePdf(context: Context, pdfFile: File, title: String = "Laporan Klinis Siklus & SpOG") {
        if (!pdfFile.exists()) return

        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, pdfFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // ClipData ensures permission persistence on Android 10+
            clipData = ClipData.newRawUri(title, contentUri)
        }

        val chooser = Intent.createChooser(shareIntent, "Bagikan Laporan Medis via...")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Directly opens the PDF in an external viewer if available.
     */
    fun viewPdfDirectly(context: Context, pdfFile: File) {
        if (!pdfFile.exists()) return

        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, pdfFile)

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(viewIntent)
    }
}
