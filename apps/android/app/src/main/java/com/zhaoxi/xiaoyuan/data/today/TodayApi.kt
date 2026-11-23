package com.zhaoxi.xiaoyuan.data.today

import retrofit2.http.GET

interface TodayApi {
    @GET("api/today")
    suspend fun today(): TodayOverview
}
