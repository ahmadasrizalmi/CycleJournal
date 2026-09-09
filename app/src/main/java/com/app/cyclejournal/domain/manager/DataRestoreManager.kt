package com.app.cyclejournal.domain.manager

import com.app.cyclejournal.data.backup.BackupJsonParser
import com.app.cyclejournal.data.local.AppDatabase
import com.app.cyclejournal.data.remote.CloudflareBackupClient
import com.app.cyclejournal.data.remote.model.CloudRestoreResult
import com.app.cyclejournal.domain.engine.CycleAggregator
import com.app.cyclejournal.security.BackupCryptoEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.crypto.AEADBadTagException

sealed class DataRestoreOutcome {
    data class Success(val logsRestored: Int, val cyclesRestored: Int) : DataRestoreOutcome()
    object InvalidPassphrase : DataRestoreOutcome()
    object IntegrityCheckFailed : DataRestoreOutcome()
    object BackupNotFound : DataRestoreOutcome()
    data class NetworkError(val message: String) : DataRestoreOutcome()
}

/**
 * Orchestrator for cloud disaster recovery:
 * 1. Downloads encrypted blob from Cloudflare Worker
 * 2. Verifies SHA-256 integrity checksum
 * 3. Decrypts via AES-256-GCM catching invalid PIN (AEADBadTagException)
 * 4. Deserializes JSON daily logs and batch upserts to Room
 * 5. Triggers CycleAggregator full retroactive history reconstruction
 */
class DataRestoreManager(
    private val database: AppDatabase,
    private val cycleAggregator: CycleAggregator,
    private val backupClient: CloudflareBackupClient,
    private val cryptoEngine: BackupCryptoEngine
) {

    suspend fun executeRestore(
        userId: String,
        passphrase: CharArray,
        specificMonth: String? = null
    ): DataRestoreOutcome = withContext(Dispatchers.IO) {
        // 1. Fetch remote ciphertext package
        val remoteResult = backupClient.fetchBackup(userId, specificMonth)

        val backupItem = when (remoteResult) {
            is CloudRestoreResult.Success -> remoteResult.packageItem
            is CloudRestoreResult.AvailableMonths -> {
                val latestMonth = remoteResult.months.firstOrNull()
                    ?: return@withContext DataRestoreOutcome.BackupNotFound
                val secondFetch = backupClient.fetchBackup(userId, latestMonth)
                if (secondFetch is CloudRestoreResult.Success) {
                    secondFetch.packageItem
                } else {
                    return@withContext DataRestoreOutcome.BackupNotFound
                }
            }
            is CloudRestoreResult.NotFound -> return@withContext DataRestoreOutcome.BackupNotFound
            is CloudRestoreResult.Error -> return@withContext DataRestoreOutcome.NetworkError(remoteResult.message)
        }

        // 2. Verify SHA-256 integrity
        val calculatedChecksum = cryptoEngine.calculateSha256(backupItem.cipherPayload)
        if (!calculatedChecksum.equals(backupItem.payloadHash, ignoreCase = true)) {
            return@withContext DataRestoreOutcome.IntegrityCheckFailed
        }

        // 3. Decrypt ciphertext using passphrase
        val decryptedJson = try {
            cryptoEngine.decrypt(backupItem.cipherPayload, passphrase)
        } catch (e: AEADBadTagException) {
            return@withContext DataRestoreOutcome.InvalidPassphrase
        } catch (e: Exception) {
            return@withContext DataRestoreOutcome.InvalidPassphrase
        }

        // 4. Parse JSON into DailyLogEntity records
        val restoredLogs = try {
            BackupJsonParser.parseDailyLogs(decryptedJson)
        } catch (e: Exception) {
            return@withContext DataRestoreOutcome.NetworkError("Gagal membaca struktur berkas cadangan: ${e.message}")
        }

        if (restoredLogs.isEmpty()) {
            return@withContext DataRestoreOutcome.Success(0, 0)
        }

        // 5. Batch upsert into Room database
        val dailyLogDao = database.dailyLogDao()
        for (log in restoredLogs) {
            dailyLogDao.upsertDailyLog(log)
        }

        // 6. Trigger retroactive cycle reconciliation
        cycleAggregator.reconcileAllHistory()

        val cyclesCount = database.cycleDao().getAllCycles().size
        DataRestoreOutcome.Success(restoredLogs.size, cyclesCount)
    }
}
