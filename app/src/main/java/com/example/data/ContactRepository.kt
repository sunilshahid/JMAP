package com.example.data

import com.example.network.JmapClient
import com.example.network.SessionManager
import kotlinx.serialization.json.*

class ContactRepository(
    private val jmapClient: JmapClient,
    private val sessionManager: SessionManager,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    suspend fun getContacts(): List<Contact> {
        val accountId = sessionManager.accountId ?: return emptyList()

        val queryMethod = buildJsonArray {
            add(json.parseToJsonElement("\"ContactCard/query\""))
            add(buildJsonObject {
                put("accountId", accountId)
                put("limit", 100)
            })
            add(json.parseToJsonElement("\"0\""))
        }

        val getMethod = buildJsonArray {
            add(json.parseToJsonElement("\"ContactCard/get\""))
            add(buildJsonObject {
                put("accountId", accountId)
                put("#ids", buildJsonObject {
                    put("resultOf", "0")
                    put("name", "ContactCard/query")
                    put("path", "/ids")
                })
            })
            add(json.parseToJsonElement("\"1\""))
        }

        val responses = jmapClient.call(listOf(queryMethod, getMethod))
        val getRes = responses.find { it.isNotEmpty() && it[0].toString().replace("\"", "") == "ContactCard/get" } ?: return emptyList()
        
        if (getRes.size > 1) {
            val list = getRes[1].jsonObject["list"]?.jsonArray
            return list?.mapNotNull { element ->
                try {
                    val obj = element.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.content ?: ""
                    
                    var fullName = "Unknown Contact"
                    obj["name"]?.jsonObject?.let { nameObj ->
                         val components = nameObj["components"]?.jsonArray
                         if (components != null && components.isNotEmpty()) {
                             fullName = components.joinToString(" ") { it.jsonObject["value"]?.jsonPrimitive?.content ?: "" }.trim()
                         }
                    }

                    var firstEmail = ""
                    obj["emails"]?.jsonObject?.let { emailsObj ->
                        val firstKey = emailsObj.keys.firstOrNull()
                        if (firstKey != null) {
                            firstEmail = emailsObj[firstKey]?.jsonObject?.get("address")?.jsonPrimitive?.content ?: ""
                        }
                    }
                    
                    Contact(id = id, fullName = fullName.ifBlank { firstEmail }, email = firstEmail)
                } catch (e: Exception) { null }
            } ?: emptyList()
        }
        return emptyList()
    }
}
