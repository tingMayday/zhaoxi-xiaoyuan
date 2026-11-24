package com.zhaoxi.xiaoyuan.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zhaoxi.xiaoyuan.ZhaoXiApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** 开机后把未完成且仍有提醒时间的日程重新挂上闹钟。 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? ZhaoXiApp ?: return@launch
                val board = runCatching { app.container.scheduleRepository.board() }.getOrNull() ?: return@launch
                val open = (board.items + board.overdueItems).filter { it.status == "open" }
                ReminderScheduler(app).sync(open)
            } finally {
                pending.finish()
            }
        }
    }
}
