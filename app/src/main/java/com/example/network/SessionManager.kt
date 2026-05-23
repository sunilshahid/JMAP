package com.example.network

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("jmap_prefs", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(prefs.getString("token", null) != null || prefs.getBoolean("isDemoMode", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun saveToken(token: String) {
        prefs.edit().putString("token", token).putBoolean("isDemoMode", false).apply()
        _isLoggedIn.value = true
    }

    fun setDemoMode(isDemo: Boolean) {
        prefs.edit().putBoolean("isDemoMode", isDemo).apply()
        _isLoggedIn.value = isDemo
    }
    
    val isDemoMode: Boolean
        get() = prefs.getBoolean("isDemoMode", false)

    var hasSeenWelcomeTour: Boolean
        get() = prefs.getBoolean("hasSeenWelcomeTour", false)
        set(value) = prefs.edit().putBoolean("hasSeenWelcomeTour", value).apply()
        
    var hasSetupSecureMail: Boolean
        get() = prefs.getBoolean("hasSetupSecureMail", false)
        set(value) = prefs.edit().putBoolean("hasSetupSecureMail", value).apply()

    var hasSelectedLanguage: Boolean
        get() = prefs.getBoolean("hasSelectedLanguage", false)
        set(value) = prefs.edit().putBoolean("hasSelectedLanguage", value).apply()

    var languageCode: String
        get() = prefs.getString("languageCode", "en") ?: "en"
        set(value) = prefs.edit().putString("languageCode", value).apply()

    private val _themeMode = MutableStateFlow(prefs.getInt("themeMode", 0)) // 0: System, 1: Light, 2: Dark
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt("themeMode", mode).apply()
        _themeMode.value = mode
    }

    var secureMailApiKey: String?
        get() = prefs.getString("secureMailApiKey", null)
        set(value) = prefs.edit().putString("secureMailApiKey", value).apply()

    var secureMailApiUrl: String?
        get() = prefs.getString("secureMailApiUrl", null)
        set(value) = prefs.edit().putString("secureMailApiUrl", value).apply()

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
