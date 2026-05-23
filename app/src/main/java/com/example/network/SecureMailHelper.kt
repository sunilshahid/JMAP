package com.example.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class SecureMailHelper(private val sessionManager: SessionManager) {

    fun generateSecureMailLink(
        encryptedPayload: String,
        iv: String,
        salt: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val apiKey = sessionManager.secureMailApiKey
        val apiUrl = sessionManager.secureMailApiUrl

        if (apiKey.isNullOrEmpty() || apiUrl.isNullOrEmpty()) {
            onError(IllegalStateException("API Key or URL not configured."))
            return
        }

        val client = OkHttpClient()
        
        val json = JSONObject().apply {
            put("encryptedPayload", encryptedPayload)
            put("iv", iv)
            put("salt", salt)
            put("expiration", "1week")
            put("viewOnce", true)
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                onError(e)
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onError(IOException("API Request Error: ${response.code}"))
                        return
                    }

                    val responseData = JSONObject(response.body!!.string())
                    val secureUrl = responseData.getString("url")
                    
                    onSuccess(secureUrl)
                }
            }
        })
    }
}
