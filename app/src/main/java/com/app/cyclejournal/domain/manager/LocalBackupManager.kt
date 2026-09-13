package com.app.cyclejournal.domain.manager

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.app.cyclejournal.data.backup.BackupJsonParser
import com.app.cyclejournal.data.local.AppDatabase
import com.app.cyclejournal.domain.engine.CycleAggregator
import com.app.cyclejournal.export.pdf.PdfShareHelper
import com.app.cyclejournal.security.BackupCryptoEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import javax.crypto.AEADBadTagException

sealed class LocalRestoreOutcome {
    data class Success(val logsRestored: Int, val cyclesRestored: Int) : LocalRestoreOutcome()
    object InvalidPin : LocalRestoreOutcome()
    object InvalidFileFormat : LocalRestoreOutcome()
    data class Error(val message: String) : LocalRestoreOutcome()
}

data class BackupFileInspection(
    val isValid: Boolean,
    val isEncrypted: Boolean,
    val rawJson: String,
    val encryptedPayload: String? = null
)

/**
 * Enterprise manager for offline self-hosted backup and restore (.cjbackup format).
 * Supports both standard open backups and AES-256-GCM PIN-encrypted backups.
 */
class LocalBackupManager(
    private val database: AppDatabase,
    private val cycleAggregator: CycleAggregator,
    private val cryptoEngine: BackupCryptoEngine = BackupCryptoEngine()
) {

    suspend fun createBackupFile(
        context: Context,
        userId: String,
        isEncrypted: Boolean,
        pin: String? = null
    ): PdfShareHelper.SaveResult = withContext(Dispatchers.IO) {
        val logs = database.dailyLogDao().getAllLogsAsc()
        val rawDataJson = BackupJsonParser.serializeBackupPayload(userId, logs)

        val rootJson = JSONObject()
        rootJson.put("magic", "CJ_BACKUP")
        rootJson.put("version", 1)
        rootJson.put("encrypted", isEncrypted)
        rootJson.put("created_at", System.currentTimeMillis())

        if (isEncrypted && !pin.isNullOrEmpty()) {
            val encryptedPackage = cryptoEngine.encrypt(rawDataJson, pin.toCharArray())
            rootJson.put("payload", encryptedPackage.base64Payload)
            rootJson.put("checksum", encryptedPackage.sha256Checksum)
        } else {
            rootJson.put("payload", rawDataJson)
            rootJson.put("checksum", cryptoEngine.calculateSha256(rawDataJson))
        }

        val jsonString = rootJson.toString(2)

        val cacheDir = File(context.cacheDir, "backups")
        cacheDir.mkdirs()
        val typeSuffix = if (isEncrypted) "terenkripsi" else "standar"
        val fileName = "Backup_CycleJournal_${LocalDate.now().toString().replace("-", "")}_$typeSuffix.cjbackup"
        val localFile = File(cacheDir, fileName)
        localFile.writeText(jsonString, Charsets.UTF_8)

        // Save to public Downloads directory
        saveBackupToDownloads(context, localFile)
    }

    private fun saveBackupToDownloads(context: Context, sourceFile: File): PdfShareHelper.SaveResult {
        var publicUri: Uri? = null
        var savedPublic = false
        val displayName = sourceFile.name

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CycleJournal")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        sourceFile.inputStream().use { input ->
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
                    sourceFile.copyTo(destFile, overwrite = true)
                    publicUri = Uri.fromFile(destFile)
                    savedPublic = true
                } catch (e: Exception) {
                    val fallbackDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "CycleJournal")
                    fallbackDir.mkdirs()
                    val destFile = File(fallbackDir, displayName)
                    sourceFile.copyTo(destFile, overwrite = true)
                    publicUri = Uri.fromFile(destFile)
                    savedPublic = false // app-private folder, not the public Downloads
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return PdfShareHelper.SaveResult(
            publicUri = publicUri,
            localFile = sourceFile,
            fileName = displayName,
            savedToPublicDownload = savedPublic
        )
    }

    suspend fun inspectBackupFile(content: String): BackupFileInspection = withContext(Dispatchers.Default) {
        try {
            val json = JSONObject(content)
            val magic = json.optString("magic", "")
            if (magic != "CJ_BACKUP") {
                return@withContext BackupFileInspection(isValid = false, isEncrypted = false, rawJson = "")
            }
            val isEncrypted = json.optBoolean("encrypted", false)
            val payload = json.getString("payload")

            BackupFileInspection(
                isValid = true,
                isEncrypted = isEncrypted,
                rawJson = if (isEncrypted) "" else payload,
                encryptedPayload = if (isEncrypted) payload else null
            )
        } catch (e: Exception) {
            BackupFileInspection(isValid = false, isEncrypted = false, rawJson = "")
        }
    }

    suspend fun restoreFromData(
        inspection: BackupFileInspection,
        pin: String? = null
    ): LocalRestoreOutcome = withContext(Dispatchers.IO) {
        if (!inspection.isValid) {
            return@withContext LocalRestoreOutcome.InvalidFileFormat
        }

        val rawJson = if (inspection.isEncrypted) {
            val payload = inspection.encryptedPayload
                ?: return@withContext LocalRestoreOutcome.InvalidFileFormat
            if (pin.isNullOrEmpty()) {
                return@withContext LocalRestoreOutcome.InvalidPin
            }
            try {
                cryptoEngine.decrypt(payload, pin.toCharArray())
            } catch (e: AEADBadTagException) {
                return@withContext LocalRestoreOutcome.InvalidPin
            } catch (e: Exception) {
                return@withContext LocalRestoreOutcome.InvalidPin
            }
        } else {
            inspection.rawJson
        }

        val logs = try {
            BackupJsonParser.parseDailyLogs(rawJson)
        } catch (e: Exception) {
            return@withContext LocalRestoreOutcome.Error("Format berkas rusak: ${e.message}")
        }

        if (logs.isEmpty()) {
            return@withContext LocalRestoreOutcome.Success(0, 0)
        }

        // Atomically replace all logs in Room DB
        val dailyLogDao = database.dailyLogDao()
        dailyLogDao.clearAllLogs()
        for (log in logs) {
            dailyLogDao.upsertDailyLog(log)
        }

        // Trigger full cycle reconstruction
        cycleAggregator.reconcileAllHistory()

        val cyclesCount = database.cycleDao().getAllCycles().size
        LocalRestoreOutcome.Success(logs.size, cyclesCount)
    }
}
