package com.zhaoxi.xiaoyuan.data

import android.content.Context
import com.zhaoxi.xiaoyuan.data.archive.ArchiveRepository
import com.zhaoxi.xiaoyuan.data.auth.AuthRepository
import com.zhaoxi.xiaoyuan.data.auth.TokenStore
import com.zhaoxi.xiaoyuan.data.buy.BuyRepository
import com.zhaoxi.xiaoyuan.data.fitness.FitnessRepository
import com.zhaoxi.xiaoyuan.data.habits.HabitsRepository
import com.zhaoxi.xiaoyuan.data.ledger.LedgerRepository
import com.zhaoxi.xiaoyuan.data.media.MediaRepository
import com.zhaoxi.xiaoyuan.data.network.NetworkModule
import com.zhaoxi.xiaoyuan.data.schedule.ScheduleRepository
import com.zhaoxi.xiaoyuan.data.today.TodayRepository

class AppContainer(context: Context) {
    val tokenStore = TokenStore(context.applicationContext)
    private val network = NetworkModule(tokenStore)
    val authRepository = AuthRepository(network.authApi, tokenStore)
    val todayRepository = TodayRepository(network.todayApi)
    val ledgerRepository = LedgerRepository(network.ledgerApi)
    val habitsRepository = HabitsRepository(network.habitsApi)
    val fitnessRepository = FitnessRepository(network.fitnessApi)
    val scheduleRepository = ScheduleRepository(network.scheduleApi)
    val buyRepository = BuyRepository(network.buyApi)
    val mediaRepository = MediaRepository(network.mediaApi)
    val archiveRepository = ArchiveRepository(network.archiveApi)
}
