package com.zhaoxi.xiaoyuan.ui.today

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhaoxi.xiaoyuan.R
import com.zhaoxi.xiaoyuan.data.today.TodayOverview
import com.zhaoxi.xiaoyuan.ui.common.ActionButton
import com.zhaoxi.xiaoyuan.ui.common.ActionTone
import com.zhaoxi.xiaoyuan.ui.common.AppField
import com.zhaoxi.xiaoyuan.ui.common.SoftYardPattern
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Blush
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Clay
import com.zhaoxi.xiaoyuan.ui.theme.Ink
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

    // 从子页返回或切回前台时刷新首页总览
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
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
                    RecentList(
                        items = data.recent.take(4),
                        onOpenArchive = onOpenArchive,
                    )
                }

                SoftButton(
                    label = "离开小院",
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    muted = true,
                )
            }
        }
    }
}

@Composable
private fun HeroHeader(data: TodayOverview) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Paper.copy(alpha = 0.5f)),
    ) {
        SoftYardPattern(
            modifier = Modifier
                .matchParentSize()
                .padding(8.dp),
        )
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Card),
                    )
                    Column {
                        Text("朝夕小院", color = Accent, style = MaterialTheme.typography.titleMedium)
                        Text(data.date, color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Card.copy(alpha = 0.85f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text("${data.score}", style = MaterialTheme.typography.headlineLarge, color = Accent)
                    Text("今日分", color = Muted, style = MaterialTheme.typography.labelLarge)
                }
            }
            Text("今天，慢慢来", style = MaterialTheme.typography.headlineMedium)
            Text(
                data.quote,
                color = Muted,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, color = Muted, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        com.zhaoxi.xiaoyuan.ui.common.MoneyValue(
            value = value,
            label = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
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
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(item.background)
                            .border(1.dp, Line.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                            .clickable(onClick = item.onClick)
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Accent.copy(alpha = 0.55f)),
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(item.title, style = MaterialTheme.typography.titleLarge)
                            Text(
                                item.metric,
                                color = Muted,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
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
        Text(
            "今天还空着，先从上面记一件小事。",
            color = Muted,
            style = MaterialTheme.typography.bodyLarge,
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Card)
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (openTasks.isNotEmpty()) {
            FocusGroupHeader(label = "待办", action = "全部", onAction = onOpenSchedule)
            openTasks.forEach { task ->
                FocusRow(
                    title = task.title,
                    detail = task.note.ifBlank { "今天" },
                    action = "完成",
                    done = false,
                    onAction = { onFinishTask(task) },
                )
            }
        }

        if (habits.isNotEmpty()) {
            if (openTasks.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .height(1.dp)
                        .background(Line.copy(alpha = 0.7f)),
                )
            }
            FocusGroupHeader(label = "习惯", action = "全部", onAction = onOpenHabits)
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
                    } else {
                        null
                    },
                    done = done && habit.kind == "check",
                    onAction = if (habit.kind == "check" && !done) {
                        { onCheckHabit(habit) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun FocusGroupHeader(label: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Muted, style = MaterialTheme.typography.labelLarge)
        Text(
            action,
            color = Accent,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onAction)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun FocusRow(
    title: String,
    detail: String,
    action: String?,
    done: Boolean,
    onAction: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (done) Sage.copy(alpha = 0.55f) else Paper.copy(alpha = 0.35f))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (done) Sage else Accent.copy(alpha = 0.7f)),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (done) Muted else Ink,
            )
            Text(detail, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        when {
            done && action != null -> {
                Text(
                    action,
                    color = Muted,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .background(Card, RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            action != null && onAction != null -> {
                ActionButton(
                    label = action,
                    onClick = onAction,
                    tone = ActionTone.Primary,
                )
            }
        }
    }
}

@Composable
private fun RecentList(
    items: List<com.zhaoxi.xiaoyuan.data.today.TodayRecent>,
    onOpenArchive: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Card)
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Paper.copy(alpha = 0.35f))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    kindLabel(item.kind),
                    color = Accent,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .background(kindTint(item.kind), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
                Text(
                    item.title,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(item.value, color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
        }
        SoftButton(
            label = "查看档案",
            onClick = onOpenArchive,
            modifier = Modifier.fillMaxWidth(),
        )
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
            val selected = state.panel == id
            Text(
                text = label,
                color = if (selected) OnAccent else Ink,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected) Accent else Paper.copy(alpha = 0.55f))
                    .border(
                        width = 1.dp,
                        color = if (selected) Accent else Line,
                        shape = RoundedCornerShape(999.dp),
                    )
                    .clickable { viewModel.togglePanel(id) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }

    when (state.panel) {
        "ledger" -> QuickForm {
            AppField("金额", state.amount, viewModel::setAmount, kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.Decimal)
            if (state.categories.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.categories.take(6).forEach { cat ->
                        val selected = state.categoryId == cat.id
                        Text(
                            text = cat.name,
                            color = if (selected) OnAccent else Ink,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (selected) Accent else Sand.copy(alpha = 0.7f))
                                .clickable { viewModel.setCategoryId(cat.id) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
            AppField("备注", state.note, viewModel::setNote)
            PrimaryAction("记下", enabled = !state.busy, onClick = viewModel::saveExpense)
        }
        "schedule" -> QuickForm {
            AppField("事项", state.scheduleTitle, viewModel::setScheduleTitle)
            AppField(
                "日期",
                state.scheduleDueOn,
                viewModel::setScheduleDueOn,
                kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.Date,
            )
            AppField(
                "提醒",
                state.scheduleRemindAt,
                viewModel::setScheduleRemindAt,
                kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.DateTime,
            )
            AppField("备注", state.scheduleNote, viewModel::setScheduleNote)
            PrimaryAction("安排", enabled = !state.busy, onClick = viewModel::saveSchedule)
        }
        "body" -> QuickForm {
            AppField(
                "体重 kg",
                state.weight,
                viewModel::setWeight,
                kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.Decimal,
            )
            AppField(
                "摄入 kcal",
                state.intake,
                viewModel::setIntake,
                kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.Number,
            )
            PrimaryAction("记下", enabled = !state.busy, onClick = viewModel::saveBody)
        }
        "buy" -> QuickForm {
            AppField("想买", state.buyTitle, viewModel::setBuyTitle)
            AppField(
                "估价",
                state.buyAmount,
                viewModel::setBuyAmount,
                kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.Decimal,
            )
            PrimaryAction("放入清单", enabled = !state.busy, onClick = viewModel::saveBuy)
        }
    }
}

@Composable
private fun QuickForm(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Card)
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
private fun PrimaryAction(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent,
            contentColor = OnAccent,
            disabledContainerColor = Accent.copy(alpha = 0.35f),
            disabledContentColor = OnAccent.copy(alpha = 0.8f),
        ),
    ) { Text(label, style = MaterialTheme.typography.titleMedium) }
}

@Composable
private fun SoftButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, if (muted) Line.copy(alpha = 0.7f) else Line, RoundedCornerShape(14.dp))
            .background(if (muted) Card else Paper.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (muted) Muted else Accent,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
}

private fun kindTint(kind: String): Color = when (kind) {
    "finance" -> Sand
    "health" -> Blush
    "schedule" -> Lilac
    "buy" -> Paper
    "habit" -> Sage
    else -> Paper.copy(alpha = 0.5f)
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
