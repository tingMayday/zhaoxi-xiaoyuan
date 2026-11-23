package com.zhaoxi.xiaoyuan.data.archive

import kotlinx.serialization.Serializable

@Serializable
data class ArchiveBoard(
    val today: String,
    val days: List<ArchiveDay> = emptyList(),
)

@Serializable
data class ArchiveDay(
    val date: String,
    val items: List<ArchiveEntry> = emptyList(),
)

@Serializable
data class ArchiveEntry(
    val kind: String,
    val title: String,
    val detail: String = "",
    val value: String = "",
)
