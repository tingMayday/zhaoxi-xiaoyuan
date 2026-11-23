package com.zhaoxi.xiaoyuan.data.fitness

class FitnessRepository(private val api: FitnessApi) {
    suspend fun series() = api.series()
    suspend fun saveBodyLog(date: String, body: BodyLogInput) { api.saveBodyLog(date, body) }
    suspend fun saveProfile(body: FitnessProfileInput) = api.saveProfile(body)
    suspend fun weekPlan() = api.weekPlan()
    suspend fun saveWeekPlan(items: List<WeekPlanItem>) = api.saveWeekPlan(ReplaceWeekPlan(items))
}