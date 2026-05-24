package com.kaku.core

internal interface KakuTransport {
    fun connect(url: String, listener: KakuTransportListener)
    fun send(message: String)
    fun disconnect()
}

internal interface KakuTransportListener {
    fun onConnected()
    fun onMessage(message: String)
    fun onDisconnected()
    fun onError(error: Throwable)
}

internal expect fun createTransport(): KakuTransport
