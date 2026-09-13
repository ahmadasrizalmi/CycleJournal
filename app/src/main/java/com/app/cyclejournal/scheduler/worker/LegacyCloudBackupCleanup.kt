package com.app.cyclejournal.scheduler.worker

import android.content.Context
import androidx.work.WorkManager

/**
 * Cancels the monthly cloud-backup work that releases up to 1.1.4 used to schedule.
 *
 * CycleJournal keeps every byte on the device now, so the worker class is gone. Installs upgrading
 * from an older release still hold the periodic request in WorkManager, which is why the unique
 * name has to stay around long enough to be cancelled.
 */
object LegacyCloudBackupCleanup {

    private const val UNIQUE_WORK_NAME = "monthly_cycle_backup_work"

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
