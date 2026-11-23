package com.zhaoxi.xiaoyuan.data.habits

class HabitsRepository(private val api: HabitsApi) {
    suspend fun board() = api.board()
    suspend fun create(body: NewHabit) = api.create(body)
    suspend fun saveLog(id: String, date: String, value: Double) {
        api.saveLog(id, date, HabitLogBody(value))
    }
    suspend fun archive(id: String) { api.archive(id) }
}
