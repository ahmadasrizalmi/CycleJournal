package com.app.cyclejournal.scheduler.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.cyclejournal.data.local.AppDatabase
import com.app.cyclejournal.domain.engine.ClinicalCycleEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reschedules alarms upon device reboot (ACTION_BOOT_COMPLETED) or app update.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val scheduler = CycleAlarmScheduler(context)
            // 1. Reschedule daily morning BBT reminder
            scheduler.scheduleDailyBbtReminder(6, 0)

            // 2. Query latest cycle and reschedule H-2 period alert
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val latestCycle = db.cycleDao().getLatestCycle()
                    val completed = db.cycleDao().getCompletedCycles()

                    if (latestCycle != null) {
                        val engine = ClinicalCycleEngine()
                        val stats = engine.calculateCycleStats(completed)
                        val avgLength = stats?.averageLength ?: 28.0
                        val prediction = engine.predictFertileWindow(latestCycle.startDate, avgLength)
                        scheduler.schedulePeriodAlert(prediction.predictedNextPeriodDate)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
