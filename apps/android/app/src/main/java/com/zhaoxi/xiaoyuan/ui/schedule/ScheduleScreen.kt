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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhaoxi.xiaoyuan.notify.scheduleMeta
import com.zhaoxi.xiaoyuan.ui.common.ActionButton
import com.zhaoxi.xiaoyuan.ui.common.ActionTone
import com.zhaoxi.xiaoyuan.ui.common.AppField
import com.zhaoxi.xiaoyuan.ui.common.ConfirmDialog
import com.zhaoxi.xiaoyuan.ui.common.EmptyBox
import com.zhaoxi.xiaoyuan.ui.common.PageHeader
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Lilac
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Sage

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    highlightId: String? = null,
) {
    val state by viewModel.ui.collectAsStateWithLifecycle()

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
        PageHeader(eyebrow = "日程", title = "今天要做的")

        if (state.loading && state.board == null) {
            CircularProgressIndicator(color = Accent, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            AppField("事项", state.title, viewModel::setTitle)
            AppField("日期", state.dueOn, viewModel::setDueOn, kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.Date)
            AppField("提醒", state.remindAt, viewModel::setRemindAt, kind = com.zhaoxi.xiaoyuan.ui.common.FieldKind.DateTime)
            AppField("备注", state.note, viewModel::setNote)
            state.error?.let { Text(it, color = Accent) }
            Button(
                onClick = viewModel::create,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
            ) { Text("安排") }

            state.board?.groups()?.forEach { (title, items) ->
                Text("$title · ${items.size}", style = MaterialTheme.typography.headlineMedium)
                if (items.isEmpty()) {
                    EmptyBox(
                        when (title) {
                            "今天" -> "今天还空着。"
                            "逾期" -> "没有拖着的事。"
                            else -> "接下来一周是轻的。"
                        },
                    )
                } else {
                    items.forEach { item ->
                        val highlighted = highlightId != null && highlightId == item.id
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when {
                                        highlighted -> Lilac.copy(alpha = 0.55f)
                                        item.status == "done" -> Sage.copy(alpha = 0.4f)
                                        else -> Card
                                    },
                                )
                                .border(1.dp, if (highlighted) Accent else Line, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    scheduleMeta(item.dueOn, item.remindAt, item.note),
                                    color = Muted,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ActionButton(
                                    label = if (item.status == "done") "已完成" else "完成",
                                    onClick = { viewModel.askToggle(item) },
                                    tone = if (item.status == "done") ActionTone.Soft else ActionTone.Primary,
                                    modifier = Modifier.weight(1f),
                                )
                                ActionButton(
                                    label = "删除",
                                    onClick = { viewModel.askDelete(item) },
                                    tone = ActionTone.Danger,
                                    modifier = Modifier.weight(1f),
                                )
                            }
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
