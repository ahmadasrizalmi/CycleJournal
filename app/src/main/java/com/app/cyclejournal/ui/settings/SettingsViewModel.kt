package com.app.cyclejournal.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cyclejournal.R
import com.app.cyclejournal.data.local.AppDatabase
import androidx.annotation.StringRes
import com.app.cyclejournal.data.preferences.AppLocale
import android.net.Uri
import com.app.cyclejournal.domain.manager.BackupFileInspection
import com.app.cyclejournal.domain.manager.LocalBackupManager
import com.app.cyclejournal.domain.manager.LocalRestoreOutcome
import com.app.cyclejournal.export.pdf.PdfShareHelper
import com.app.cyclejournal.domain.manager.DataWipeManager
import com.app.cyclejournal.export.csv.CsvExportHelper
import com.app.cyclejournal.security.SecurityPinManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Settings actions. Backups are local files only - CycleJournal deliberately keeps no server
 * component, so the only paths here are file export, file restore and the full local wipe.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val database: AppDatabase,
    private val pinManager: SecurityPinManager,
    private val localBackupManager: LocalBackupManager,
    private val wipeManager: DataWipeManager
) : ViewModel() {

    /** Resolves user-facing text in the language selected inside the app. */
    private fun text(@StringRes id: Int, vararg args: Any?): String =
        AppLocale.wrap(appContext).getString(id, *args)

    val anonymousUserId: String = pinManager.getOrCreateAnonymousUserId()

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
            val result = wipeManager.executeCompleteWipe()
            withContext(Dispatchers.Main) {
                onComplete(result.isSuccess)
            }
        }
    }
}
