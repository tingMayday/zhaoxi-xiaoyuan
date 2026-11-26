package com.zhaoxi.xiaoyuan.data.media

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface MediaApi {
    @GET("api/media")
    suspend fun list(): List<MediaItem>

    @GET("api/media/stats")
    suspend fun stats(): MediaStats

    @POST("api/media")
    suspend fun create(@Body body: NewMedia): MediaItem

    @Multipart
    @POST("api/media/cover")
    suspend fun uploadCover(@Part file: MultipartBody.Part): CoverUploadResult

    @DELETE("api/media/{id}")
    suspend fun remove(@Path("id") id: String): ResponseBody
}
