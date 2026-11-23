package com.zhaoxi.xiaoyuan.data.auth

class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
) {
    val tokenFlow = tokenStore.tokenFlow

    suspend fun login(email: String, password: String): Profile {
        val session = api.login(LoginRequest(email.trim(), password))
        tokenStore.save(session.token)
        return session.user
    }

    suspend fun register(email: String, password: String, name: String): Profile {
        val session = api.register(
            RegisterRequest(
                email = email.trim(),
                password = password,
                name = name.trim(),
            ),
        )
        tokenStore.save(session.token)
        return session.user
    }

    suspend fun logout() {
        tokenStore.clear()
    }

    suspend fun profile(): Profile = api.me()

    suspend fun updateProfile(body: UpdateProfileRequest): Profile = api.updateMe(body)
}
