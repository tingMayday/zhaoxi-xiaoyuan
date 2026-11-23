package com.zhaoxi.xiaoyuan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.zhaoxi.xiaoyuan.ui.nav.ZhaoXiNav
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.ZhaoXiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as ZhaoXiApp
        setContent {
            ZhaoXiTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Card) {
                    ZhaoXiNav(app.container)
                }
            }
        }
    }
}
