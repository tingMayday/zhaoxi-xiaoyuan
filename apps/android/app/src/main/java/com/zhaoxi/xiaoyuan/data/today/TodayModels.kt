package com.zhaoxi.xiaoyuan.data.today

import kotlinx.serialization.Serializable

@Serializable
data class TodayOverview(
    val date: String,
    val quote: String,
    val score: Int,
    val finance: TodayFinance,
    val habits: TodayHabitsSummary,
    val schedule: TodayScheduleSummary,
    val fitness: TodayFitness,
)

@Serializable
data class TodayFinance(
    val expenseCents: Int,
    val totalBudgetCents: Int? = null,
    val remainingCents: Int? = null,
)

@Serializable
data class TodayHabitsSummary(
    val done: Int,
    val total: Int,
)

@Serializable
data class TodayScheduleSummary(
    val todayOpen: Int,
    val todayTotal: Int,
    val overdue: Int,
    val next7: Int,
)

@Serializable
data class TodayFitness(
    val weightKg: Double? = null,
    val weightDate: String? = null,
)
