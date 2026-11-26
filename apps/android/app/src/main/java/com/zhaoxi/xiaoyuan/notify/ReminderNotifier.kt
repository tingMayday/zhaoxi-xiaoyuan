package com.zhaoxi.xiaoyuan.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zhaoxi.xiaoyuan.MainActivity
import com.zhaoxi.xiaoyuan.R

object ReminderNotifier {
    const val CHANNEL_ID = "schedule_reminders"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "日程提醒",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "到点提醒今天要做的事"
        }
        manager.createNotificationChannel(channel)
    }

    fun show(context: Context, scheduleId: String, title: String, body: String) {
        ensureChannel(context)
        val open = PendingIntent.getActivity(
            context,
            scheduleId.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(DeepLink.EXTRA_OPEN, DeepLink.OPEN_SCHEDULE)
                putExtra(DeepLink.EXTRA_SCHEDULE_ID, scheduleId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle(title)
            .setContentText(body.ifBlank { "该做这件事了" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(body.ifBlank { "该做这件事了" }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        runCatching {
            NotificationManagerCompat.from(context).notify(scheduleId.hashCode(), notification)
        }
    }
}
