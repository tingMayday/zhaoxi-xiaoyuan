package com.zhaoxi.xiaoyuan.data.buy

import kotlinx.serialization.Serializable

@Serializable
data class BuyBoard(
    val items: List<BuyItem> = emptyList(),
    val openCount: Int = 0,
    val estimateCents: Int = 0,
    val boughtThisMonth: Int = 0,
)

@Serializable
data class BuyItem(
    val id: String,
    val title: String,
    val quantity: Double = 1.0,
    val estimateCents: Int = 0,
    val status: String,
    val boughtOn: String? = null,
    val note: String = "",
)

@Serializable
data class NewBuyItem(
    val title: String,
    val quantity: Double = 1.0,
    val estimateCents: Int = 0,
    val note: String = "",
)

@Serializable
data class BuyStatusBody(val status: String)
