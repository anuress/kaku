package com.kaku.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { encodeDefaults = true }

class KakuClient internal constructor(
    private val transport: KakuTransport = createTransport(),
) {
    private val plugins = mutableListOf<KakuPlugin>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var serverUrl = "ws://localhost:8765"
    private var reconnectAttempts = 0
    private var emittersInitialized = false

    fun register(plugin: KakuPlugin) {
        plugins.add(plugin)
    }

    internal fun startForTest(url: String) = start(url)

    internal fun start(url: String) {
        serverUrl = url
        reconnectAttempts = 0
        connect()
    }

    private fun connect() {
        transport.connect(serverUrl, object : KakuTransportListener {
            override fun onConnected() {
                reconnectAttempts = 0
                if (!emittersInitialized) {
                    setupEmitters()
                    emittersInitialized = true
                }
                sendHello()
            }
            override fun onMessage(message: String) {}
            override fun onDisconnected() {
                plugins.forEach { it.onDisconnected() }
                scheduleReconnect()
            }
            override fun onError(error: Throwable) { scheduleReconnect() }
        })
    }

    private fun setupEmitters() {
        plugins.forEach { plugin ->
            plugin.onRegistered(object : KakuEmitter {
                override fun emit(event: KakuEvent) {
                    transport.send(json.encodeToString(event))
                }
            })
        }
    }

    private fun sendHello() {
        val hello = HelloMessage(
            platform = "android",
            sdkVersion = 1,
            plugins = plugins.map { it.id },
        )
        transport.send(json.encodeToString(hello))
    }

    private fun scheduleReconnect() {
        val delayMs = minOf(1000L * (1L shl reconnectAttempts), 30_000L)
        reconnectAttempts++
        scope.launch {
            delay(delayMs)
            connect()
        }
    }
}
