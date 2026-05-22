package com.example.ui.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EmailRepository
import com.example.data.MailboxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ComposeViewModel(
    private val emailRepository: EmailRepository,
    private val mailboxRepository: MailboxRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComposeUiState())
    val uiState: StateFlow<ComposeUiState> = _uiState.asStateFlow()

    fun onToChange(to: String) {
        _uiState.value = _uiState.value.copy(to = to)
    }

    fun onSubjectChange(subject: String) {
        _uiState.value = _uiState.value.copy(subject = subject)
    }

    fun onBodyChange(body: String) {
        _uiState.value = _uiState.value.copy(body = body)
    }

    fun sendEmail() {
        val currentState = _uiState.value
        if (currentState.to.isBlank()) {
            _uiState.value = currentState.copy(error = "Recipient is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true, error = null)
            try {
                // Find draft and sent mailboxes
                val mailboxes = mailboxRepository.getMailboxes()
                val draftMailboxId = mailboxes.find { it.role?.lowercase() == "drafts" }?.id
                val sentMailboxId = mailboxes.find { it.role?.lowercase() == "sent" }?.id
                
                if (draftMailboxId == null || sentMailboxId == null) {
                    throw IllegalStateException("Could not locate Drafts or Sent mailboxes")
                }
                
                val identityId = emailRepository.getFirstIdentityId() ?: throw IllegalStateException("Could not find sender identity")
                
                emailRepository.sendEmail(
                    identityId = identityId,
                    toEmail = currentState.to,
                    subject = currentState.subject,
                    body = currentState.body,
                    draftMailboxId = draftMailboxId,
                    sentMailboxId = sentMailboxId
                )
                
                _uiState.value = _uiState.value.copy(isSending = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSending = false, error = e.localizedMessage ?: "Failed to send email")
            }
        }
    }
    
    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ComposeUiState(
    val to: String = "",
    val subject: String = "",
    val body: String = "",
    val isSending: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
