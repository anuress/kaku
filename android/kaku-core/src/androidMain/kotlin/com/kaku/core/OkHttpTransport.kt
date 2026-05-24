package com.kaku.core

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

internal actual fun createTransport(): KakuTransport = OkHttpTransport()

private class OkHttpTransport : KakuTransport {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null

    override fun connect(url: String, listener: KakuTransportListener) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) =
                listener.onConnected()

            override fun onMessage(webSocket: WebSocket, text: String) =
                listener.onMessage(text)

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) =
                listener.onDisconnected()

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) =
                listener.onError(t)
        })
    }

    override fun send(message: String) {
        webSocket?.send(message)
    }

    override fun disconnect() {
        webSocket?.close(1000, "client disconnect")
        webSocket = null
    }
}
