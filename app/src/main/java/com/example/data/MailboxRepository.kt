package com.example.data

import com.example.network.JmapClient
import com.example.network.SessionManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

class MailboxRepository(
    private val jmapClient: JmapClient,
    private val sessionManager: SessionManager,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    suspend fun getMailboxes(): List<Mailbox> {
        val accountId = sessionManager.accountId ?: throw IllegalStateException("Account ID missing")

        val methodCall = buildJsonArray {
            add(json.parseToJsonElement("\"Mailbox/get\""))
            add(buildJsonObject {
                put("accountId", accountId)
            })
            add(json.parseToJsonElement("\"0\""))
        }

        val responses = jmapClient.call(listOf(methodCall))
        
        // Find the Mailbox/get response
        val mailboxResponse = responses.find { 
            it.isNotEmpty() && it[0].toString().replace("\"", "") == "Mailbox/get" 
        } ?: return emptyList()

        if (mailboxResponse.size > 1) {
            val responseArgs = mailboxResponse[1].jsonObject
            val list = responseArgs["list"]?.jsonArray
            if (list != null) {
                return json.decodeFromJsonElement(list)
            }
        }
        
        return emptyList()
    }
}
