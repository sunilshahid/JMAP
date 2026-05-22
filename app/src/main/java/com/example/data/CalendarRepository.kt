package com.example.data

import com.example.network.JmapClient
import com.example.network.SessionManager
import kotlinx.serialization.json.*

data class CalendarEvent(val id: String, val title: String, val date: String, val calendarId: String)

class CalendarRepository(
    private val jmapClient: JmapClient,
    private val sessionManager: SessionManager,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    suspend fun getEvents(): List<CalendarEvent> {
        val accountId = sessionManager.accountId ?: return emptyList()

        val queryMethod = buildJsonArray {
            add(json.parseToJsonElement("\"CalendarEvent/query\""))
            add(buildJsonObject { put("accountId", accountId) })
            add(json.parseToJsonElement("\"0\""))
        }

        val getMethod = buildJsonArray {
            add(json.parseToJsonElement("\"CalendarEvent/get\""))
            add(buildJsonObject {
                put("accountId", accountId)
                put("#ids", buildJsonObject {
                    put("resultOf", "0")
                    put("name", "CalendarEvent/query")
                    put("path", "/ids")
                })
            })
            add(json.parseToJsonElement("\"1\""))
        }

        val responses = jmapClient.call(listOf(queryMethod, getMethod))
        val getRes = responses.find { it.isNotEmpty() && it[0].toString().replace("\"", "") == "CalendarEvent/get" } ?: return emptyList()
        
        if (getRes.size > 1) {
            val list = getRes[1].jsonObject["list"]?.jsonArray
            return list?.mapNotNull { element ->
                try {
                    val obj = element.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.content ?: ""
                    val title = obj["title"]?.jsonPrimitive?.content ?: "(No title)"
                    val start = obj["start"]?.jsonPrimitive?.content ?: ""
                    val calendarIds = obj["calendarIds"]?.jsonObject?.keys?.firstOrNull() ?: obj["calendarId"]?.jsonPrimitive?.content ?: ""
                    CalendarEvent(id, title, start, calendarIds)
                } catch (e: Exception) { null }
            } ?: emptyList()
        }
        return emptyList()
    }
}
