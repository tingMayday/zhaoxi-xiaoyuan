package com.zhaoxi.xiaoyuan.data.habits

import kotlinx.serialization.Serializable

@Serializable
data class HabitBoard(
    val today: String,
    val days: List<String> = emptyList(),
    val habits: List<Habit> = emptyList(),
)

@Serializable
data class Habit(
    val id: String,
    val name: String,
    val kind: String,
    val unit: String = "",
    val dailyTarget: Double,
    val logs: List<HabitLog> = emptyList(),
)

@Serializable
data class HabitLog(
    val date: String,
    val value: Double,
)

@Serializable
data class HabitLogBody(val value: Double)

@Serializable
data class NewHabit(
    val name: String,
    val kind: String,
    val unit: String = "",
    val dailyTarget: Double,
)

@Serializable
data class ArchiveBody(val archived: Boolean = true)
