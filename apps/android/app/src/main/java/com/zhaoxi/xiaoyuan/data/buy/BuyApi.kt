package com.zhaoxi.xiaoyuan.data.buy

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface BuyApi {
    @GET("api/buy-items")
    suspend fun board(): BuyBoard

    @POST("api/buy-items")
    suspend fun create(@Body body: NewBuyItem): BuyItem

    @PATCH("api/buy-items/{id}")
    suspend fun update(@Path("id") id: String, @Body body: BuyStatusBody): ResponseBody

    @DELETE("api/buy-items/{id}")
    suspend fun remove(@Path("id") id: String): ResponseBody
}
