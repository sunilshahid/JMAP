package com.example.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.Email
import com.example.data.Mailbox
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onLogout: () -> Unit,
    onEmailClick: (String) -> Unit,
    onComposeClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Email, contentDescription = "Mail") },
                    label = { Text("Mail") },
                    selected = uiState.activeApp == AppType.MAIL,
                    onClick = {
                        viewModel.setAppType(AppType.MAIL)
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Contacts, contentDescription = "Contacts") },
                    label = { Text("Contacts") },
                    selected = uiState.activeApp == AppType.CONTACTS,
                    onClick = {
                        viewModel.setAppType(AppType.CONTACTS)
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = "Calendar") },
                    label = { Text("Calendar") },
                    selected = uiState.activeApp == AppType.CALENDAR,
                    onClick = {
                        viewModel.setAppType(AppType.CALENDAR)
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                HorizontalDivider()
                Text(
                    "Mailboxes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.mailboxes) { mailbox ->
                        NavigationDrawerItem(
                            icon = { Icon(getIconForRole(mailbox.role), contentDescription = mailbox.name) },
                            label = { Text(mailbox.name) },
                            selected = uiState.activeApp == AppType.MAIL && mailbox.id == uiState.selectedMailboxId,
                            onClick = {
                                viewModel.setAppType(AppType.MAIL)
                                viewModel.selectMailbox(mailbox.id)
                                scope.launch { drawerState.close() }
                            },
                            badge = { 
                                val unread = mailbox.unreadEmails.takeIf { it > 0 } ?: mailbox.unread.takeIf { it > 0 } ?: mailbox.unreadThreads
                                if (unread > 0) {
                                    Badge { Text(unread.toString()) }
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
                HorizontalDivider()
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = uiState.activeApp == AppType.SETTINGS,
                    onClick = {
                        viewModel.setAppType(AppType.SETTINGS)
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout") },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        val selectedMailbox = uiState.mailboxes.find { it.id == uiState.selectedMailboxId }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val title = when (uiState.activeApp) {
                            AppType.MAIL -> selectedMailbox?.name ?: "Mail"
                            AppType.CONTACTS -> "Contacts"
                            AppType.CALENDAR -> "Calendar"
                            AppType.SETTINGS -> "Settings"
                        }
                        Text(title)
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            when(uiState.activeApp) {
                                AppType.MAIL -> viewModel.refreshEmails()
                                else -> {} 
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
            },
            floatingActionButton = {
                if (uiState.activeApp == AppType.MAIL) {
                    FloatingActionButton(onClick = { onComposeClick() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Compose")
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.error != null) {
                    Text(
                        uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                } else {
                    when (uiState.activeApp) {
                        AppType.MAIL -> {
                            EmailList(
                                emails = uiState.emails,
                                isLoading = uiState.isEmailsLoading,
                                error = uiState.emailsError,
                                onEmailClick = { email -> onEmailClick(email.id) }
                            )
                        }
                        AppType.CONTACTS -> {
                            ContactList(contacts = uiState.contacts, isLoading = uiState.isContactsLoading)
                        }
                        AppType.CALENDAR -> {
                            CalendarEventList(events = uiState.events, isLoading = uiState.isEventsLoading)
                        }
                        AppType.SETTINGS -> {
                            com.example.ui.settings.SettingsScreen(sessionManager = viewModel.sessionManager)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmailList(
    emails: List<Email>,
    isLoading: Boolean,
    error: String?,
    onEmailClick: (Email) -> Unit
) {
    if (isLoading && emails.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (error != null && emails.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        }
    } else if (emails.isEmpty()) {
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No emails found.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(emails) { email ->
                EmailItemCard(email = email, onClick = { onEmailClick(email) })
            }
        }
    }
}

@Composable
fun EmailItemCard(email: Email, onClick: () -> Unit) {
    val isUnread = !email.read
    val fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if(isUnread) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape, modifier = Modifier.fillMaxSize()) {}
                Text(
                    text = email.from?.name?.take(1)?.uppercase() ?: email.from?.email?.take(1)?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = email.from?.name?.takeIf { it.isNotBlank() } ?: email.from?.email ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = fontWeight),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDate(email.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = email.subject.takeIf { it.isNotBlank() } ?: "(No Subject)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = fontWeight),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = email.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        // Simple parsed ISO string
        val instant = Instant.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("MMM dd")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
         dateString.take(10) // fallback short string
    }
}

fun getIconForRole(role: String?): androidx.compose.ui.graphics.vector.ImageVector {
    return when (role?.lowercase()) {
        "inbox" -> Icons.Default.Inbox
        "drafts" -> Icons.Default.Drafts
        "sent" -> Icons.AutoMirrored.Filled.Send
        "archive" -> Icons.Default.Archive
        "trash" -> Icons.Default.Delete
        "junk", "spam" -> Icons.Default.Report
        else -> Icons.Default.Folder
    }
}

@Composable
fun ContactList(contacts: List<com.example.data.Contact>, isLoading: Boolean) {
    if (isLoading && contacts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (contacts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No contacts found.") }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(contacts) { contact ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape, modifier = Modifier.fillMaxSize()) {}
                            Text(
                                text = (contact.fullName ?: "").take(1).uppercase(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = contact.fullName ?: "Unknown", style = MaterialTheme.typography.titleMedium)
                            if (contact.email.isNotBlank()) {
                                Text(text = contact.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarEventList(events: List<com.example.data.CalendarEvent>, isLoading: Boolean) {
    if (isLoading && events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No events found.") }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(events) { event ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = event.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, contentDescription = "Date", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = formatDate(event.date), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

