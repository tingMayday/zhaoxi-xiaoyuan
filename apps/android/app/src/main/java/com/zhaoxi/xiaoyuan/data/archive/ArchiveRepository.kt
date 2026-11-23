package com.zhaoxi.xiaoyuan.data.archive

class ArchiveRepository(private val api: ArchiveApi) {
    suspend fun board() = api.board()
}