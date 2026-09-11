package com.app.cyclejournal.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cyclejournal.R
import com.app.cyclejournal.data.backup.BackupJsonParser
import com.app.cyclejournal.data.local.AppDatabase
import androidx.annotation.StringRes
import com.app.cyclejournal.data.preferences.AppLocale
import com.app.cyclejournal.data.preferences.OnboardingPreferences
import com.app.cyclejournal.data.remote.CloudflareBackupClient
import com.app.cyclejournal.data.remote.model.BackupUploadRequest
import android.net.Uri
import com.app.cyclejournal.domain.manager.BackupFileInspection
import com.app.cyclejournal.domain.manager.LocalBackupManager
import com.app.cyclejournal.domain.manager.LocalRestoreOutcome
import com.app.cyclejournal.export.pdf.PdfShareHelper
import com.app.cyclejournal.domain.manager.DataRestoreManager
import com.app.cyclejournal.domain.manager.DataRestoreOutcome
import com.app.cyclejournal.domain.manager.DataWipeManager
import com.app.cyclejournal.export.csv.CsvExportHelper
import com.app.cyclejournal.security.BackupCryptoEngine
import com.app.cyclejournal.security.SecurityPinManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed class SyncState {
    object Idle : SyncState()
    object InProgress : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val message: String) : SyncState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val database: AppDatabase,
    private val pinManager: SecurityPinManager,
    private val prefs: OnboardingPreferences,
    private val cryptoEngine: BackupCryptoEngine,
    private val backupClient: CloudflareBackupClient,
    private val restoreManager: DataRestoreManager,
    private val localBackupManager: LocalBackupManager,
    private val wipeManager: DataWipeManager
) : ViewModel() {

    /** Resolves user-facing text in the language selected inside the app. */
    private fun text(@StringRes id: Int, vararg args: Any?): String =
        AppLocale.wrap(appContext).getString(id, *args)

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    val anonymousUserId: String = pinManager.getOrCreateAnonymousUserId()

    fun getLastSyncTimestamp(): Long = prefs.getLastSyncTimestamp()

    fun performManualBackup(userPin: String) {
        if (!pinManager.verifyPin(userPin)) {
            _syncState.value = SyncState.Error(text(R.string.security_backup_invalid_pin))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _syncState.value = SyncState.InProgress
            try {
                val logs = database.dailyLogDao().getAllLogsAsc()
                val rawJson = BackupJsonParser.serializeBackupPayload(anonymousUserId, logs)
                val encryptedPackage = cryptoEngine.encrypt(rawJson, userPin.toCharArray())

                val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val success = backupClient.uploadMonthlyBackup(
                    BackupUploadRequest(
                        userId = anonymousUserId,
                        backupMonth = currentMonth,
                        cipherPayload = encryptedPackage.base64Payload,
                        payloadHash = encryptedPackage.sha256Checksum
                    )
                )

                if (success) {
                    prefs.setLastSyncTimestamp(System.currentTimeMillis())
                    _syncState.value = SyncState.Success(text(R.string.security_backup_upload_success))
                } else {
                    _syncState.value = SyncState.Error(text(R.string.security_backup_upload_failed))
                }
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.message ?: text(R.string.security_backup_error_generic))
            }
        }
    }
    fun createLocalBackup(
        context: Context,
        isEncrypted: Boolean,
        pin: String? = null,
        onComplete: (PdfShareHelper.SaveResult) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = localBackupManager.createBackupFile(context, anonymousUserId, isEncrypted, pin)
            withContext(Dispatchers.Main) {
                onComplete(result)
            }
        }
    }

    fun restoreFromBackupFile(
        context: Context,
        fileUri: Uri,
        pin: String? = null,
        onNeedsPin: (BackupFileInspection) -> Unit,
        onComplete: (LocalRestoreOutcome) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val content = try {
                context.contentResolver.openInputStream(fileUri)?.bufferedReader()?.use { it.readText() } ?: ""
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(LocalRestoreOutcome.Error(text(R.string.security_restore_file_read_failed, e.message)))
                }
                return@launch
            }

            val inspection = localBackupManager.inspectBackupFile(content)
            if (!inspection.isValid) {
                withContext(Dispatchers.Main) {
                    onComplete(LocalRestoreOutcome.InvalidFileFormat)
                }
                return@launch
            }

            if (inspection.isEncrypted && pin.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    onNeedsPin(inspection)
                }
                return@launch
            }

            val outcome = localBackupManager.restoreFromData(inspection, pin)
            withContext(Dispatchers.Main) {
                onComplete(outcome)
            }
        }
    }

    fun restoreInspectedBackup(
        inspection: BackupFileInspection,
        pin: String,
        onComplete: (LocalRestoreOutcome) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val outcome = localBackupManager.restoreFromData(inspection, pin)
            withContext(Dispatchers.Main) {
                onComplete(outcome)
            }
        }
    }

    fun performManualRestore(userPin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _syncState.value = SyncState.InProgress
            val outcome = restoreManager.executeRestore(
                userId = anonymousUserId,
                passphrase = userPin.toCharArray()
            )

            _syncState.value = when (outcome) {
                is DataRestoreOutcome.Success -> {
                    SyncState.Success(text(R.string.security_restore_success, outcome.logsRestored, outcome.cyclesRestored))
                }
                is DataRestoreOutcome.InvalidPassphrase -> {
                    SyncState.Error(text(R.string.security_restore_invalid_passphrase))
                }
                is DataRestoreOutcome.BackupNotFound -> {
                    SyncState.Error(text(R.string.security_restore_backup_not_found))
                }
                is DataRestoreOutcome.IntegrityCheckFailed -> {
                    SyncState.Error(text(R.string.security_restore_integrity_failed))
                }
                is DataRestoreOutcome.NetworkError -> {
                    SyncState.Error(text(R.string.security_restore_network_error, outcome.message))
                }
            }
        }
    }

    fun exportAndDownloadCsv(context: Context, onDownloaded: (PdfShareHelper.SaveResult) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val cycles = database.cycleDao().getAllCycles()
            val logs = database.dailyLogDao().getAllLogsDesc()
            val file = CsvExportHelper.generateCsvFile(context, cycles, logs)
            val saveResult = CsvExportHelper.saveCsvToDownloads(context, file)
            withContext(Dispatchers.Main) {
                onDownloaded(saveResult)
            }
        }
    }

    fun exportAndShareCsv(context: Context) {
        exportAndDownloadCsv(context) {
            CsvExportHelper.shareCsv(context, it)
        }
    }

    fun wipeAllUserData(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = wipeManager.executeCompleteWipe(purgeRemoteCloud = true)
            withContext(Dispatchers.Main) {
                onComplete(result.isSuccess)
            }
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }
}
