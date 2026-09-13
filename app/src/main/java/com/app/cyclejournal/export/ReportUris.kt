package com.app.cyclejournal.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Resolves a shareable content URI for a generated file.
 *
 * `FileProvider.getUriForFile` throws when the file sits outside every root declared in
 * `res/xml/file_paths.xml` - which used to crash the app while sharing a backup. Callers get null
 * instead and can tell the user something useful.
 */
object ReportUris {

    fun contentUri(context: Context, file: File): Uri? = try {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        null
    }
}
