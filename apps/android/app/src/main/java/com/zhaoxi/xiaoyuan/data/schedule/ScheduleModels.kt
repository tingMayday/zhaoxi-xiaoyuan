package com.zhaoxi.xiaoyuan.data.schedule

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleBoard(
    val today: String,
    val items: List<ScheduleItem> = emptyList(),
    val overdueItems: List<ScheduleItem> = emptyList(),
)

@Serializable
data class ScheduleItem(
    val id: String,
    val title: String,
    val dueOn: String,
    val status: String,
    val note: String = "",
    val startsAt: String? = null,
    val remindAt: String? = null,
    val allDay: Boolean = true,
)

@Serializable
data class NewSchedule(
    val title: String,
    val dueOn: String,
    val note: String = "",
    val remindAt: String? = null,
    val allDay: Boolean = true,
)

@Serializable
data class ScheduleStatusBody(val status: String)
