package com.zhaoxi.xiaoyuan.data.ledger

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LedgerApi {
    @GET("api/categories")
    suspend fun categories(): List<Category>

    @GET("api/finance/summary")
    suspend fun summary(@Query("month") month: String): FinanceSummary

    @GET("api/transactions")
    suspend fun transactions(
        @Query("month") month: String,
        @Query("direction") direction: String? = null,
        @Query("categoryId") categoryId: String? = null,
    ): List<Transaction>

    @POST("api/transactions")
    suspend fun create(@Body body: NewTransaction): Transaction

    @DELETE("api/transactions/{id}")
    suspend fun remove(@Path("id") id: String): ResponseBody
}
