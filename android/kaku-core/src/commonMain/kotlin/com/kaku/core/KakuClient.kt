package com.kaku.core

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

class KakuClient internal constructor(
    private val transport: KakuTransport = createTransport(),
) {
    private val plugins = mutableListOf<KakuPlugin>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var serverUrl = "ws://localhost:8765"
    private var reconnectAttempts = 0
    private var emittersInitialized = false
    private val listenerGeneration = AtomicInteger(0)
    internal var deviceId: String? = null

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
        val myGeneration = listenerGeneration.incrementAndGet()
        transport.connect(serverUrl, object : KakuTransportListener {
            override fun onConnected() {
                reconnectAttempts = 0
                deviceId = null
                if (!emittersInitialized) {
                    setupEmitters()
                    emittersInitialized = true
                }
                sendHello()
            }
            override fun onMessage(message: String) {
                if (listenerGeneration.get() != myGeneration) return
                handleMessage(message)
            }
            override fun onDisconnected() {
                if (listenerGeneration.get() != myGeneration) return
                plugins.forEach { it.onDisconnected() }
                scheduleReconnect()
            }
            override fun onError(error: Throwable) {
                if (listenerGeneration.get() != myGeneration) return
                plugins.forEach { it.onDisconnected() }
                scheduleReconnect()
            }
        })
    }

    private fun handleMessage(message: String) {
        val obj = try {
            json.parseToJsonElement(message).jsonObject
        } catch (e: Exception) {
            return
        }
        when (obj["type"]?.jsonPrimitive?.contentOrNull) {
            "hello_ack" -> {
                val ack = try {
                    json.decodeFromJsonElement<HelloAckMessage>(obj)
                } catch (e: Exception) {
                    return
                }
                deviceId = ack.deviceId
            }
            "reconnect" -> {
                reconnectAttempts = 0
                transport.disconnect()
                connect()
            }
            else -> {
                val cmd = try {
                    json.decodeFromJsonElement<KakuCommand>(obj)
                } catch (e: Exception) {
                    return
                }
                plugins.find { it.id == cmd.plugin }?.onCommand(cmd)
            }
        }
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

    fun close() {
        scope.cancel()
        transport.disconnect()
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
