package com.app.cyclejournal.scheduler.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.app.cyclejournal.R

object NotificationChannelManager {
    const val CHANNEL_BBT = "channel_bbt_reminder"
    const val CHANNEL_PERIOD = "channel_period_alert"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. High-importance morning BBT reminder channel
            val bbtChannel = NotificationChannel(
                CHANNEL_BBT,
                context.getString(R.string.notif_channel_bbt_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_bbt_description)
                enableVibration(true)
            }

            // 2. Discrete period prediction alert channel
            val periodChannel = NotificationChannel(
                CHANNEL_PERIOD,
                context.getString(R.string.notif_channel_period_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_period_description)
            }

            notificationManager.createNotificationChannel(bbtChannel)
            notificationManager.createNotificationChannel(periodChannel)
        }
    }
}
