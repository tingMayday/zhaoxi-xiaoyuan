package com.zhaoxi.xiaoyuan.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.tokenDataStore by preferencesDataStore(name = "zhaoxi_auth")

class TokenStore(private val context: Context) {
    private val key = stringPreferencesKey("jwt")

    val tokenFlow: Flow<String?> = context.tokenDataStore.data.map { prefs ->
        prefs[key]?.takeIf { it.isNotBlank() }
    }

    suspend fun token(): String? = tokenFlow.first()

    fun tokenBlocking(): String? = runBlocking { token() }

    suspend fun save(token: String) {
        context.tokenDataStore.edit { it[key] = token }
    }

    suspend fun clear() {
        context.tokenDataStore.edit { it.remove(key) }
    }
}
