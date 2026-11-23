package com.zhaoxi.xiaoyuan.data.fitness

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface FitnessApi {
    @GET("api/fitness/series")
    suspend fun series(): FitnessSeries

    @PUT("api/body-logs/{date}")
    suspend fun saveBodyLog(@Path("date") date: String, @Body body: BodyLogInput): ResponseBody
}
