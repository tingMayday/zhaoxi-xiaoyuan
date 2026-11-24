package com.zhaoxi.xiaoyuan.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.zhaoxi.xiaoyuan.data.schedule.ScheduleItem

class ReminderScheduler(private val context: Context) {
    private val alarms = context.getSystemService(AlarmManager::class.java)

    fun sync(items: List<ScheduleItem>) {
        items.forEach { upsert(it) }
    }

    fun upsert(item: ScheduleItem) {
        cancel(item.id)
        if (item.status != "open") return
        val at = isoToMillis(item.remindAt) ?: return
        if (at <= System.currentTimeMillis() + 3_000) return
        val pending = pendingIntent(item.id, item.title, item.note)
        val manager = alarms ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        } else {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        }
    }

    fun cancel(id: String) {
        val pending = pendingIntent(id, "", "")
        alarms?.cancel(pending)
        pending.cancel()
    }

    private fun pendingIntent(id: String, title: String, body: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION
            putExtra(ReminderReceiver.EXTRA_ID, id)
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_BODY, body)
        }
        return PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
