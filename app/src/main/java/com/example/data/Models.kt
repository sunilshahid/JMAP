package com.example.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Mailbox(
    val id: String,
    val name: String,
    val unread: Int = 0,
    val unreadEmails: Int = 0,
    val unreadThreads: Int = 0,
    val totalEmails: Int? = null,
    val totalThreads: Int? = null,
    val sortOrder: Int = 0,
    val icon: String? = null,
    val role: String? = null
)

@Serializable
data class EmailAddress(
    val name: String = "",
    val email: String = ""
)

@Serializable
data class EmailHeader(
    val name: String,
    val value: String
)

@Serializable
data class Email(
    val id: String,
    val mailboxId: String? = null,
    val mailboxIds: Map<String, Boolean>? = null,
    val from: EmailAddress? = null,
    val to: List<EmailAddress> = emptyList(),
    val subject: String = "",
    val preview: String = "",
    val body: String = "",
    val date: String = "",
    val read: Boolean = false,
    val starred: Boolean = false,
    val unsubscribeUrl: String? = null,
    val headers: List<EmailHeader>? = null,
    val keywords: Map<String, Boolean>? = null
)

@Serializable
data class Identity(
    val id: String,
    val name: String,
    val email: String,
    val replyTo: List<EmailAddress>? = null,
    val bcc: List<EmailAddress>? = null,
    val textSignature: String? = null,
    val htmlSignature: String? = null
)

@Serializable
data class Account(
    val serverUrl: String,
    val username: String,
    val password: String? = null,
    val token: String? = null,
    val apiUrl: String,
    val accountId: String,
    val uploadUrl: String? = null,
    val downloadUrl: String? = null,
    val websocketUrl: String? = null,
    val primaryAccounts: Map<String, String>? = null,
    val capabilities: List<String> = emptyList()
)

@Serializable
data class Contact(
    val id: String,
    val fullName: String? = null,
    val notes: String? = null,
    val avatar: String? = null,
    val email: String = ""
)

@Serializable
data class Calendar(
    val id: String,
    val name: String,
    val color: String? = null
)
