package com.zhaoxi.xiaoyuan.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhaoxi.xiaoyuan.data.today.TodayOverview
import com.zhaoxi.xiaoyuan.ui.common.AppField
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Blush
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Clay
import com.zhaoxi.xiaoyuan.ui.theme.Lilac
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Paper
import com.zhaoxi.xiaoyuan.ui.theme.Sage
import com.zhaoxi.xiaoyuan.ui.theme.Sand
import com.zhaoxi.xiaoyuan.ui.util.yuan

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onOpenLedger: () -> Unit,
    onOpenHabits: () -> Unit,
    onOpenFitness: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenBuy: () -> Unit,
    onOpenMedia: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    val state by viewModel.ui.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        when {
            state.loading && state.data == null -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(48.dp),
                    color = Accent,
                )
            }
            state.error != null && state.data == null -> {
                Text(state.error ?: "", color = Accent)
                Button(
                    onClick = viewModel::refresh,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
                    shape = RoundedCornerShape(999.dp),
                ) { Text("再试一次") }
            }
            else -> {
                val data = state.data ?: return

                HeroHeader(data)

                GlanceStrip(
                    data = data,
                    onOpenLedger = onOpenLedger,
                    onOpenHabits = onOpenHabits,
                    onOpenSchedule = onOpenSchedule,
                    onOpenFitness = onOpenFitness,
                )

                SectionLabel("进入模块")
                ModuleGrid(
                    data = data,
                    onOpenLedger = onOpenLedger,
                    onOpenHabits = onOpenHabits,
                    onOpenFitness = onOpenFitness,
                    onOpenSchedule = onOpenSchedule,
                    onOpenBuy = onOpenBuy,
                    onOpenMedia = onOpenMedia,
                    onOpenArchive = onOpenArchive,
                    onOpenSettings = onOpenSettings,
                )

                SectionLabel("此刻记下")
                QuickPanels(viewModel, state)
                state.error?.let { Text(it, color = Accent, style = MaterialTheme.typography.bodyMedium) }
                state.message?.let { Text(it, color = Accent, style = MaterialTheme.typography.bodyMedium) }

                TodayFocus(
                    data = data,
                    onOpenSchedule = onOpenSchedule,
                    onOpenHabits = onOpenHabits,
                    onFinishTask = viewModel::finishTask,
                    onCheckHabit = viewModel::checkHabit,
                )

                if (data.recent.isNotEmpty()) {
                    SectionLabel("最近")
                    data.recent.take(3).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(kindLabel(item.kind), color = Muted, modifier = Modifier.weight(0.2f))
                            Text(
                                item.title,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(item.value, color = Muted)
                        }
                    }
                    TextButton(onClick = onOpenArchive, modifier = Modifier.align(Alignment.Start)) {
                        Text("查看档案", color = Accent)
                    }
                }

                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("离开小院", color = Muted)
                }
            }
        }
    }
}

@Composable
private fun HeroHeader(data: TodayOverview) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Paper.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("朝夕小院", color = Accent, style = MaterialTheme.typography.labelLarge)
            Text(data.date, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("今天，慢慢来", style = MaterialTheme.typography.headlineMedium)
                Text(
                    data.quote,
                    color = Muted,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${data.score}", style = MaterialTheme.typography.displayLarge, color = Accent)
                Text("今日分", color = Muted, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun GlanceStrip(
    data: TodayOverview,
    onOpenLedger: () -> Unit,
    onOpenHabits: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenFitness: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GlanceCell(
            modifier = Modifier.weight(1f),
            label = "支出",
            value = "¥${yuan(data.finance.expenseCents)}",
            bg = Sand,
            onClick = onOpenLedger,
        )
        GlanceCell(
            modifier = Modifier.weight(1f),
            label = "习惯",
            value = if (data.habits.total == 0) "—" else "${data.habits.done}/${data.habits.total}",
            bg = Sage,
            onClick = onOpenHabits,
        )
        GlanceCell(
            modifier = Modifier.weight(1f),
            label = "待办",
            value = "${data.schedule.todayOpen}",
            bg = Lilac,
            onClick = onOpenSchedule,
        )
        GlanceCell(
            modifier = Modifier.weight(1f),
            label = "体重",
            value = data.fitness.weightKg?.let { trimNum(it) } ?: "—",
            bg = Blush,
            onClick = onOpenFitness,
        )
    }
}

@Composable
private fun GlanceCell(
    modifier: Modifier,
    label: String,
    value: String,
    bg: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .background(bg, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, color = Muted, style = MaterialTheme.typography.labelLarge)
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ModuleGrid(
    data: TodayOverview,
    onOpenLedger: () -> Unit,
    onOpenHabits: () -> Unit,
    onOpenFitness: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenBuy: () -> Unit,
    onOpenMedia: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val modules = listOf(
        ModuleItem("记账", "¥${yuan(data.finance.expenseCents)}", Sand, onOpenLedger),
        ModuleItem(
            "习惯",
            if (data.habits.total == 0) "去添加" else "${data.habits.done}/${data.habits.total}",
            Sage,
            onOpenHabits,
        ),
        ModuleItem("健身", data.fitness.weightKg?.let { "${trimNum(it)} kg" } ?: "去记录", Blush, onOpenFitness),
        ModuleItem("日程", "待办 ${data.schedule.todayOpen}", Lilac, onOpenSchedule),
        ModuleItem("待买", if (data.buy.openCount == 0) "清单空" else "${data.buy.openCount} 件", Paper, onOpenBuy),
        ModuleItem("书影音", "收藏", Card, onOpenMedia),
        ModuleItem("档案", "回看", Sand.copy(alpha = 0.7f), onOpenArchive),
        ModuleItem("设置", "资料", Clay.copy(alpha = 0.16f), onOpenSettings),
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        modules.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { item ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(item.background, RoundedCornerShape(18.dp))
                            .border(1.dp, Line.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                            .clickable(onClick = item.onClick)
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(item.title, style = MaterialTheme.typography.titleLarge)
                        Text(item.metric, color = Muted, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

private data class ModuleItem(
    val title: String,
    val metric: String,
    val background: Color,
    val onClick: () -> Unit,
)

@Composable
private fun TodayFocus(
    data: TodayOverview,
    onOpenSchedule: () -> Unit,
    onOpenHabits: () -> Unit,
    onFinishTask: (com.zhaoxi.xiaoyuan.data.today.TodayTask) -> Unit,
    onCheckHabit: (com.zhaoxi.xiaoyuan.data.today.TodayHabit) -> Unit,
) {
    SectionLabel("今天要做")

    val openTasks = data.schedule.items.filter { it.status != "done" }.take(3)
    val habits = data.habits.items.take(4)

    if (openTasks.isEmpty() && habits.isEmpty()) {
        Text("今天还空着，先从上面记一件小事。", color = Muted, style = MaterialTheme.typography.bodyLarge)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Card, RoundedCornerShape(18.dp))
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (openTasks.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("待办", color = Muted, style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = onOpenSchedule) { Text("全部", color = Accent) }
            }
            openTasks.forEach { task ->
                FocusRow(
                    title = task.title,
                    detail = task.note.ifBlank { "今天" },
                    action = "完成",
                    onAction = { onFinishTask(task) },
                )
            }
        }

        if (habits.isNotEmpty()) {
            if (openTasks.isNotEmpty()) Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("习惯", color = Muted, style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = onOpenHabits) { Text("全部", color = Accent) }
            }
            habits.forEach { habit ->
                val done = habit.value >= habit.dailyTarget
                FocusRow(
                    title = habit.name,
                    detail = if (habit.kind == "check") {
                        if (done) "已完成" else "待打卡"
                    } else {
                        "${trimNum(habit.value)} / ${trimNum(habit.dailyTarget)}"
                    },
                    action = if (habit.kind == "check") {
                        if (done) "已完成" else "打卡"
                    } else null,
                    onAction = if (habit.kind == "check") {{ onCheckHabit(habit) }} else null,
                )
            }
        }
    }
}

@Composable
private fun FocusRow(
    title: String,
    detail: String,
    action: String?,
    onAction: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) { Text(action, color = Accent) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.headlineMedium)
}

@Composable
private fun QuickPanels(viewModel: TodayViewModel, state: TodayUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf("ledger" to "记一笔", "schedule" to "排日程", "body" to "记体重", "buy" to "待买").forEach { (id, label) ->
            FilterChip(
                selected = state.panel == id,
                onClick = { viewModel.togglePanel(id) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Accent,
                    selectedLabelColor = OnAccent,
                ),
            )
        }
    }

    when (state.panel) {
        "ledger" -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField("金额", state.amount, viewModel::setAmount)
            if (state.categories.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.categories.take(6).forEach { cat ->
                        FilterChip(
                            selected = state.categoryId == cat.id,
                            onClick = { viewModel.setCategoryId(cat.id) },
                            label = { Text(cat.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Accent,
                                selectedLabelColor = OnAccent,
                            ),
                        )
                    }
                }
            }
            AppField("备注", state.note, viewModel::setNote)
            PrimaryAction("记下", enabled = !state.busy, onClick = viewModel::saveExpense)
        }
        "schedule" -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField("事项", state.scheduleTitle, viewModel::setScheduleTitle)
            AppField("日期 YYYY-MM-DD", state.scheduleDueOn, viewModel::setScheduleDueOn)
            PrimaryAction("安排", enabled = !state.busy, onClick = viewModel::saveSchedule)
        }
        "body" -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField("体重 kg", state.weight, viewModel::setWeight)
            AppField("摄入 kcal", state.intake, viewModel::setIntake)
            PrimaryAction("记下", enabled = !state.busy, onClick = viewModel::saveBody)
        }
        "buy" -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField("想买", state.buyTitle, viewModel::setBuyTitle)
            AppField("估价", state.buyAmount, viewModel::setBuyAmount)
            PrimaryAction("放入清单", enabled = !state.busy, onClick = viewModel::saveBuy)
        }
    }
}

@Composable
private fun PrimaryAction(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
    ) { Text(label) }
}

private fun kindLabel(kind: String) = when (kind) {
    "finance" -> "财务"
    "health" -> "健康"
    "schedule" -> "日程"
    "buy" -> "待买"
    "habit" -> "习惯"
    else -> kind
}

private fun trimNum(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
