package com.zhaoxi.xiaoyuan.ui.fitness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhaoxi.xiaoyuan.data.fitness.BodyLogInput
import com.zhaoxi.xiaoyuan.data.fitness.FitnessRepository
import com.zhaoxi.xiaoyuan.data.fitness.FitnessSeries
import com.zhaoxi.xiaoyuan.data.network.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FitnessUiState(
    val loading: Boolean = true,
    val series: FitnessSeries? = null,
    val weight: String = "",
    val intake: String = "",
    val minutes: String = "",
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

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching { repo.series() }
                .onSuccess { series ->
                    _ui.update {
                        it.copy(
                            loading = false,
                            series = series,
                            weight = series.latest?.weightKg?.toString().orEmpty(),
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

    companion object {
        fun factory(repo: FitnessRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = FitnessViewModel(repo) as T
        }
    }
}
