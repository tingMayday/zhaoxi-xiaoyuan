package com.zhaoxi.xiaoyuan.data.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Serializable
data class Profile(
    val id: String,
    val email: String,
    val name: String,
    val timezone: String,
    val heightCm: Double? = null,
)

@Serializable
data class AuthSession(
    val token: String,
    val user: Profile,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val timezone: String? = null,
    val heightCm: Double? = null,
)

@Serializable
data class ApiErrorBody(
    val message: JsonElement? = null,
    val statusCode: Int? = null,
)

fun ApiErrorBody.messageText(): String? = when (val msg = message) {
    null -> null
    is JsonPrimitive -> msg.contentOrNull?.takeIf { it.isNotBlank() }
    is JsonArray -> msg.mapNotNull {
        (it as? JsonPrimitive)?.contentOrNull?.takeIf { text -> text.isNotBlank() }
    }.joinToString("；").ifBlank { null }
    else -> msg.toString().ifBlank { null }
}
