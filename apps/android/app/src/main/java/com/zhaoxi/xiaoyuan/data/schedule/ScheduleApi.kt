package com.zhaoxi.xiaoyuan.data.schedule

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ScheduleApi {
    @GET("api/schedule")
    suspend fun board(): ScheduleBoard

    @POST("api/schedule")
    suspend fun create(@Body body: NewSchedule): ScheduleItem

    @PATCH("api/schedule/{id}")
    suspend fun update(@Path("id") id: String, @Body body: ScheduleStatusBody): ResponseBody

    @DELETE("api/schedule/{id}")
    suspend fun remove(@Path("id") id: String): ResponseBody
}
