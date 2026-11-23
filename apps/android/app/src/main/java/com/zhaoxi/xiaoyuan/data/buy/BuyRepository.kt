package com.zhaoxi.xiaoyuan.data.buy

class BuyRepository(private val api: BuyApi) {
    suspend fun board() = api.board()
    suspend fun create(body: NewBuyItem) = api.create(body)
    suspend fun markBought(id: String) { api.update(id, BuyStatusBody("bought")) }
    suspend fun remove(id: String) { api.remove(id) }
}
