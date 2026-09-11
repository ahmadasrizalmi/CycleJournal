package com.app.cyclejournal

import android.app.Application
import com.app.cyclejournal.data.preferences.AppLocale
import com.app.cyclejournal.scheduler.alarm.CycleAlarmScheduler
import com.app.cyclejournal.scheduler.notification.NotificationChannelManager
import com.app.cyclejournal.scheduler.worker.BackupScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CycleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Create high-importance notification channels in the user's chosen app language
        NotificationChannelManager.createChannels(AppLocale.wrap(this))

        // 2. Schedule 30-day periodic encrypted cloud backup worker
        BackupScheduler.scheduleMonthlyBackup(this)

        // 3. Initialize daily morning waking BBT reminder
        val alarmScheduler = CycleAlarmScheduler(this)
        alarmScheduler.scheduleDailyBbtReminder(6, 0)
    }
}
