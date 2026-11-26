package com.zhaoxi.xiaoyuan.ui.media

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zhaoxi.xiaoyuan.data.media.MediaItem
import com.zhaoxi.xiaoyuan.data.media.MediaRepository
import com.zhaoxi.xiaoyuan.data.media.MediaStats
import com.zhaoxi.xiaoyuan.data.media.NewMedia
import com.zhaoxi.xiaoyuan.data.network.toUserMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MediaViewMode { Wall, List }

data class MediaUiState(
    val loading: Boolean = true,
    val items: List<MediaItem> = emptyList(),
    val stats: MediaStats? = null,
    val kind: String = "film",
    val title: String = "",
    val creator: String = "",
    val status: String = "want",
    val note: String = "",
    val rating: Int = 0,
    val coverUrl: String = "",
    val coverUploading: Boolean = false,
    val filterStatus: String = "",
    val viewMode: MediaViewMode = MediaViewMode.Wall,
    val error: String? = null,
    val busy: Boolean = false,
    val pendingDetail: MediaItem? = null,
    val pendingDelete: MediaItem? = null,
) {
    val shown: List<MediaItem>
        get() = if (filterStatus.isBlank()) items else items.filter { it.status == filterStatus }
}

class MediaViewModel(private val repo: MediaRepository) : ViewModel() {
    private val _ui = MutableStateFlow(MediaUiState())
    val ui: StateFlow<MediaUiState> = _ui.asStateFlow()

    init {
        refresh()
    }

    fun setKind(v: String) = _ui.update { it.copy(kind = v) }
    fun setTitle(v: String) = _ui.update { it.copy(title = v, error = null) }
    fun setCreator(v: String) = _ui.update { it.copy(creator = v) }
    fun setStatus(v: String) = _ui.update { it.copy(status = v) }
    fun setNote(v: String) = _ui.update { it.copy(note = v) }
    fun setRating(v: Int) = _ui.update { it.copy(rating = v.coerceIn(0, 5)) }
    fun clearCover() = _ui.update { it.copy(coverUrl = "") }
    fun setFilterStatus(v: String) = _ui.update { it.copy(filterStatus = v) }
    fun setViewMode(mode: MediaViewMode) = _ui.update { it.copy(viewMode = mode) }
    fun openDetail(item: MediaItem) = _ui.update { it.copy(pendingDetail = item) }
    fun closeDetail() = _ui.update { it.copy(pendingDetail = null) }
    fun askDelete(item: MediaItem) = _ui.update { it.copy(pendingDetail = null, pendingDelete = item) }
    fun cancelDelete() = _ui.update { it.copy(pendingDelete = null) }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching { repo.list() to repo.stats() }
                .onSuccess { (items, stats) ->
                    _ui.update { it.copy(loading = false, items = items, stats = stats) }
                }
                .onFailure { err ->
                    _ui.update { it.copy(loading = false, error = err.toUserMessage()) }
                }
        }
    }

    fun uploadCover(context: Context, uri: Uri) {
        viewModelScope.launch {
            _ui.update { it.copy(coverUploading = true, error = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    val resolver = context.contentResolver
                    val mime = resolver.getType(uri) ?: "image/jpeg"
                    val ext = when {
                        mime.contains("png") -> "png"
                        mime.contains("webp") -> "webp"
                        mime.contains("gif") -> "gif"
                        else -> "jpg"
                    }
                    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: error("无法读取图片")
                    if (bytes.size > 5 * 1024 * 1024) error("封面需小于 5MB")
                    repo.uploadCover(bytes, "cover.$ext", mime)
                }
            }.onSuccess { result ->
                _ui.update { it.copy(coverUploading = false, coverUrl = result.url) }
            }.onFailure { err ->
                _ui.update { it.copy(coverUploading = false, error = err.toUserMessage()) }
            }
        }
    }

    fun create() {
        val state = _ui.value
        if (state.title.isBlank()) {
            _ui.update { it.copy(error = "请填写名称") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null) }
            runCatching {
                repo.create(
                    NewMedia(
                        kind = state.kind,
                        title = state.title.trim(),
                        creator = state.creator.trim(),
                        coverUrl = state.coverUrl.trim(),
                        status = state.status,
                        note = state.note.trim(),
                        rating = state.rating.takeIf { it > 0 }?.toDouble(),
                    ),
                )
            }.onSuccess {
                _ui.update {
                    it.copy(
                        busy = false,
                        title = "",
                        creator = "",
                        note = "",
                        rating = 0,
                        coverUrl = "",
                    )
                }
                refresh()
            }.onFailure { err ->
                _ui.update { it.copy(busy = false, error = err.toUserMessage()) }
            }
        }
    }

    fun confirmDelete() {
        val item = _ui.value.pendingDelete ?: return
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, pendingDelete = null, error = null) }
            runCatching { repo.remove(item.id) }
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
        fun factory(repo: MediaRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MediaViewModel(repo) as T
        }
    }
}

fun kindLabel(kind: String) = when (kind) {
    "book" -> "图书"
    "film" -> "电影"
    "series" -> "剧集"
    "album" -> "专辑"
    else -> kind
}

fun statusLabel(status: String) = when (status) {
    "want" -> "想看"
    "doing" -> "在看"
    "done" -> "看完"
    "dropped" -> "搁置"
    else -> status
}
