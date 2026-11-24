package com.zhaoxi.xiaoyuan.notify

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val localInput = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
private val localDisplay = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private val clockOnly = DateTimeFormatter.ofPattern("HH:mm")

/** 把表单里的本地时间（YYYY-MM-DDTHH:mm 或带空格）转成 ISO，供 API 使用。 */
fun localDateTimeToIso(input: String): String? {
    val trimmed = input.trim().replace(' ', 'T')
    if (trimmed.isEmpty()) return null
    return runCatching {
        when {
            trimmed.length >= 16 && !trimmed.endsWith("Z") && !trimmed.contains('+') -> {
                val local = LocalDateTime.parse(trimmed.take(16), localInput)
                local.atZone(ZoneId.systemDefault()).toInstant().toString()
            }
            else -> Instant.parse(trimmed).toString()
        }
    }.getOrNull()
}

fun isoToMillis(iso: String?): Long? {
    if (iso.isNullOrBlank()) return null
    return runCatching { Instant.parse(iso).toEpochMilli() }.getOrNull()
}

fun formatScheduleClock(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching {
        Instant.parse(iso).atZone(ZoneId.systemDefault()).format(clockOnly)
    }.getOrDefault("")
}

fun formatScheduleDateTime(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching {
        Instant.parse(iso).atZone(ZoneId.systemDefault()).format(localDisplay)
    }.getOrDefault("")
}

fun scheduleMeta(dueOn: String, remindAt: String?, note: String): String {
    val parts = mutableListOf(dueOn.takeLast(5))
    val remind = formatScheduleClock(remindAt)
    if (remind.isNotBlank()) parts += "提醒 $remind"
    if (note.isNotBlank()) parts += note
    return parts.joinToString(" · ")
}
