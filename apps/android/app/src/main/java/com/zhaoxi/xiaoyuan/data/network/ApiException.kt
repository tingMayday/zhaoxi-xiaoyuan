package com.zhaoxi.xiaoyuan.data.network

import com.zhaoxi.xiaoyuan.data.auth.ApiErrorBody
import com.zhaoxi.xiaoyuan.data.auth.messageText
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

class ApiException(message: String, val code: Int? = null) : Exception(message)

private val errorJson = Json { ignoreUnknownKeys = true; isLenient = true }

fun Throwable.toUserMessage(): String = when (this) {
    is ApiException -> message ?: "请求失败"
    is HttpException -> {
        val raw = response()?.errorBody()?.string().orEmpty()
        val parsed = runCatching { errorJson.decodeFromString<ApiErrorBody>(raw) }.getOrNull()
        val msg = parsed?.messageText()
        when {
            !msg.isNullOrBlank() -> msg
            code() == 401 -> "请重新登录"
            else -> "请求失败（${code()}）"
        }
    }
    is IOException -> "网络不通，请稍后再试"
    else -> message ?: "出了点问题"
}

fun HttpException.asApiException(): ApiException {
    val raw = response()?.errorBody()?.string().orEmpty()
    val parsed = runCatching { errorJson.decodeFromString<ApiErrorBody>(raw) }.getOrNull()
    val msg = parsed?.messageText()
        ?: if (code() == 401) "请重新登录" else "请求失败（${code()}）"
    return ApiException(msg, code())
}
