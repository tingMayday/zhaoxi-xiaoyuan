package com.zhaoxi.xiaoyuan.ui.util

import java.text.NumberFormat
import java.util.Locale

fun yuan(cents: Int): String {
    val value = cents / 100.0
    return NumberFormat.getNumberInstance(Locale.CHINA).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(value)
}
