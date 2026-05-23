package com.example.ui.compose

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EmailRepository
import com.example.data.MailboxRepository
import com.example.network.SessionManager
import com.example.network.SecureMailHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class ComposeViewModel(
    private val emailRepository: EmailRepository,
    private val mailboxRepository: MailboxRepository,
    private val sessionManager: SessionManager
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

    fun onSecureMailToggle(isSecure: Boolean) {
        _uiState.value = _uiState.value.copy(isSecureMail = isSecure)
    }
    
    fun onExpirationChange(expiration: String) {
        _uiState.value = _uiState.value.copy(expiration = expiration)
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
                val mailboxes = mailboxRepository.getMailboxes()
                val draftMailboxId = mailboxes.find { it.role?.lowercase() == "drafts" }?.id
                val sentMailboxId = mailboxes.find { it.role?.lowercase() == "sent" }?.id
                
                if (draftMailboxId == null || sentMailboxId == null) {
                    throw IllegalStateException("Could not locate Drafts or Sent mailboxes")
                }
                
                var finalSubject = currentState.subject
                var finalBody = currentState.body

                if (currentState.isSecureMail) {
                    val encryptedPayload = Base64.encodeToString(currentState.body.toByteArray(), Base64.DEFAULT)
                    val iv = "random_iv"
                    val salt = "random_salt"
                    
                    val secureUrl = suspendCancellableCoroutine<String> { continuation ->
                        SecureMailHelper(sessionManager).generateSecureMailLink(
                            encryptedPayload = encryptedPayload,
                            iv = iv,
                            salt = salt,
                            onSuccess = { url -> continuation.resume(url) },
                            onError = { e -> continuation.resumeWithException(e) }
                        )
                    }
                    finalSubject = "Secure Message"
                    finalBody = "You have received a secure message. It will expire in ${currentState.expiration}.\nView it here: $secureUrl"
                }

                if (!sessionManager.isDemoMode) {
                    val identityId = emailRepository.getFirstIdentityId() ?: throw IllegalStateException("Could not find sender identity")
                    
                    emailRepository.sendEmail(
                        identityId = identityId,
                        toEmail = currentState.to,
                        subject = finalSubject,
                        body = finalBody,
                        draftMailboxId = draftMailboxId,
                        sentMailboxId = sentMailboxId
                    )
                }
                
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
    val isSecureMail: Boolean = false,
    val expiration: String = "1week",
    val isSending: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
