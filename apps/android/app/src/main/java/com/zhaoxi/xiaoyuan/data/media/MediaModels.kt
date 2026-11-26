package com.zhaoxi.xiaoyuan.data.media

import kotlinx.serialization.Serializable

@Serializable
data class MediaItem(
    val id: String,
    val kind: String,
    val title: String,
    val creator: String = "",
    val coverUrl: String = "",
    val status: String,
    val rating: Double? = null,
    val note: String = "",
)

@Serializable
data class MediaKindCount(
    val kind: String,
    val count: Int = 0,
)

@Serializable
data class MediaStats(
    val year: String,
    val doneCount: Int = 0,
    val averageRating: Double? = null,
    val kinds: List<MediaKindCount> = emptyList(),
)

@Serializable
data class NewMedia(
    val kind: String,
    val title: String,
    val creator: String = "",
    val coverUrl: String = "",
    val status: String,
    val note: String = "",
    val rating: Double? = null,
)

@Serializable
data class CoverUploadResult(
    val url: String,
)
