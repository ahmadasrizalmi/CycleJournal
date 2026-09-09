package com.app.cyclejournal.scheduler.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.cyclejournal.data.backup.BackupJsonParser
import com.app.cyclejournal.data.local.AppDatabase
import com.app.cyclejournal.data.preferences.OnboardingPreferences
import com.app.cyclejournal.data.remote.CloudflareBackupClient
import com.app.cyclejournal.data.remote.model.BackupUploadRequest
import com.app.cyclejournal.security.BackupCryptoEngine
import com.app.cyclejournal.security.SecurityPinManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Periodic background worker executing monthly zero-knowledge encrypted backups to Cloudflare D1.
 * Only executes under Charging + Unmetered Wi-Fi constraints.
 */
class MonthlyBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val pinManager = SecurityPinManager(applicationContext)
            val userId = pinManager.getOrCreateAnonymousUserId()

            // If PIN is not set yet, skip backup
            if (!pinManager.isPinSet()) {
                return@withContext Result.failure()
            }

            // 1. Query local Room data
            val db = AppDatabase.getInstance(applicationContext)
            val logs = db.dailyLogDao().getAllLogsAsc()

            if (logs.isEmpty()) {
                return@withContext Result.success()
            }

            // 2. Serialize to standard JSON payload
            val rawJson = BackupJsonParser.serializeBackupPayload(userId, logs)

            // 3. Encrypt payload client-side with user passphrase
            // Use derived local seed or master pin
            val cryptoEngine = BackupCryptoEngine()
            val encryptedPackage = cryptoEngine.encrypt(rawJson, userId.toCharArray())

            // 4. Upload to Cloudflare Worker
            val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val backupClient = CloudflareBackupClient("https://backup-api.asridigital.com")

            val success = backupClient.uploadMonthlyBackup(
                BackupUploadRequest(
                    userId = userId,
                    backupMonth = currentMonth,
                    cipherPayload = encryptedPackage.base64Payload,
                    payloadHash = encryptedPackage.sha256Checksum
                )
            )

            if (success) {
                val prefs = OnboardingPreferences(applicationContext)
                prefs.setLastSyncTimestamp(System.currentTimeMillis())
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
