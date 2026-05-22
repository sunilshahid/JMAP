package com.example.ui.email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Email
import com.example.data.EmailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmailDetailViewModel(
    private val emailId: String,
    private val emailRepository: EmailRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailDetailUiState(isLoading = true))
    val uiState: StateFlow<EmailDetailUiState> = _uiState.asStateFlow()

    init {
        loadEmail()
    }

    private fun loadEmail() {
        viewModelScope.launch {
            try {
                val email = emailRepository.getEmail(emailId)
                if (email != null) {
                    _uiState.value = EmailDetailUiState(email = email, isLoading = false)
                } else {
                    _uiState.value = EmailDetailUiState(error = "Email not found", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = EmailDetailUiState(error = e.localizedMessage ?: "Failed to load email", isLoading = false)
            }
        }
    }
}

data class EmailDetailUiState(
    val email: Email? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
