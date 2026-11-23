package com.zhaoxi.xiaoyuan

import android.app.Application
import com.zhaoxi.xiaoyuan.data.AppContainer

class ZhaoXiApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
