package com.zhaoxi.xiaoyuan.data.schedule

class ScheduleRepository(private val api: ScheduleApi) {
    suspend fun board() = api.board()
    suspend fun create(body: NewSchedule) = api.create(body)
    suspend fun setStatus(id: String, status: String) { api.update(id, ScheduleStatusBody(status)) }
    suspend fun remove(id: String) { api.remove(id) }
}
