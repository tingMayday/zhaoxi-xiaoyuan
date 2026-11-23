package com.zhaoxi.xiaoyuan.data.ledger

class LedgerRepository(private val api: LedgerApi) {
    suspend fun categories() = api.categories()
    suspend fun summary(month: String) = api.summary(month)
    suspend fun transactions(month: String, direction: String? = null) =
        api.transactions(month = month, direction = direction?.ifBlank { null })
    suspend fun create(body: NewTransaction) = api.create(body)
    suspend fun remove(id: String) { api.remove(id) }
}
