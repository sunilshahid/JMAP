package com.example.network

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class JmapWebSocketManager(
    private val sessionManager: SessionManager,
    private val client: OkHttpClient = OkHttpClient()
) {
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var webSocket: WebSocket? = null

    fun connect(websocketUrl: String?) {
        val url = websocketUrl ?: return
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", sessionManager.getAuthHeader())
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("JmapWebSocketManager", "Received StateChange: $text")
                _events.tryEmit(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("JmapWebSocketManager", "WebSocket failure. This is expected if the server doesn't support WS.", t)
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Disconnecting")
        webSocket = null
    }
}
