package com.example.network

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("jmap_prefs", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(prefs.getString("token", null) != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun saveToken(token: String) {
        prefs.edit().putString("token", token).apply()
        _isLoggedIn.value = true
    }

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
    }

    var authMethod: String?
        get() = prefs.getString("authMethod", "Basic")
        set(value) = prefs.edit().putString("authMethod", value).apply()

    fun getAuthHeader(): String {
        return "${authMethod ?: "Basic"} ${getToken() ?: ""}"
    }

    var apiUrl: String?
        get() = prefs.getString("apiUrl", null)
        set(value) = prefs.edit().putString("apiUrl", value).apply()

    var accountId: String?
        get() = prefs.getString("accountId", null)
        set(value) = prefs.edit().putString("accountId", value).apply()
        
    var serverUrl: String?
        get() = prefs.getString("serverUrl", null)
        set(value) = prefs.edit().putString("serverUrl", value).apply()
}
