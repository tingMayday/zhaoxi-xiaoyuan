package com.zhaoxi.xiaoyuan.ui.fitness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhaoxi.xiaoyuan.data.fitness.BodyLogInput
import com.zhaoxi.xiaoyuan.data.fitness.FitnessProfileInput
import com.zhaoxi.xiaoyuan.data.fitness.FitnessRepository
import com.zhaoxi.xiaoyuan.data.fitness.FitnessSeries
import com.zhaoxi.xiaoyuan.data.fitness.WeekPlanItem
import com.zhaoxi.xiaoyuan.data.network.toUserMessage
import com.zhaoxi.xiaoyuan.ui.common.todayDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FitnessUiState(
    val loading: Boolean = true,
    val series: FitnessSeries? = null,
    val weekPlan: List<WeekPlanItem> = emptyList(),
    val weight: String = "",
    val intake: String = "",
    val minutes: String = "",
    val startWeight: String = "",
    val targetWeight: String = "",
    val startDate: String = todayDate(),
    val targetIntake: String = "1800",
    val planDraft: Map<Int, String> = (1..7).associateWith { "" },
    val error: String? = null,
    val busy: Boolean = false,
)

class FitnessViewModel(
    private val repo: FitnessRepository,
) : ViewModel() {
    private val _ui = MutableStateFlow(FitnessUiState())
    val ui: StateFlow<FitnessUiState> = _ui.asStateFlow()

    fun setWeight(v: String) = _ui.update { it.copy(weight = v, error = null) }
    fun setIntake(v: String) = _ui.update { it.copy(intake = v) }
    fun setMinutes(v: String) = _ui.update { it.copy(minutes = v) }
    fun setStartWeight(v: String) = _ui.update { it.copy(startWeight = v, error = null) }
    fun setTargetWeight(v: String) = _ui.update { it.copy(targetWeight = v) }
    fun setStartDate(v: String) = _ui.update { it.copy(startDate = v) }
    fun setTargetIntake(v: String) = _ui.update { it.copy(targetIntake = v) }
    fun setPlanDay(weekday: Int, title: String) = _ui.update {
        it.copy(planDraft = it.planDraft + (weekday to title))
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching { repo.series() to repo.weekPlan() }
                .onSuccess { (series, plan) ->
                    val profile = series.profile
                    val draft = (1..7).associateWith { day ->
                        plan.filter { it.weekday == day }.joinToString(" / ") { it.title }
                    }
                    _ui.update {
                        it.copy(
                            loading = false,
                            series = series,
                            weekPlan = plan,
                            weight = series.latest?.weightKg?.toString().orEmpty(),
                            startWeight = profile?.startWeightKg?.toString().orEmpty(),
                            targetWeight = profile?.targetWeightKg?.toString().orEmpty(),
                            startDate = profile?.startDate ?: it.startDate,
                            targetIntake = profile?.targetIntakeKcal?.toString() ?: it.targetIntake,
                            planDraft = draft,
                        )
                    }
                }
                .onFailure { err ->
                    _ui.update { it.copy(loading = false, error = err.toUserMessage()) }
                }
        }
    }

    fun save() {
        val state = _ui.value
        val series = state.series ?: return
        val weight = state.weight.toDoubleOrNull()
        val intake = state.intake.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
        val minutes = state.minutes.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
        if (weight == null && intake == null && minutes == null) {
            _ui.update { it.copy(error = "请至少填写一项") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null) }
            runCatching {
                repo.saveBodyLog(
                    series.today,
                    BodyLogInput(weightKg = weight, intakeKcal = intake, exerciseMin = minutes),
                )
            }.onSuccess {
                _ui.update { it.copy(busy = false, intake = "", minutes = "") }
                refresh()
            }.onFailure { err ->
                _ui.update { it.copy(busy = false, error = err.toUserMessage()) }
            }
        }
    }

    fun saveProfile() {
        val state = _ui.value
        val start = state.startWeight.toDoubleOrNull()
        val target = state.targetWeight.toDoubleOrNull()
        val intake = state.targetIntake.toIntOrNull()
        if (start == null || target == null || intake == null || state.startDate.isBlank()) {
            _ui.update { it.copy(error = "请完整填写减脂目标") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null) }
            runCatching {
                repo.saveProfile(
                    FitnessProfileInput(
                        startWeightKg = start,
                        targetWeightKg = target,
                        startDate = state.startDate.trim(),
                        targetIntakeKcal = intake,
                    ),
                )
            }.onSuccess {
                _ui.update { it.copy(busy = false) }
                refresh()
            }.onFailure { err ->
                _ui.update { it.copy(busy = false, error = err.toUserMessage()) }
            }
        }
    }

    fun saveWeekPlan() {
        val items = _ui.value.planDraft.flatMap { (weekday, text) ->
            text.split('/', '、', ',').map { it.trim() }.filter { it.isNotEmpty() }
                .mapIndexed { index, title -> WeekPlanItem(weekday = weekday, title = title, sort = index) }
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null) }
            runCatching { repo.saveWeekPlan(items) }
                .onSuccess {
                    _ui.update { it.copy(busy = false) }
                    refresh()
                }
                .onFailure { err ->
                    _ui.update { it.copy(busy = false, error = err.toUserMessage()) }
                }
        }
    }

    companion object {
        fun factory(repo: FitnessRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = FitnessViewModel(repo) as T
        }
    }
}
