package com.example.data

import com.example.network.JmapClient
import com.example.network.SessionManager
import kotlinx.serialization.json.*

class EmailRepository(
    private val jmapClient: JmapClient,
    private val sessionManager: SessionManager,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    suspend fun getEmails(mailboxId: String): List<Email> {
        val accountId = sessionManager.accountId ?: throw IllegalStateException("Account ID missing")
        
        val queryMethod = buildJsonArray {
            add(json.parseToJsonElement("\"Email/query\""))
            add(buildJsonObject {
                put("accountId", accountId)
                put("filter", buildJsonObject { put("inMailbox", mailboxId) })
                put("sort", buildJsonArray {
                    add(buildJsonObject {
                        put("property", "receivedAt")
                        put("isAscending", false)
                    })
                })
                put("limit", 50)
            })
            add(json.parseToJsonElement("\"query1\""))
        }

        val getMethod = buildJsonArray {
            add(json.parseToJsonElement("\"Email/get\""))
            add(buildJsonObject {
                put("accountId", accountId)
                put("#ids", buildJsonObject {
                    put("resultOf", "query1")
                    put("name", "Email/query")
                    put("path", "/ids")
                })
                put("properties", buildJsonArray {
                    add(JsonPrimitive("id"))
                    add(JsonPrimitive("mailboxIds"))
                    add(JsonPrimitive("from"))
                    add(JsonPrimitive("to"))
                    add(JsonPrimitive("subject"))
                    add(JsonPrimitive("preview"))
                    add(JsonPrimitive("receivedAt"))
                    add(JsonPrimitive("keywords"))
                })
            })
            add(json.parseToJsonElement("\"get1\""))
        }

        val responses = jmapClient.call(listOf(queryMethod, getMethod))
        
        val getResponse = responses.find { 
            it.isNotEmpty() && it[0].toString().replace("\"", "") == "Email/get" 
        } ?: return emptyList()
        
        if (getResponse.size > 1) {
            val responseArgs = getResponse[1].jsonObject
            val list = responseArgs["list"]?.jsonArray
            if (list != null) {
                return list.mapNotNull { element ->
                    try {
                        val obj = element.jsonObject
                        val id = obj["id"]?.jsonPrimitive?.content ?: ""
                        
                        val fromObj = obj["from"]?.jsonArray?.firstOrNull()?.jsonObject
                        val fromAddress = fromObj?.let {
                            EmailAddress(
                                name = it["name"]?.jsonPrimitive?.content ?: "",
                                email = it["email"]?.jsonPrimitive?.content ?: ""
                            )
                        }
                        
                        val subject = obj["subject"]?.jsonPrimitive?.content ?: ""
                        val preview = obj["preview"]?.jsonPrimitive?.content ?: ""
                        val receivedAt = obj["receivedAt"]?.jsonPrimitive?.content ?: ""
                        
                        val keywords = obj["keywords"]?.jsonObject?.mapValues { 
                            it.value.jsonPrimitive.booleanOrNull ?: false 
                        }
                        
                        val read = keywords?.get("\$seen") == true

                        Email(
                            id = id,
                            from = fromAddress,
                            subject = subject,
                            preview = preview,
                            date = receivedAt,
                            read = read,
                            keywords = keywords
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
        
        return emptyList()
    }

    suspend fun getEmail(emailId: String): Email? {
        val accountId = sessionManager.accountId ?: return null

        val getMethod = buildJsonArray {
            add(json.parseToJsonElement("\"Email/get\""))
            add(buildJsonObject {
                put("accountId", accountId)
                put("ids", buildJsonArray { add(JsonPrimitive(emailId)) })
                put("properties", buildJsonArray {
                    add(JsonPrimitive("id"))
                    add(JsonPrimitive("from"))
                    add(JsonPrimitive("to"))
                    add(JsonPrimitive("subject"))
                    add(JsonPrimitive("receivedAt"))
                    add(JsonPrimitive("htmlBody"))
                    add(JsonPrimitive("textBody"))
                    add(JsonPrimitive("bodyValues"))
                })
            })
            add(json.parseToJsonElement("\"get1\""))
        }

        val responses = jmapClient.call(listOf(getMethod))
        
        val getResponse = responses.find { 
            it.isNotEmpty() && it[0].toString().replace("\"", "") == "Email/get" 
        } ?: return null
        
        if (getResponse.size > 1) {
            val responseArgs = getResponse[1].jsonObject
            val list = responseArgs["list"]?.jsonArray
            if (list != null && list.isNotEmpty()) {
                try {
                    val obj = list[0].jsonObject
                    val id = obj["id"]?.jsonPrimitive?.content ?: ""
                    
                    val fromObj = obj["from"]?.jsonArray?.firstOrNull()?.jsonObject
                    val fromAddress = fromObj?.let {
                        EmailAddress(
                            name = it["name"]?.jsonPrimitive?.content ?: "",
                            email = it["email"]?.jsonPrimitive?.content ?: ""
                        )
                    }
                    
                    val toList = obj["to"]?.jsonArray?.mapNotNull {
                        val toObj = it.jsonObject
                        EmailAddress(
                            name = toObj["name"]?.jsonPrimitive?.content ?: "",
                            email = toObj["email"]?.jsonPrimitive?.content ?: ""
                        )
                    } ?: emptyList()
                    
                    val subject = obj["subject"]?.jsonPrimitive?.content ?: "(No Subject)"
                    val receivedAt = obj["receivedAt"]?.jsonPrimitive?.content ?: ""
                    
                    var bodyText = ""
                    val htmlBodyArr = obj["htmlBody"]?.jsonArray
                    val textBodyArr = obj["textBody"]?.jsonArray
                    val bodyValues = obj["bodyValues"]?.jsonObject
                    
                    if (htmlBodyArr != null && htmlBodyArr.isNotEmpty() && bodyValues != null) {
                        val partId = htmlBodyArr[0].jsonObject["partId"]?.jsonPrimitive?.content
                        if (partId != null) {
                            bodyText = bodyValues[partId]?.jsonObject?.get("value")?.jsonPrimitive?.content ?: ""
                        }
                    } else if (textBodyArr != null && textBodyArr.isNotEmpty() && bodyValues != null) {
                        val partId = textBodyArr[0].jsonObject["partId"]?.jsonPrimitive?.content
                        if (partId != null) {
                            bodyText = bodyValues[partId]?.jsonObject?.get("value")?.jsonPrimitive?.content ?: ""
                            bodyText = "<pre style=\"font-family: sans-serif; white-space: pre-wrap; word-wrap: break-word;\">$bodyText</pre>"
                        }
                    }

                    return Email(
                        id = id,
                        from = fromAddress,
                        to = toList,
                        subject = subject,
                        date = receivedAt,
                        body = bodyText
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    suspend fun sendEmail(identityId: String, toEmail: String, subject: String, body: String, draftMailboxId: String, sentMailboxId: String) {
        val accountId = sessionManager.accountId ?: throw IllegalStateException("Account ID missing")
        val draftId = "draft-1"

        val emailSetMethod = buildJsonArray {
            add(json.parseToJsonElement("\"Email/set\""))
            add(buildJsonObject {
                put("accountId", accountId)
                put("create", buildJsonObject {
                    put(draftId, buildJsonObject {
                        put("mailboxIds", buildJsonObject {
                            put(draftMailboxId, true)
                        })
                        put("to", buildJsonArray {
                            add(buildJsonObject { put("email", toEmail) })
                        })
                        put("subject", subject)
                        put("textBody", buildJsonArray {
                            add(buildJsonObject { put("partId", "body1") })
                        })
                        put("bodyValues", buildJsonObject {
                            put("body1", buildJsonObject {
                                put("value", body)
                                put("isEncodingProblem", false)
                                put("isTruncated", false)
                            })
                        })
                    })
                })
            })
            add(json.parseToJsonElement("\"set1\""))
        }

        val emailSubmissionMethod = buildJsonArray {
            add(json.parseToJsonElement("\"EmailSubmission/set\""))
            add(buildJsonObject {
                put("accountId", accountId)
                put("create", buildJsonObject {
                    put("sub1", buildJsonObject {
                        put("emailId", "#$draftId")
                        put("identityId", identityId)
                    })
                })
                put("onSuccessUpdateEmail", buildJsonObject {
                    put("#$draftId", buildJsonObject {
                        put("mailboxIds", buildJsonObject {
                            put(sentMailboxId, true)
                        })
                    })
                })
            })
            add(json.parseToJsonElement("\"sub1\""))
        }

        jmapClient.call(listOf(emailSetMethod, emailSubmissionMethod))
    }
    
    suspend fun getFirstIdentityId(): String? {
        val accountId = sessionManager.accountId ?: return null
        val method = buildJsonArray {
            add(json.parseToJsonElement("\"Identity/get\""))
            add(buildJsonObject {
                put("accountId", accountId)
            })
            add(json.parseToJsonElement("\"id1\""))
        }
        val response = jmapClient.call(listOf(method))
        val getRes = response.find { it.isNotEmpty() && it[0].toString().replace("\"", "") == "Identity/get" }
        return getRes?.get(1)?.jsonObject?.get("list")?.jsonArray?.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.content
    }
}
