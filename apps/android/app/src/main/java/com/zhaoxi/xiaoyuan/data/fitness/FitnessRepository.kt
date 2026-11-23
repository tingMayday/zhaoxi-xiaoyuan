package com.zhaoxi.xiaoyuan.data.fitness

class FitnessRepository(private val api: FitnessApi) {
    suspend fun series() = api.series()
    suspend fun saveBodyLog(date: String, body: BodyLogInput) { api.saveBodyLog(date, body) }
}