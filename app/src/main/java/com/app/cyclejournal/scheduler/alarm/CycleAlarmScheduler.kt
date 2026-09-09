package com.app.cyclejournal.scheduler.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

/**
 * Exact alarm scheduler utilizing AlarmManager.setExactAndAllowWhileIdle to bypass Doze mode.
 */
class CycleAlarmScheduler(private val context: Context) {

    companion object {
        const val ACTION_TRIGGER_BBT = "com.app.cyclejournal.ACTION_TRIGGER_BBT"
        const val ACTION_TRIGGER_PERIOD = "com.app.cyclejournal.ACTION_TRIGGER_PERIOD"

        const val REQUEST_CODE_BBT = 1001
        const val REQUEST_CODE_PERIOD = 1002
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedules the 06:00 AM waking BBT reminder.
     */
    fun scheduleDailyBbtReminder(hour: Int = 6, minute: Int = 0) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
            action = ACTION_TRIGGER_BBT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BBT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(calendar.timeInMillis, pendingIntent)
    }

    /**
     * Schedules the period warning 2 days before predicted start at 09:00 AM.
     */
    fun schedulePeriodAlert(predictedPeriodDate: LocalDate) {
        val triggerDateTime = predictedPeriodDate.minusDays(2).atTime(9, 0)
        val triggerEpoch = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (triggerEpoch <= System.currentTimeMillis()) return

        val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
            action = ACTION_TRIGGER_PERIOD
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PERIOD,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(triggerEpoch, pendingIntent)
    }

    private fun scheduleExact(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelBbtReminder() {
        val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
            action = ACTION_TRIGGER_BBT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BBT,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    fun cancelPeriodAlert() {
        val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
            action = ACTION_TRIGGER_PERIOD
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PERIOD,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }
}
