package com.app.cyclejournal.domain.manager

import android.content.Context
import com.app.cyclejournal.data.local.AppDatabase
import com.app.cyclejournal.data.remote.CloudflareBackupClient
import com.app.cyclejournal.scheduler.alarm.CycleAlarmScheduler
import com.app.cyclejournal.scheduler.worker.BackupScheduler
import com.app.cyclejournal.security.DatabaseKeyManager
import com.app.cyclejournal.security.SecurityPinManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.KeyStore

/**
 * Orchestrates the irreversible "Nuke Option" (GDPR / UU PDP Right to be Forgotten):
 * 1. Purges remote ciphertext blob from Cloudflare D1
 * 2. Cancels all scheduled AlarmManager reminders and WorkManager periodic workers
 * 3. Closes Room and deletes local encrypted SQLCipher database files (.db, -wal, -shm)
 * 4. Recursively purges temporary and permanent report files
 * 5. Wipes all SharedPreferences instances
 * 6. Destroys hardware-backed master keys in Android Keystore
 */
class DataWipeManager(
    private val context: Context,
    private val database: AppDatabase,
    private val pinManager: SecurityPinManager,
    private val databaseKeyManager: DatabaseKeyManager,
    private val alarmScheduler: CycleAlarmScheduler,
    private val backupClient: CloudflareBackupClient
) {

    suspend fun executeCompleteWipe(purgeRemoteCloud: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Purge remote Cloudflare D1 ciphertext before local identifiers are removed
            if (purgeRemoteCloud) {
                val userId = pinManager.getOrCreateAnonymousUserId()
                try {
                    backupClient.purgeRemoteCloudData(userId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 2. Cancel background alarm reminders and WorkManager tasks
            alarmScheduler.cancelBbtReminder()
            alarmScheduler.cancelPeriodAlert()
            BackupScheduler.cancelBackup(context)

            // 3. Close database and delete SQLCipher database files
            AppDatabase.closeAndResetInstance()
            context.deleteDatabase(AppDatabase.DATABASE_NAME)
            context.deleteDatabase("${AppDatabase.DATABASE_NAME}-wal")
            context.deleteDatabase("${AppDatabase.DATABASE_NAME}-shm")

            // 4. Purge temporary report caches
            File(context.cacheDir, "reports").deleteRecursively()
            File(context.filesDir, "reports").deleteRecursively()

            // 5. Wipe all SharedPreferences
            context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE).edit().clear().commit()
            context.getSharedPreferences("db_key_secure_storage", Context.MODE_PRIVATE).edit().clear().commit()
            context.getSharedPreferences("secure_user_pin_prefs", Context.MODE_PRIVATE).edit().clear().commit()

            // 6. Destroy Android Keystore hardware keys
            databaseKeyManager.destroyDatabaseKey()
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                val aliases = keyStore.aliases()
                while (aliases.hasMoreElements()) {
                    val alias = aliases.nextElement()
                    if (alias.startsWith("cycle_journal_") || alias.contains("master_key")) {
                        keyStore.deleteEntry(alias)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
