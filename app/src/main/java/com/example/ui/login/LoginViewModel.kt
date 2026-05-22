package com.example.ui.login

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.NetworkClient
import com.example.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onServerUrlChange(url: String) {
        _uiState.value = _uiState.value.copy(serverUrl = url, error = null)
    }

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun login() {
        val currentState = _uiState.value
        if (currentState.serverUrl.isBlank() || currentState.username.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(error = "All fields are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Formatting server URL
                val baseUrl = if (currentState.serverUrl.endsWith("/")) {
                    currentState.serverUrl.dropLast(1)
                } else {
                    currentState.serverUrl
                }
                val sessionUrl = "$baseUrl/.well-known/jmap"

                val authString = "${currentState.username}:${currentState.password}"
                val base64Auth = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)
                val authHeader = "Basic $base64Auth"

                val sessionResponse = NetworkClient.jmapApi.getSession(sessionUrl, authHeader)

                val accountId = sessionResponse.primaryAccounts["urn:ietf:params:jmap:mail"]
                    ?: sessionResponse.accounts.keys.firstOrNull()
                    ?: throw Exception("No mail account found")

                sessionManager.serverUrl = baseUrl
                sessionManager.authMethod = "Basic"
                sessionManager.saveToken(base64Auth)
                sessionManager.apiUrl = sessionResponse.apiUrl
                sessionManager.accountId = accountId

                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage ?: "Login failed")
            }
        }
    }
}

data class LoginUiState(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
