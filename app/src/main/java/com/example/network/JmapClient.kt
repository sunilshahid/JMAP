package com.example.network

import com.example.data.JmapRequest
import kotlinx.serialization.json.JsonElement

class JmapClient(
    private val sessionManager: SessionManager,
    private val apiService: JmapApiService = NetworkClient.jmapApi
) {
    suspend fun call(methodCalls: List<List<JsonElement>>): List<List<JsonElement>> {
        val apiUrl = sessionManager.apiUrl ?: throw IllegalStateException("API URL not set. Call /session first.")
        
        val request = JmapRequest(
            using = listOf("urn:ietf:params:jmap:core", "urn:ietf:params:jmap:mail"),
            methodCalls = methodCalls
        )
        
        val authHeader = sessionManager.getAuthHeader()
        
        val response = apiService.callApi(apiUrl, authHeader, request)
        return response.methodResponses
    }
}
