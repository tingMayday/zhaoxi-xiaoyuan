package com.zhaoxi.xiaoyuan.data.archive

import retrofit2.http.GET

interface ArchiveApi {
    @GET("api/archive")
    suspend fun board(): ArchiveBoard
}
