package com.app.cyclejournal.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cyclejournal.data.backup.BackupJsonParser
import com.app.cyclejournal.data.local.AppDatabase
import com.app.cyclejournal.data.preferences.OnboardingPreferences
import com.app.cyclejournal.data.remote.CloudflareBackupClient
import com.app.cyclejournal.data.remote.model.BackupUploadRequest
import com.app.cyclejournal.export.pdf.PdfShareHelper
import com.app.cyclejournal.domain.manager.DataRestoreManager
import com.app.cyclejournal.domain.manager.DataRestoreOutcome
import com.app.cyclejournal.domain.manager.DataWipeManager
import com.app.cyclejournal.export.csv.CsvExportHelper
import com.app.cyclejournal.security.BackupCryptoEngine
import com.app.cyclejournal.security.SecurityPinManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val database: AppDatabase,
    private val pinManager: SecurityPinManager,
    private val prefs: OnboardingPreferences,
    private val cryptoEngine: BackupCryptoEngine,
    private val backupClient: CloudflareBackupClient,
    private val restoreManager: DataRestoreManager,
    private val wipeManager: DataWipeManager
) : ViewModel() {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    val anonymousUserId: String = pinManager.getOrCreateAnonymousUserId()

    fun getLastSyncTimestamp(): Long = prefs.getLastSyncTimestamp()

    fun performManualBackup(userPin: String) {
        if (!pinManager.verifyPin(userPin)) {
            _syncState.value = SyncState.Error("PIN yang dimasukkan salah.")
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
                    _syncState.value = SyncState.Success("Cadangan terenkripsi berhasil disinkronkan ke Cloudflare D1.")
                } else {
                    _syncState.value = SyncState.Error("Gagal mengunggah ke Cloudflare Worker. Periksa jaringan internet.")
                }
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.message ?: "Terjadi kesalahan saat mencadangkan.")
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
                    SyncState.Success("Berhasil memulihkan ${outcome.logsRestored} catatan harian & ${outcome.cyclesRestored} siklus.")
                }
                is DataRestoreOutcome.InvalidPassphrase -> {
                    SyncState.Error("PIN/Passphrase salah. Gagal mendekripsi ciphertext.")
                }
                is DataRestoreOutcome.BackupNotFound -> {
                    SyncState.Error("Tidak ada berkas cadangan di Cloudflare D1 untuk ID ini.")
                }
                is DataRestoreOutcome.IntegrityCheckFailed -> {
                    SyncState.Error("Integritas data rusak (SHA-256 Checksum mismatch).")
                }
                is DataRestoreOutcome.NetworkError -> {
                    SyncState.Error("Kesalahan jaringan: ${outcome.message}")
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

    fun wipeAllUserData(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            wipeManager.executeCompleteWipe(purgeRemoteCloud = true)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }
}
