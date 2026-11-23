package com.zhaoxi.xiaoyuan.data.media

class MediaRepository(private val api: MediaApi) {
    suspend fun list() = api.list()
    suspend fun stats() = api.stats()
    suspend fun create(body: NewMedia) = api.create(body)
    suspend fun remove(id: String) { api.remove(id) }
}
