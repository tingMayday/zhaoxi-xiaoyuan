package com.zhaoxi.xiaoyuan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.zhaoxi.xiaoyuan.notify.DeepLink
import com.zhaoxi.xiaoyuan.ui.nav.ZhaoXiNav
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.ZhaoXiTheme

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* 拒绝时仍可写入日程，只是到点不一定弹出 */ }

    private var deepLinkOpen by mutableStateOf<String?>(null)
    private var deepLinkScheduleId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askNotificationPermission()
        applyDeepLink(intent)
        val app = application as ZhaoXiApp
        setContent {
            ZhaoXiTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Card) {
                    ZhaoXiNav(
                        container = app.container,
                        deepLinkOpen = deepLinkOpen,
                        deepLinkScheduleId = deepLinkScheduleId,
                        onDeepLinkConsumed = {
                            deepLinkOpen = null
                            deepLinkScheduleId = null
                        },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        applyDeepLink(intent)
    }

    private fun applyDeepLink(intent: Intent?) {
        deepLinkOpen = intent?.getStringExtra(DeepLink.EXTRA_OPEN)
        deepLinkScheduleId = intent?.getStringExtra(DeepLink.EXTRA_SCHEDULE_ID)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
