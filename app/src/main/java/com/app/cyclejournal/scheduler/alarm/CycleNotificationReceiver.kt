package com.app.cyclejournal.scheduler.alarm

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.cyclejournal.MainActivity
import com.app.cyclejournal.R
import com.app.cyclejournal.scheduler.notification.NotificationChannelManager

class CycleNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID_BBT = 2001
        const val NOTIFICATION_ID_PERIOD = 2002
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = NotificationManagerCompat.from(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        when (intent.action) {
            CycleAlarmScheduler.ACTION_TRIGGER_BBT -> {
                // 1. Show high-priority morning BBT notification
                val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_BBT)
                    .setSmallIcon(R.drawable.logo_pdf_header)
                    .setContentTitle("Waktunya Ukur Suhu Basal (BBT)")
                    .setContentText("Ukur suhu basal tubuh sekarang sebelum beranjak dari tempat tidur untuk akurasi ovulasi.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                try {
                    notificationManager.notify(NOTIFICATION_ID_BBT, notification)
                } catch (e: SecurityException) {
                    // Notification permission not granted on Android 13+
                }

                // 2. Self-chain schedule next alarm for tomorrow 06:00 AM
                val scheduler = CycleAlarmScheduler(context)
                scheduler.scheduleDailyBbtReminder(6, 0)
            }

            CycleAlarmScheduler.ACTION_TRIGGER_PERIOD -> {
                // Show discrete period warning (H-2)
                val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_PERIOD)
                    .setSmallIcon(R.drawable.logo_pdf_header)
                    .setContentTitle("Peringatan Siklus Menstruasi")
                    .setContentText("Berdasarkan prediksi FIGO, siklus menstruasi diperkirakan akan dimulai dalam 2 hari.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                try {
                    notificationManager.notify(NOTIFICATION_ID_PERIOD, notification)
                } catch (e: SecurityException) {
                    // Ignored if permission revoked
                }
            }
        }
    }
}
