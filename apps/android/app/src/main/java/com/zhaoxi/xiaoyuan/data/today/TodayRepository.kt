package com.zhaoxi.xiaoyuan.data.today

class TodayRepository(
    private val api: TodayApi,
) {
    suspend fun fetch(): TodayOverview = api.today()
}
