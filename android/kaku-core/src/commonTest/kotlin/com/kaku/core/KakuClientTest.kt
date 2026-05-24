package com.kaku.core

import kotlin.test.Test
import kotlin.test.assertTrue

class KakuClientTest {
    @Test
    fun `registers plugins`() {
        val transport = FakeTransport()
        val client = KakuClient(transport)
        val plugin = FakePlugin("network")

        client.register(plugin)
        client.startForTest("ws://localhost:8765")

        transport.simulateConnected()
        assertTrue(plugin.emitterSet)
    }

    @Test
    fun `notifies plugins on clean disconnect`() {
        val transport = FakeTransport()
        val client = KakuClient(transport)
        val plugin = FakePlugin("network")
        client.register(plugin)
        client.startForTest("ws://localhost:8765")
        transport.simulateConnected()

        transport.simulateDisconnected()

        assertTrue(plugin.disconnectedCalled)
    }

    @Test
    fun `notifies plugins on connection error`() {
        val transport = FakeTransport()
        val client = KakuClient(transport)
        val plugin = FakePlugin("network")
        client.register(plugin)
        client.startForTest("ws://localhost:8765")
        transport.simulateConnected()

        transport.simulateError()

        assertTrue(plugin.disconnectedCalled)
    }

    @Test
    fun `sends hello with plugin ids on connect`() {
        val transport = FakeTransport()
        val client = KakuClient(transport)
        client.register(FakePlugin("network"))
        client.register(FakePlugin("loadtime"))

        client.startForTest("ws://localhost:8765")
        transport.simulateConnected()

        val sent = transport.sentMessages.first()
        assertTrue(sent.contains("\"type\":\"hello\""))
        assertTrue(sent.contains("network"))
        assertTrue(sent.contains("loadtime"))
    }
}

// Test doubles
internal class FakeTransport : KakuTransport {
    val sentMessages = mutableListOf<String>()
    private var listener: KakuTransportListener? = null

    override fun connect(url: String, listener: KakuTransportListener) {
        this.listener = listener
    }
    override fun send(message: String) { sentMessages.add(message) }
    override fun disconnect() {}

    fun simulateConnected() = listener?.onConnected()
    fun simulateDisconnected() = listener?.onDisconnected()
    fun simulateError(t: Throwable = RuntimeException("network error")) = listener?.onError(t)
}

internal class FakePlugin(override val id: String) : KakuPlugin {
    var emitterSet = false
    var disconnectedCalled = false
    override fun onRegistered(emitter: KakuEmitter) { emitterSet = true }
    override fun onDisconnected() { disconnectedCalled = true }
}
