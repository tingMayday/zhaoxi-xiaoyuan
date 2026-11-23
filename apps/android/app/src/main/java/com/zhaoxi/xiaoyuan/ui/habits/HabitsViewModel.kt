package com.zhaoxi.xiaoyuan.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhaoxi.xiaoyuan.data.habits.Habit
import com.zhaoxi.xiaoyuan.data.habits.HabitBoard
import com.zhaoxi.xiaoyuan.data.habits.HabitsRepository
import com.zhaoxi.xiaoyuan.data.habits.NewHabit
import com.zhaoxi.xiaoyuan.data.network.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitsUiState(
    val loading: Boolean = true,
    val board: HabitBoard? = null,
    val error: String? = null,
    val name: String = "",
    val kind: String = "check",
    val unit: String = "",
    val target: String = "1",
    val busy: Boolean = false,
    val pendingCheck: Habit? = null,
    val pendingArchive: Habit? = null,
)

class HabitsViewModel(
    private val repo: HabitsRepository,
) : ViewModel() {
    private val _ui = MutableStateFlow(HabitsUiState())
    val ui: StateFlow<HabitsUiState> = _ui.asStateFlow()

    fun setName(v: String) = _ui.update { it.copy(name = v, error = null) }
    fun setKind(v: String) = _ui.update { it.copy(kind = v) }
    fun setUnit(v: String) = _ui.update { it.copy(unit = v) }
    fun setTarget(v: String) = _ui.update { it.copy(target = v) }
    fun askCheck(habit: Habit) = _ui.update { it.copy(pendingCheck = habit) }
    fun askArchive(habit: Habit) = _ui.update { it.copy(pendingArchive = habit) }
    fun cancelDialog() = _ui.update { it.copy(pendingCheck = null, pendingArchive = null) }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching { repo.board() }
                .onSuccess { board -> _ui.update { it.copy(loading = false, board = board) } }
                .onFailure { err -> _ui.update { it.copy(loading = false, error = err.toUserMessage()) } }
        }
    }

    fun create() {
        val state = _ui.value
        val target = state.target.toDoubleOrNull()
        if (state.name.isBlank() || target == null || target <= 0) {
            _ui.update { it.copy(error = "请填写名称和有效目标") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null) }
            runCatching {
                repo.create(
                    NewHabit(
                        name = state.name.trim(),
                        kind = state.kind,
                        unit = state.unit.trim(),
                        dailyTarget = target,
                    ),
                )
            }.onSuccess {
                _ui.update { it.copy(busy = false, name = "", unit = "", target = "1") }
                refresh()
            }.onFailure { err ->
                _ui.update { it.copy(busy = false, error = err.toUserMessage()) }
            }
        }
    }

    fun confirmCheck() {
        val habit = _ui.value.pendingCheck ?: return
        val today = _ui.value.board?.today ?: return
        val current = habit.logs.find { it.date == today }?.value ?: 0.0
        val done = current >= habit.dailyTarget
        val next = if (done) 0.0 else habit.dailyTarget
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, pendingCheck = null, error = null) }
            runCatching { repo.saveLog(habit.id, today, next) }
                .onSuccess {
                    _ui.update { it.copy(busy = false) }
                    refresh()
                }
                .onFailure { err ->
                    _ui.update { it.copy(busy = false, error = err.toUserMessage()) }
                }
        }
    }

    fun bump(habit: Habit, delta: Double) {
        val today = _ui.value.board?.today ?: return
        val current = habit.logs.find { it.date == today }?.value ?: 0.0
        val next = (current + delta).coerceAtLeast(0.0)
        viewModelScope.launch {
            runCatching { repo.saveLog(habit.id, today, next) }
                .onSuccess { refresh() }
                .onFailure { err -> _ui.update { it.copy(error = err.toUserMessage()) } }
        }
    }

    fun confirmArchive() {
        val habit = _ui.value.pendingArchive ?: return
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, pendingArchive = null, error = null) }
            runCatching { repo.archive(habit.id) }
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
        fun factory(repo: HabitsRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HabitsViewModel(repo) as T
        }
    }
}
