package com.zhaoxi.xiaoyuan.data.today

import kotlinx.serialization.Serializable

@Serializable
data class TodayOverview(
    val date: String,
    val quote: String,
    val score: Int,
    val finance: TodayFinance = TodayFinance(),
    val habits: TodayHabitsSummary = TodayHabitsSummary(),
    val schedule: TodayScheduleSummary = TodayScheduleSummary(),
    val fitness: TodayFitness = TodayFitness(),
    val recent: List<TodayRecent> = emptyList(),
    val buy: TodayBuy = TodayBuy(),
)

@Serializable
data class TodayFinance(
    val expenseCents: Int = 0,
    val totalBudgetCents: Int? = null,
    val remainingCents: Int? = null,
)

@Serializable
data class TodayHabitsSummary(
    val done: Int = 0,
    val total: Int = 0,
    val items: List<TodayHabit> = emptyList(),
)

@Serializable
data class TodayHabit(
    val id: String,
    val name: String,
    val kind: String,
    val unit: String = "",
    val dailyTarget: Double = 1.0,
    val value: Double = 0.0,
)

@Serializable
data class TodayScheduleSummary(
    val todayOpen: Int = 0,
    val todayTotal: Int = 0,
    val overdue: Int = 0,
    val next7: Int = 0,
    val items: List<TodayTask> = emptyList(),
)

@Serializable
data class TodayTask(
    val id: String,
    val title: String,
    val note: String = "",
    val status: String = "open",
    val time: String = "",
)

@Serializable
data class TodayRecent(
    val id: String,
    val date: String,
    val kind: String,
    val title: String,
    val detail: String = "",
    val value: String = "",
)

@Serializable
data class TodayFitness(
    val weightKg: Double? = null,
    val weightDate: String? = null,
)

@Serializable
data class TodayBuy(
    val openCount: Int = 0,
    val estimateCents: Int = 0,
)
