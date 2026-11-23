package com.zhaoxi.xiaoyuan.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.zhaoxi.xiaoyuan.BuildConfig
import com.zhaoxi.xiaoyuan.ui.common.AppField
import com.zhaoxi.xiaoyuan.ui.common.ConfirmDialog
import com.zhaoxi.xiaoyuan.ui.common.EmptyBox
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.Paper

@Composable
fun MediaScreen(viewModel: MediaViewModel, onBack: () -> Unit) {
    val state by viewModel.ui.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) { Text("返回", color = Accent) }
        Text("收藏", color = Muted, style = MaterialTheme.typography.labelLarge)
        Text("书影音", style = MaterialTheme.typography.headlineLarge)

        state.stats?.let { stats ->
            Text("${stats.year} 看完 ${stats.doneCount} · 平均 ${stats.averageRating ?: "—"} 星", color = Muted)
        }

        if (state.loading && state.items.isEmpty()) {
            CircularProgressIndicator(color = Accent, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("book" to "图书", "film" to "电影", "series" to "剧集", "album" to "专辑").forEach { (id, label) ->
                    FilterChip(
                        selected = state.kind == id,
                        onClick = { viewModel.setKind(id) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Accent, selectedLabelColor = OnAccent),
                    )
                }
            }
            AppField("名称", state.title, viewModel::setTitle)
            AppField("作者 / 导演", state.creator, viewModel::setCreator)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("want" to "想看", "doing" to "在看", "done" to "看完", "dropped" to "搁置").forEach { (id, label) ->
                    FilterChip(
                        selected = state.status == id,
                        onClick = { viewModel.setStatus(id) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Accent, selectedLabelColor = OnAccent),
                    )
                }
            }
            AppField("短评", state.note, viewModel::setNote)
            state.error?.let { Text(it, color = Accent) }
            Button(
                onClick = viewModel::create,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
            ) { Text("收入收藏") }

            if (state.items.isEmpty()) {
                EmptyBox("这里还空着。把最近看完的一部先放进来。")
            } else {
                state.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Card, RoundedCornerShape(16.dp))
                            .border(1.dp, Line, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AsyncImage(
                            model = coverUrl(item.kind, item.coverUrl),
                            contentDescription = item.title,
                            modifier = Modifier
                                .width(72.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Paper),
                            contentScale = ContentScale.Crop,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleLarge)
                            Text("${kindLabel(item.kind)} · ${statusLabel(item.status)}", color = Muted)
                            if (item.note.isNotBlank()) Text(item.note, color = Muted)
                            TextButton(onClick = { viewModel.askDelete(item) }) { Text("删除", color = Accent) }
                        }
                    }
                }
            }
        }
    }

    state.pendingDelete?.let { item ->
        ConfirmDialog(
            title = "删除收藏",
            message = "删除后，「${item.title}」就不会再出现在收藏里。",
            confirmText = "删除",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete,
        )
    }
}

private fun coverUrl(kind: String, coverUrl: String): String {
    val base = BuildConfig.API_BASE_URL.trimEnd('/')
    val raw = coverUrl.trim()
    return when {
        raw.startsWith("http") -> raw
        raw.startsWith("/") -> base + raw
        raw.isNotBlank() -> "$base/$raw"
        else -> "$base/covers/$kind.svg"
    }
}
