package com.zhaoxi.xiaoyuan.ui.util

import java.util.Locale
import kotlin.math.abs

/** 展示用金额：无千分位，整数不带小数，保证窄格也能尽量单行完整显示。 */
fun yuan(cents: Int): String {
    val value = cents / 100.0
    return if (abs(value % 1.0) < 1e-9) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", value)
    }
}
