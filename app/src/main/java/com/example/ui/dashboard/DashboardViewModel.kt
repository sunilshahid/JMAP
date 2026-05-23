package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Email
import com.example.data.EmailRepository
import com.example.data.Mailbox
import com.example.data.MailboxRepository
import com.example.data.CalendarEvent
import com.example.data.CalendarRepository
import com.example.data.Contact
import com.example.data.ContactRepository
import com.example.network.JmapWebSocketManager
import com.example.network.NotificationHelper
import com.example.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class AppType {
    MAIL, CONTACTS, CALENDAR, SETTINGS
}

class DashboardViewModel(
    private val mailboxRepository: MailboxRepository,
    private val emailRepository: EmailRepository,
    private val contactRepository: ContactRepository,
    private val calendarRepository: CalendarRepository,
    val sessionManager: SessionManager,
    private val webSocketManager: JmapWebSocketManager,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        fetchMailboxes()
        
        webSocketManager.events
            .onEach { 
                refreshEmails()
                notificationHelper.showNotification("New JMAP Event", "Syncing pushed changes from server.")
            }
            .launchIn(viewModelScope)
            
        webSocketManager.connect(sessionManager.serverUrl?.replace("https://", "wss://")?.plus("/.well-known/jmap/ws"))
    }

    private fun fetchMailboxes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                var mailboxes = mailboxRepository.getMailboxes()
                mailboxes = mailboxes.sortedWith(compareBy({ getRolePriority(it.role) }, { it.sortOrder }, { it.name }))
                
                val firstMailboxId = mailboxes.firstOrNull()?.id
                _uiState.value = _uiState.value.copy(
                    mailboxes = mailboxes,
                    isLoading = false,
                    selectedMailboxId = firstMailboxId
                )
                
                if (firstMailboxId != null) {
                    fetchEmails(firstMailboxId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage ?: "Failed to load mailboxes")
            }
        }
    }

    private fun fetchEmails(mailboxId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isEmailsLoading = true, emailsError = null)
            try {
                val emails = emailRepository.getEmails(mailboxId)
                _uiState.value = _uiState.value.copy(emails = emails, isEmailsLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isEmailsLoading = false, emailsError = e.localizedMessage ?: "Failed to fetch emails")
            }
        }
    }

    private fun getRolePriority(role: String?): Int {
        return when (role?.lowercase()) {
            "inbox" -> 0
            "drafts" -> 1
            "sent" -> 2
            "archive" -> 3
            "trash" -> 4
            "junk", "spam" -> 5
            else -> 10
        }
    }

    fun selectMailbox(id: String) {
        if (_uiState.value.selectedMailboxId != id) {
            _uiState.value = _uiState.value.copy(selectedMailboxId = id, emails = emptyList())
            fetchEmails(id)
        }
    }
    
    fun refreshEmails() {
        _uiState.value.selectedMailboxId?.let { fetchEmails(it) }
    }
    
    fun logout() {
        webSocketManager.disconnect()
        sessionManager.clearSession()
    }
    
    fun setAppType(appType: AppType) {
        _uiState.value = _uiState.value.copy(activeApp = appType)
        when (appType) {
            AppType.CONTACTS -> fetchContacts()
            AppType.CALENDAR -> fetchEvents()
            else -> {}
        }
    }

    private fun fetchContacts() {
        if (_uiState.value.contacts.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isContactsLoading = true)
            try {
                val contacts = contactRepository.getContacts()
                _uiState.value = _uiState.value.copy(contacts = contacts, isContactsLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isContactsLoading = false)
            }
        }
    }

    private fun fetchEvents() {
        if (_uiState.value.events.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isEventsLoading = true)
            try {
                val events = calendarRepository.getEvents()
                _uiState.value = _uiState.value.copy(events = events, isEventsLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isEventsLoading = false)
            }
        }
    }
}

data class DashboardUiState(
    val mailboxes: List<Mailbox> = emptyList(),
    val selectedMailboxId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val emails: List<Email> = emptyList(),
    val isEmailsLoading: Boolean = false,
    val emailsError: String? = null,
    val activeApp: AppType = AppType.MAIL,
    val contacts: List<Contact> = emptyList(),
    val isContactsLoading: Boolean = false,
    val events: List<CalendarEvent> = emptyList(),
    val isEventsLoading: Boolean = false
)

