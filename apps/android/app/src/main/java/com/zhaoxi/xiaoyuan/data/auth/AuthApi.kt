package com.zhaoxi.xiaoyuan.data.auth

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthSession

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthSession

    @GET("api/me")
    suspend fun me(): Profile

    @PATCH("api/me")
    suspend fun updateMe(@Body body: UpdateProfileRequest): Profile
}
