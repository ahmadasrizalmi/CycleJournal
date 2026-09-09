package com.app.cyclejournal.scheduler.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

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
                "Pengingat Suhu Basal (BBT)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pagi hari untuk mengukur suhu basal tubuh segera setelah bangun tidur."
                enableVibration(true)
            }

            // 2. Discrete period prediction alert channel
            val periodChannel = NotificationChannel(
                CHANNEL_PERIOD,
                "Peringatan Prediksi Haid (H-2)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pemberitahuan diskrit 2 hari sebelum perkiraan tanggal mulai siklus haid."
            }

            notificationManager.createNotificationChannel(bbtChannel)
            notificationManager.createNotificationChannel(periodChannel)
        }
    }
}
