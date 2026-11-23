package com.zhaoxi.xiaoyuan.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhaoxi.xiaoyuan.data.network.toUserMessage
import com.zhaoxi.xiaoyuan.data.schedule.NewSchedule
import com.zhaoxi.xiaoyuan.data.schedule.ScheduleBoard
import com.zhaoxi.xiaoyuan.data.schedule.ScheduleItem
import com.zhaoxi.xiaoyuan.data.schedule.ScheduleRepository
import com.zhaoxi.xiaoyuan.ui.common.todayDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ScheduleUiState(
    val loading: Boolean = true,
    val board: ScheduleBoard? = null,
    val title: String = "",
    val dueOn: String = todayDate(),
    val note: String = "",
    val error: String? = null,
    val busy: Boolean = false,
    val pendingToggle: ScheduleItem? = null,
    val pendingDelete: ScheduleItem? = null,
)

class ScheduleViewModel(private val repo: ScheduleRepository) : ViewModel() {
    private val _ui = MutableStateFlow(ScheduleUiState())
    val ui: StateFlow<ScheduleUiState> = _ui.asStateFlow()

    init { refresh() }

    fun setTitle(v: String) = _ui.update { it.copy(title = v, error = null) }
    fun setDueOn(v: String) = _ui.update { it.copy(dueOn = v) }
    fun setNote(v: String) = _ui.update { it.copy(note = v) }
    fun askToggle(item: ScheduleItem) = _ui.update { it.copy(pendingToggle = item) }
    fun askDelete(item: ScheduleItem) = _ui.update { it.copy(pendingDelete = item) }
    fun cancelDialog() = _ui.update { it.copy(pendingToggle = null, pendingDelete = null) }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching { repo.board() }
                .onSuccess { board -> _ui.update { it.copy(loading = false, board = board, dueOn = board.today) } }
                .onFailure { err -> _ui.update { it.copy(loading = false, error = err.toUserMessage()) } }
        }
    }

    fun create() {
        val state = _ui.value
        if (state.title.isBlank()) {
            _ui.update { it.copy(error = "请填写标题") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null) }
            runCatching {
                repo.create(NewSchedule(title = state.title.trim(), dueOn = state.dueOn, note = state.note.trim()))
            }.onSuccess {
                _ui.update { it.copy(busy = false, title = "", note = "") }
                refresh()
            }.onFailure { err ->
                _ui.update { it.copy(busy = false, error = err.toUserMessage()) }
            }
        }
    }

    fun confirmToggle() {
        val item = _ui.value.pendingToggle ?: return
        val next = if (item.status == "done") "open" else "done"
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, pendingToggle = null, error = null) }
            runCatching { repo.setStatus(item.id, next) }
                .onSuccess { _ui.update { it.copy(busy = false) }; refresh() }
                .onFailure { err -> _ui.update { it.copy(busy = false, error = err.toUserMessage()) } }
        }
    }

    fun confirmDelete() {
        val item = _ui.value.pendingDelete ?: return
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, pendingDelete = null, error = null) }
            runCatching { repo.remove(item.id) }
                .onSuccess { _ui.update { it.copy(busy = false) }; refresh() }
                .onFailure { err -> _ui.update { it.copy(busy = false, error = err.toUserMessage()) } }
        }
    }

    companion object {
        fun factory(repo: ScheduleRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ScheduleViewModel(repo) as T
        }
    }
}

fun ScheduleBoard.groups(): List<Pair<String, List<ScheduleItem>>> {
    val upcomingEnd = LocalDate.parse(today).plusDays(7).toString()
    return listOf(
        "逾期" to overdueItems,
        "今天" to items.filter { it.dueOn == today },
        "未来 7 天" to items.filter { it.dueOn > today && it.dueOn <= upcomingEnd },
    )
}
