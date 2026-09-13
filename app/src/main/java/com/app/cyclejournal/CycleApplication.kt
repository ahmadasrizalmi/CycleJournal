package com.app.cyclejournal

import android.app.Application
import com.app.cyclejournal.data.local.AppDatabase
import com.app.cyclejournal.data.local.DemoDataSeeder
import com.app.cyclejournal.data.preferences.AppLocale
import com.app.cyclejournal.data.preferences.OnboardingPreferences
import com.app.cyclejournal.scheduler.alarm.CycleAlarmScheduler
import com.app.cyclejournal.scheduler.notification.NotificationChannelManager
import com.app.cyclejournal.scheduler.worker.LegacyCloudBackupCleanup
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class CycleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Create high-importance notification channels in the user's chosen app language
        NotificationChannelManager.createChannels(AppLocale.wrap(this))

        // 2. Drop any monthly cloud-backup work an older release left behind: backups are
        //    fully offline now, so nothing may keep reaching for the network.
        LegacyCloudBackupCleanup.cancel(this)

        // 3. Daily morning waking BBT reminder, unless the user switched it off
        val alarmScheduler = CycleAlarmScheduler(this)
        if (OnboardingPreferences(this).isBbtReminderEnabled()) {
            alarmScheduler.scheduleDailyBbtReminder(5, 30)
        } else {
            alarmScheduler.cancelBbtReminder()
        }

        // 4. Marketing screenshot builds (-PdemoSeed=true) start from a populated history
        if (BuildConfig.DEMO_SEED) {
            CoroutineScope(Dispatchers.IO).launch {
                DemoDataSeeder.seedIfEmpty(AppDatabase.getInstance(this@CycleApplication))
            }
        }
    }
}
