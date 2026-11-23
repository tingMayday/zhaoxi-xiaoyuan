package com.zhaoxi.xiaoyuan.data.fitness

import kotlinx.serialization.Serializable

@Serializable
data class FitnessSeries(
    val today: String,
    val bmi: Double? = null,
    val progress: Double? = null,
    val deficit: Int? = null,
    val latest: LatestWeight? = null,
    val todayLog: TodayLog? = null,
    val profile: FitnessProfile? = null,
    val points: List<WeightPoint> = emptyList(),
)

@Serializable
data class LatestWeight(val weightKg: Double? = null)

@Serializable
data class TodayLog(val exerciseMin: Int? = null)

@Serializable
data class FitnessProfile(
    val startWeightKg: Double,
    val targetWeightKg: Double,
    val startDate: String,
    val targetIntakeKcal: Int,
)

@Serializable
data class WeightPoint(
    val date: String,
    val weightKg: Double? = null,
    val ma7: Double? = null,
)

@Serializable
data class BodyLogInput(
    val weightKg: Double? = null,
    val bodyFatPct: Double? = null,
    val intakeKcal: Int? = null,
    val exerciseMin: Int? = null,
)
