package com.zhaoxi.xiaoyuan.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.zhaoxi.xiaoyuan.BuildConfig
import com.zhaoxi.xiaoyuan.data.archive.ArchiveApi
import com.zhaoxi.xiaoyuan.data.auth.AuthApi
import com.zhaoxi.xiaoyuan.data.auth.TokenStore
import com.zhaoxi.xiaoyuan.data.buy.BuyApi
import com.zhaoxi.xiaoyuan.data.fitness.FitnessApi
import com.zhaoxi.xiaoyuan.data.habits.HabitsApi
import com.zhaoxi.xiaoyuan.data.ledger.LedgerApi
import com.zhaoxi.xiaoyuan.data.media.MediaApi
import com.zhaoxi.xiaoyuan.data.schedule.ScheduleApi
import com.zhaoxi.xiaoyuan.data.today.TodayApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class NetworkModule(tokenStore: TokenStore) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor(tokenStore))
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            },
        )
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL.trimEnd('/') + "/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val todayApi: TodayApi = retrofit.create(TodayApi::class.java)
    val ledgerApi: LedgerApi = retrofit.create(LedgerApi::class.java)
    val habitsApi: HabitsApi = retrofit.create(HabitsApi::class.java)
    val fitnessApi: FitnessApi = retrofit.create(FitnessApi::class.java)
    val scheduleApi: ScheduleApi = retrofit.create(ScheduleApi::class.java)
    val buyApi: BuyApi = retrofit.create(BuyApi::class.java)
    val mediaApi: MediaApi = retrofit.create(MediaApi::class.java)
    val archiveApi: ArchiveApi = retrofit.create(ArchiveApi::class.java)
}
