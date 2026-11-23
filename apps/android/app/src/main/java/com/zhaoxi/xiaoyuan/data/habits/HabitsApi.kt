package com.zhaoxi.xiaoyuan.data.habits

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface HabitsApi {
    @GET("api/habits")
    suspend fun board(): HabitBoard

    @POST("api/habits")
    suspend fun create(@Body body: NewHabit): Habit

    @PUT("api/habits/{id}/logs/{date}")
    suspend fun saveLog(
        @Path("id") id: String,
        @Path("date") date: String,
        @Body body: HabitLogBody,
    ): ResponseBody

    @PATCH("api/habits/{id}")
    suspend fun archive(@Path("id") id: String, @Body body: ArchiveBody = ArchiveBody()): ResponseBody
}
