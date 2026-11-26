package com.zhaoxi.xiaoyuan.ui.media

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.zhaoxi.xiaoyuan.BuildConfig
import com.zhaoxi.xiaoyuan.data.media.MediaItem
import com.zhaoxi.xiaoyuan.data.media.MediaStats
import com.zhaoxi.xiaoyuan.ui.common.ActionButton
import com.zhaoxi.xiaoyuan.ui.common.ActionTone
import com.zhaoxi.xiaoyuan.ui.common.AppField
import com.zhaoxi.xiaoyuan.ui.common.ConfirmDialog
import com.zhaoxi.xiaoyuan.ui.common.EmptyBox
import com.zhaoxi.xiaoyuan.ui.common.PageHeader
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Clay
import com.zhaoxi.xiaoyuan.ui.theme.Line
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.OnAccent
import com.zhaoxi.xiaoyuan.ui.theme.Paper
import com.zhaoxi.xiaoyuan.ui.theme.Sand

@Composable
fun MediaScreen(viewModel: MediaViewModel) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val pickCover = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.uploadCover(context, uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PageHeader(eyebrow = "收藏", title = "书影音")

        state.stats?.let { StatsBlock(it) }

        if (state.loading && state.items.isEmpty()) {
            CircularProgressIndicator(color = Accent, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            ChipRow(
                options = listOf("book" to "图书", "film" to "电影", "series" to "剧集", "album" to "专辑"),
                selected = state.kind,
                onSelect = viewModel::setKind,
            )
            AppField("名称", state.title, viewModel::setTitle)
            AppField("作者 / 导演", state.creator, viewModel::setCreator)
            ChipRow(
                options = listOf("want" to "想看", "doing" to "在看", "done" to "看完", "dropped" to "搁置"),
                selected = state.status,
                onSelect = viewModel::setStatus,
            )
            AppField("短评", state.note, viewModel::setNote)
            StarRating(value = state.rating, onChange = viewModel::setRating)
            CoverPicker(
                kind = state.kind,
                coverUrl = state.coverUrl,
                uploading = state.coverUploading,
                onPick = {
                    pickCover.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                onClear = viewModel::clearCover,
            )

            state.error?.let { Text(it, color = Accent) }
            Button(
                onClick = viewModel::create,
                enabled = !state.busy && !state.coverUploading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
            ) { Text("收入收藏") }

            ChipRow(
                options = listOf(
                    "" to "全部",
                    "want" to "想看",
                    "doing" to "在看",
                    "done" to "看完",
                    "dropped" to "搁置",
                ),
                selected = state.filterStatus,
                onSelect = viewModel::setFilterStatus,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("展示", color = Muted, style = MaterialTheme.typography.labelLarge)
                ViewChip(
                    label = "封面墙",
                    selected = state.viewMode == MediaViewMode.Wall,
                    onClick = { viewModel.setViewMode(MediaViewMode.Wall) },
                )
                ViewChip(
                    label = "列表",
                    selected = state.viewMode == MediaViewMode.List,
                    onClick = { viewModel.setViewMode(MediaViewMode.List) },
                )
            }

            val shown = state.shown
            if (shown.isEmpty()) {
                EmptyBox("这里还空着。把最近看完的一部先放进来。")
            } else if (state.viewMode == MediaViewMode.Wall) {
                WallGallery(items = shown, onOpen = viewModel::openDetail)
            } else {
                ListGallery(items = shown, onOpen = viewModel::openDetail)
            }
        }
    }

    state.pendingDetail?.let { item ->
        MediaDetailDialog(
            item = item,
            onDismiss = viewModel::closeDetail,
            onDelete = { viewModel.askDelete(item) },
        )
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

@Composable
private fun StatsBlock(stats: MediaStats) {
    val topKind = stats.kinds.maxByOrNull { it.count }
        ?.takeIf { it.count > 0 }
        ?.let { kindLabel(it.kind) }
        ?: "—"
    val maxKind = (stats.kinds.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatCell(modifier = Modifier.weight(1f), label = "${stats.year} 看完", value = "${stats.doneCount}")
            StatCell(
                modifier = Modifier.weight(1f),
                label = "平均星级",
                value = stats.averageRating?.toString() ?: "—",
            )
            StatCell(modifier = Modifier.weight(1f), label = "最多的一类", value = topKind)
        }
        stats.kinds.forEach { item ->
            KindMeter(
                label = kindLabel(item.kind),
                value = "${item.count}",
                fraction = item.count.toFloat() / maxKind.toFloat(),
            )
        }
    }
}

@Composable
private fun StatCell(modifier: Modifier, label: String, value: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Sand)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, color = Muted, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun KindMeter(label: String, value: String, fraction: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, color = Muted, style = MaterialTheme.typography.labelLarge)
            Text(value, color = Muted, style = MaterialTheme.typography.labelLarge)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Paper.copy(alpha = 0.7f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction.coerceIn(0.04f, 1f))
                    .clip(RoundedCornerShape(999.dp))
                    .background(Accent.copy(alpha = 0.55f)),
            )
        }
    }
}

@Composable
private fun ChipRow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (id, label) ->
            FilterChip(
                selected = selected == id,
                onClick = { onSelect(id) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Accent,
                    selectedLabelColor = OnAccent,
                ),
            )
        }
    }
}

@Composable
private fun ViewChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) OnAccent else Accent,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Accent else Card)
            .border(1.dp, if (selected) Accent else Line, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun StarRating(value: Int, onChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("星级", color = Muted, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            (1..5).forEach { star ->
                val on = star <= value
                Icon(
                    imageVector = if (on) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "$star 星",
                    tint = if (on) Clay else Muted,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            onChange(if (value == star) 0 else star)
                        },
                )
            }
        }
        if (value > 0) {
            Text("$value 星", color = Muted, style = MaterialTheme.typography.bodyMedium)
        } else {
            Text("未评分", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CoverPicker(
    kind: String,
    coverUrl: String,
    uploading: Boolean,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("封面", color = Muted, style = MaterialTheme.typography.labelLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = resolveCoverUrl(kind, coverUrl),
                contentDescription = "封面预览",
                modifier = Modifier
                    .width(72.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Paper)
                    .border(1.dp, Line, RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton(
                    label = when {
                        uploading -> "上传中…"
                        coverUrl.isNotBlank() -> "已选封面"
                        else -> "选择封面"
                    },
                    onClick = onPick,
                    tone = ActionTone.Primary,
                    enabled = !uploading,
                )
                if (coverUrl.isNotBlank()) {
                    ActionButton(
                        label = "清除",
                        onClick = onClear,
                        tone = ActionTone.Soft,
                        enabled = !uploading,
                    )
                }
            }
        }
    }
}

@Composable
private fun WallGallery(items: List<MediaItem>, onOpen: (MediaItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { item ->
                    WallCard(
                        item = item,
                        onOpen = { onOpen(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WallCard(item: MediaItem, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Card)
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .clickable(onClick = onOpen)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AsyncImage(
            model = resolveCoverUrl(item.kind, item.coverUrl),
            contentDescription = item.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Paper),
            contentScale = ContentScale.Crop,
        )
        Text(
            item.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            "${kindLabel(item.kind)} · ${statusLabel(item.status)} · ${ratingText(item.rating)}",
            color = Muted,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ListGallery(items: List<MediaItem>, onOpen: (MediaItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Card)
                    .border(1.dp, Line, RoundedCornerShape(14.dp))
                    .clickable { onOpen(item) }
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = resolveCoverUrl(item.kind, item.coverUrl),
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Paper),
                    contentScale = ContentScale.Crop,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        listIntro(item),
                        color = Muted,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    "${statusLabel(item.status)} ${item.rating?.toInt() ?: "—"}",
                    color = Muted,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun MediaDetailDialog(
    item: MediaItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Card,
        title = { Text(item.title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = resolveCoverUrl(item.kind, item.coverUrl),
                    contentDescription = item.title,
                    modifier = Modifier
                        .width(160.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Paper)
                        .border(1.dp, Line, RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop,
                )
                DetailLine(label = "类型", value = kindLabel(item.kind))
                DetailLine(label = "状态", value = statusLabel(item.status))
                DetailLine(label = "星级", value = ratingText(item.rating))
                DetailLine(
                    label = "作者 / 导演",
                    value = item.creator.ifBlank { "—" },
                )
                DetailLine(
                    label = "短评",
                    value = item.note.ifBlank { "还没有短评" },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭", color = Accent) }
        },
        dismissButton = {
            TextButton(onClick = onDelete) { Text("删除", color = Clay) }
        },
    )
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label, color = Muted, style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun listIntro(item: MediaItem): String {
    val parts = listOf(item.creator, item.note).map { it.trim() }.filter { it.isNotBlank() }
    return if (parts.isEmpty()) "还没有介绍" else parts.joinToString(" · ")
}

private fun ratingText(rating: Double?): String =
    if (rating == null || rating <= 0) "未评分" else "${rating.toInt()} 星"

private fun resolveCoverUrl(kind: String, coverUrl: String): String {
    val base = BuildConfig.API_BASE_URL.trimEnd('/')
    val raw = coverUrl.trim()
    return when {
        raw.startsWith("http") -> raw
        raw.startsWith("/") -> base + raw
        raw.isNotBlank() -> "$base/$raw"
        else -> "$base/covers/$kind.svg"
    }
}
