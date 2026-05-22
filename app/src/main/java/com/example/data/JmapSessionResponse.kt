package com.example.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class JmapSessionResponse(
    val capabilities: JsonObject,
    val accounts: Map<String, JmapAccount>,
    val primaryAccounts: Map<String, String>,
    val apiUrl: String,
    val downloadUrl: String,
    val uploadUrl: String,
    val eventSourceUrl: String,
    val state: String
)

@Serializable
data class JmapAccount(
    val name: String,
    val isPersonal: Boolean,
    val isReadOnly: Boolean,
    val accountCapabilities: JsonObject
)
