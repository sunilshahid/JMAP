package com.example.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class JmapRequest(
    val using: List<String>,
    val methodCalls: List<List<JsonElement>>
)

@Serializable
data class JmapResponse(
    val methodResponses: List<List<JsonElement>>,
    val sessionState: String? = null,
    val latestClientVersion: String? = null
)
