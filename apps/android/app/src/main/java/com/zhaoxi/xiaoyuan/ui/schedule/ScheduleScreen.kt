package com.zhaoxi.xiaoyuan.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhaoxi.xiaoyuan.ui.common.AppField
import com.zhaoxi.xiaoyuan.ui.common.ConfirmDialog
import com.zhaoxi.xiaoyuan.ui.common.EmptyBox
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel, onBack: () -> Unit) {
    val state by viewModel.ui.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) { Text("返回", color = Accent) }
        Text("日程", color = Muted, style = MaterialTheme.typography.labelLarge)
        Text("把日子排开", style = MaterialTheme.typography.headlineLarge)

        if (state.loading && state.board == null) {
            CircularProgressIndicator(color = Accent, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            AppField("标题", state.title, viewModel::setTitle)
            AppField("日期", state.dueOn, viewModel::setDueOn)
            AppField("备注", state.note, viewModel::setNote)
            state.error?.let { Text(it, color = Accent) }
            Button(
                onClick = viewModel::create,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
            ) { Text("加入日程") }

            state.board?.groups()?.forEach { (title, items) ->
                Text("$title · ${items.size}", style = MaterialTheme.typography.headlineMedium)
                if (items.isEmpty()) {
                    EmptyBox(if (title == "今天") "今天还空着。" else "这一栏暂时空着。")
                } else {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Card, RoundedCornerShape(14.dp))
                                .border(1.dp, Line, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title)
                                Text("${item.dueOn.takeLast(5)}${if (item.note.isBlank()) "" else " · ${item.note}"}", color = Muted)
                            }
                            TextButton(onClick = { viewModel.askToggle(item) }) {
                                Text(if (item.status == "done") "已完成" else "完成", color = Accent)
                            }
                            TextButton(onClick = { viewModel.askDelete(item) }) { Text("删除", color = Muted) }
                        }
                    }
                }
            }
        }
    }

    state.pendingToggle?.let { item ->
        val done = item.status != "done"
        ConfirmDialog(
            title = if (done) "完成日程" else "取消完成",
            message = if (done) "把「${item.title}」标成完成？" else "把「${item.title}」改回未完成？",
            confirmText = if (done) "完成" else "改回",
            onConfirm = viewModel::confirmToggle,
            onDismiss = viewModel::cancelDialog,
        )
    }
    state.pendingDelete?.let { item ->
        ConfirmDialog(
            title = "删除日程",
            message = "删除后，「${item.title}」就不会再出现。",
            confirmText = "删除",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDialog,
        )
    }
}
