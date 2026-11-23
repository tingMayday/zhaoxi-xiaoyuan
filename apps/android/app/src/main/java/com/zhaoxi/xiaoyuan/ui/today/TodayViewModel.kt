package com.zhaoxi.xiaoyuan.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhaoxi.xiaoyuan.data.buy.BuyRepository
import com.zhaoxi.xiaoyuan.data.buy.NewBuyItem
import com.zhaoxi.xiaoyuan.data.fitness.BodyLogInput
import com.zhaoxi.xiaoyuan.data.fitness.FitnessRepository
import com.zhaoxi.xiaoyuan.data.habits.HabitsRepository
import com.zhaoxi.xiaoyuan.data.ledger.Category
import com.zhaoxi.xiaoyuan.data.ledger.LedgerRepository
import com.zhaoxi.xiaoyuan.data.ledger.NewTransaction
import com.zhaoxi.xiaoyuan.data.network.toUserMessage
import com.zhaoxi.xiaoyuan.data.schedule.NewSchedule
import com.zhaoxi.xiaoyuan.data.schedule.ScheduleRepository
import com.zhaoxi.xiaoyuan.data.today.TodayHabit
import com.zhaoxi.xiaoyuan.data.today.TodayOverview
import com.zhaoxi.xiaoyuan.data.today.TodayRepository
import com.zhaoxi.xiaoyuan.data.today.TodayTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class TodayUiState(
    val loading: Boolean = true,
    val data: TodayOverview? = null,
    val categories: List<Category> = emptyList(),
    val panel: String = "",
    val amount: String = "",
    val categoryId: String = "",
    val note: String = "",
    val scheduleTitle: String = "",
    val scheduleDueOn: String = "",
    val weight: String = "",
    val intake: String = "",
    val buyTitle: String = "",
    val buyAmount: String = "",
    val error: String? = null,
    val message: String? = null,
    val busy: Boolean = false,
    val unauthorized: Boolean = false,
)

class TodayViewModel(
    private val todayRepository: TodayRepository,
    private val ledgerRepository: LedgerRepository,
    private val scheduleRepository: ScheduleRepository,
    private val fitnessRepository: FitnessRepository,
    private val buyRepository: BuyRepository,
    private val habitsRepository: HabitsRepository,
    private val onUnauthorized: suspend () -> Unit,
) : ViewModel() {
    private val _ui = MutableStateFlow(TodayUiState())
    val ui: StateFlow<TodayUiState> = _ui.asStateFlow()

    fun togglePanel(id: String) = _ui.update {
        it.copy(panel = if (it.panel == id) "" else id, error = null, message = null)
    }

    fun setAmount(v: String) = _ui.update { it.copy(amount = v, error = null) }
    fun setCategoryId(v: String) = _ui.update { it.copy(categoryId = v) }
    fun setNote(v: String) = _ui.update { it.copy(note = v) }
    fun setScheduleTitle(v: String) = _ui.update { it.copy(scheduleTitle = v, error = null) }
    fun setScheduleDueOn(v: String) = _ui.update { it.copy(scheduleDueOn = v) }
    fun setWeight(v: String) = _ui.update { it.copy(weight = v, error = null) }
    fun setIntake(v: String) = _ui.update { it.copy(intake = v) }
    fun setBuyTitle(v: String) = _ui.update { it.copy(buyTitle = v, error = null) }
    fun setBuyAmount(v: String) = _ui.update { it.copy(buyAmount = v) }

    fun refresh() {
        viewModelScope.launch {
            val keepShowing = _ui.value.data != null
            _ui.update { it.copy(loading = !keepShowing, error = null, unauthorized = false) }
            runCatching {
                todayRepository.fetch() to ledgerRepository.categories()
            }.onSuccess { (overview, categories) ->
                val expense = categories.filter { it.kind == "expense" }
                _ui.update {
                    it.copy(
                        loading = false,
                        data = overview,
                        categories = expense,
                        categoryId = it.categoryId.ifBlank { expense.firstOrNull()?.id.orEmpty() },
                        scheduleDueOn = it.scheduleDueOn.ifBlank { overview.date },
                        weight = it.weight.ifBlank { overview.fitness.weightKg?.toString().orEmpty() },
                    )
                }
            }.onFailure { err ->
                if (err is HttpException && err.code() == 401) {
                    onUnauthorized()
                    _ui.update { it.copy(loading = false, unauthorized = true, error = "请重新登录") }
                } else {
                    _ui.update { it.copy(loading = false, error = err.toUserMessage()) }
                }
            }
        }
    }

    fun saveExpense() {
        val state = _ui.value
        val yuan = state.amount.toDoubleOrNull()
        val date = state.data?.date ?: return
        if (yuan == null || yuan <= 0) {
            _ui.update { it.copy(error = "请填写大于 0 的金额") }
            return
        }
        if (state.categoryId.isBlank()) {
            _ui.update { it.copy(error = "请选择分类") }
            return
        }
        runAction {
            ledgerRepository.create(
                NewTransaction(
                    occurredOn = date,
                    direction = "expense",
                    amountCents = Math.round(yuan * 100).toInt(),
                    categoryId = state.categoryId,
                    note = state.note.trim(),
                ),
            )
            _ui.update { it.copy(amount = "", note = "", message = "已记下支出") }
        }
    }

    fun saveSchedule() {
        val state = _ui.value
        if (state.scheduleTitle.isBlank()) {
            _ui.update { it.copy(error = "请填写事项") }
            return
        }
        val dueOn = state.scheduleDueOn.ifBlank { state.data?.date.orEmpty() }
        if (dueOn.isBlank()) {
            _ui.update { it.copy(error = "请填写日期") }
            return
        }
        runAction {
            scheduleRepository.create(NewSchedule(title = state.scheduleTitle.trim(), dueOn = dueOn))
            _ui.update { it.copy(scheduleTitle = "", message = "已安排") }
        }
    }

    fun saveBody() {
        val state = _ui.value
        val date = state.data?.date ?: return
        val weight = state.weight.toDoubleOrNull()
        if (weight == null) {
            _ui.update { it.copy(error = "请填写体重") }
            return
        }
        val intake = state.intake.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
        if (state.intake.isNotBlank() && intake == null) {
            _ui.update { it.copy(error = "摄入请填写整数") }
            return
        }
        runAction {
            fitnessRepository.saveBodyLog(date, BodyLogInput(weightKg = weight, intakeKcal = intake))
            _ui.update { it.copy(message = "已记下体重") }
        }
    }

    fun saveBuy() {
        val state = _ui.value
        if (state.buyTitle.isBlank()) {
            _ui.update { it.copy(error = "请填写想买的东西") }
            return
        }
        val cents = state.buyAmount.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()?.let { Math.round(it * 100).toInt() } ?: 0
        if (state.buyAmount.isNotBlank() && cents < 0) {
            _ui.update { it.copy(error = "金额无效") }
            return
        }
        runAction {
            buyRepository.create(NewBuyItem(title = state.buyTitle.trim(), estimateCents = cents))
            _ui.update { it.copy(buyTitle = "", buyAmount = "", message = "已放入清单") }
        }
    }

    fun finishTask(task: TodayTask) {
        val next = if (task.status == "done") "open" else "done"
        runAction {
            scheduleRepository.setStatus(task.id, next)
            _ui.update { it.copy(message = if (next == "done") "已完成" else "已改回待办") }
        }
    }

    fun checkHabit(habit: TodayHabit) {
        val date = _ui.value.data?.date ?: return
        val next = if (habit.value >= habit.dailyTarget) 0.0 else habit.dailyTarget
        runAction {
            habitsRepository.saveLog(habit.id, date, next)
            _ui.update { it.copy(message = if (next > 0) "已打卡" else "已取消") }
        }
    }

    private fun runAction(block: suspend () -> Unit) {
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null, message = null) }
            runCatching { block() }
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
        fun factory(
            todayRepository: TodayRepository,
            ledgerRepository: LedgerRepository,
            scheduleRepository: ScheduleRepository,
            fitnessRepository: FitnessRepository,
            buyRepository: BuyRepository,
            habitsRepository: HabitsRepository,
            onUnauthorized: suspend () -> Unit,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                TodayViewModel(
                    todayRepository,
                    ledgerRepository,
                    scheduleRepository,
                    fitnessRepository,
                    buyRepository,
                    habitsRepository,
                    onUnauthorized,
                ) as T
        }
    }
}
