package com.zhaoxi.xiaoyuan.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(EXTRA_ID).orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "日程提醒" }
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        if (id.isBlank()) return
        ReminderNotifier.show(context, id, title, body)
    }

    companion object {
        const val ACTION = "com.zhaoxi.xiaoyuan.SCHEDULE_REMINDER"
        const val EXTRA_ID = "schedule_id"
        const val EXTRA_TITLE = "schedule_title"
        const val EXTRA_BODY = "schedule_body"
    }
}
