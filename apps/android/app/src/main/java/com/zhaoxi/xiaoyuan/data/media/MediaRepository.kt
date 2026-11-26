package com.zhaoxi.xiaoyuan.data.media

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class MediaRepository(private val api: MediaApi) {
    suspend fun list() = api.list()
    suspend fun stats() = api.stats()
    suspend fun create(body: NewMedia) = api.create(body)
    suspend fun remove(id: String) {
        api.remove(id)
    }

    suspend fun uploadCover(bytes: ByteArray, filename: String, mime: String): CoverUploadResult {
        val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", filename, body)
        return api.uploadCover(part)
    }
}
