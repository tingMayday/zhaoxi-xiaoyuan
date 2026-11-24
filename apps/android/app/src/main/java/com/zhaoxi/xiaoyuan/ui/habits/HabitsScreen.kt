package com.zhaoxi.xiaoyuan.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhaoxi.xiaoyuan.data.habits.Habit
import com.zhaoxi.xiaoyuan.ui.common.AppField
import com.zhaoxi.xiaoyuan.ui.common.ConfirmDialog
import com.zhaoxi.xiaoyuan.ui.common.EmptyBox
import com.zhaoxi.xiaoyuan.ui.common.PageHeader
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Paper
import com.zhaoxi.xiaoyuan.ui.theme.Sage

@Composable
fun HabitsScreen(viewModel: HabitsViewModel, onBack: () -> Unit) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val today = state.board?.today.orEmpty()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PageHeader(onBack = onBack, eyebrow = "习惯", title = "一点点积累")

        if (state.loading && state.board == null) {
            CircularProgressIndicator(color = Accent, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Text("新习惯", style = MaterialTheme.typography.headlineMedium)
            AppField("名称", state.name, viewModel::setName)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("check" to "打卡", "count" to "计数", "value" to "数值").forEach { (id, label) ->
                    FilterChip(
                        selected = state.kind == id,
                        onClick = { viewModel.setKind(id) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Accent,
                            selectedLabelColor = OnAccent,
                        ),
                    )
                }
            }
            if (state.kind != "check") {
                AppField("单位", state.unit, viewModel::setUnit)
                AppField("每日目标", state.target, viewModel::setTarget)
            }
            state.error?.let { Text(it, color = Accent) }
            Button(
                onClick = viewModel::create,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
            ) { Text("加入") }

            Text("今天", style = MaterialTheme.typography.headlineMedium)
            val habits = state.board?.habits.orEmpty()
            if (habits.isEmpty()) {
                EmptyBox("还没有习惯。先加一件今天就能做的小事。")
            } else {
                habits.forEach { habit ->
                    val value = habit.logs.find { it.date == today }?.value ?: 0.0
                    val done = value >= habit.dailyTarget
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Card, RoundedCornerShape(16.dp))
                            .border(1.dp, Line, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(habit.name, style = MaterialTheme.typography.titleLarge)
                        Text(
                            when (habit.kind) {
                                "check" -> if (done) "今天已完成" else "待打卡"
                                else -> "${trimNum(value)} / ${trimNum(habit.dailyTarget)}${if (habit.unit.isBlank()) "" else " ${habit.unit}"}"
                            },
                            color = Muted,
                        )
                        HabitHeatmap(habit = habit, days = state.board?.days.orEmpty())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            when (habit.kind) {
                                "check" -> TextButton(onClick = { viewModel.askCheck(habit) }) {
                                    Text(if (done) "已完成" else "打卡", color = Accent)
                                }
                                else -> {
                                    TextButton(onClick = { viewModel.bump(habit, -1.0) }) { Text("−", color = Accent) }
                                    Text(trimNum(value))
                                    TextButton(onClick = { viewModel.bump(habit, 1.0) }) { Text("+", color = Accent) }
                                }
                            }
                            TextButton(onClick = { viewModel.askArchive(habit) }) { Text("归档", color = Muted) }
                        }
                    }
                }
            }
        }
    }

    state.pendingCheck?.let { habit ->
        val todayValue = habit.logs.find { it.date == today }?.value ?: 0.0
        val done = todayValue >= habit.dailyTarget
        ConfirmDialog(
            title = if (done) "取消完成" else "完成习惯",
            message = if (done) "取消「${habit.name}」今天的完成？" else "把「${habit.name}」记成今天完成？",
            confirmText = if (done) "改回" else "完成",
            onConfirm = viewModel::confirmCheck,
            onDismiss = viewModel::cancelDialog,
        )
    }
    state.pendingArchive?.let { habit ->
        ConfirmDialog(
            title = "归档习惯",
            message = "归档后，「${habit.name}」不会再出现在列表里。",
            confirmText = "归档",
            onConfirm = viewModel::confirmArchive,
            onDismiss = viewModel::cancelDialog,
        )
    }
}

@Composable
private fun HabitHeatmap(habit: Habit, days: List<String>) {
    if (days.isEmpty()) return
    val byDate = habit.logs.associateBy { it.date }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("近 ${days.size} 天", color = Muted, style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            days.takeLast(30).forEach { date ->
                val value = byDate[date]?.value ?: 0.0
                val ratio = if (habit.dailyTarget <= 0) 0f else (value / habit.dailyTarget).toFloat().coerceIn(0f, 1f)
                val color = when {
                    ratio <= 0f -> Paper
                    ratio < 1f -> Sage.copy(alpha = 0.45f + ratio * 0.35f)
                    else -> Accent.copy(alpha = 0.85f)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(color, RoundedCornerShape(3.dp))
                        .border(0.5.dp, Line.copy(alpha = 0.5f), RoundedCornerShape(3.dp)),
                )
            }
        }
    }
}

private fun trimNum(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
