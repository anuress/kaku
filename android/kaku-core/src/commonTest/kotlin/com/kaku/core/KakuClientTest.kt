package com.kaku.core

import kotlin.test.Test
import kotlin.test.assertEquals
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

    @Test
    fun `stores deviceId when hello_ack received`() {
        val transport = FakeTransport()
        val client = KakuClient(transport)
        client.startForTest("ws://localhost:8765")
        transport.simulateConnected()

        transport.simulateMessage("""{"type":"hello_ack","deviceId":"device-abc"}""")

        assertEquals("device-abc", client.deviceId)
    }

    @Test
    fun `dispatches command to matching plugin`() {
        val transport = FakeTransport()
        val client = KakuClient(transport)
        val plugin = FakePlugin("network")
        client.register(plugin)
        client.startForTest("ws://localhost:8765")
        transport.simulateConnected()

        transport.simulateMessage(
            """{"plugin":"network","type":"clear","id":"cmd-1","timestamp":1000,"deviceId":"dev-1"}"""
        )

        assertEquals(1, plugin.receivedCommands.size)
        assertEquals("clear", plugin.receivedCommands[0].type)
    }

    @Test
    fun `ignores commands for unknown plugin`() {
        val transport = FakeTransport()
        val client = KakuClient(transport)
        val plugin = FakePlugin("network")
        client.register(plugin)
        client.startForTest("ws://localhost:8765")
        transport.simulateConnected()

        transport.simulateMessage(
            """{"plugin":"unknown","type":"clear","id":"cmd-1","timestamp":1000,"deviceId":"dev-1"}"""
        )

        assertEquals(0, plugin.receivedCommands.size)
    }

    @Test
    fun `reconnects immediately when server sends reconnect command`() {
        val transport = FakeTransport()
        val client = KakuClient(transport)
        client.startForTest("ws://localhost:8765")
        transport.simulateConnected()
        transport.simulateMessage("""{"type":"hello_ack","deviceId":"device-abc"}""")
        val connectsBefore = transport.connectCount

        transport.simulateMessage("""{"type":"reconnect"}""")

        assertEquals(1, transport.disconnectCount)
        assertEquals(connectsBefore + 1, transport.connectCount)
    }
}

internal class FakeTransport : KakuTransport {
    val sentMessages = mutableListOf<String>()
    var connectCount = 0
    var disconnectCount = 0
    private var listener: KakuTransportListener? = null

    override fun connect(url: String, listener: KakuTransportListener) {
        connectCount++
        this.listener = listener
    }
    override fun send(message: String) { sentMessages.add(message) }
    override fun disconnect() { disconnectCount++ }

    fun simulateConnected() = listener?.onConnected()
    fun simulateDisconnected() = listener?.onDisconnected()
    fun simulateError(t: Throwable = RuntimeException("network error")) = listener?.onError(t)
    fun simulateMessage(message: String) = listener?.onMessage(message)
}

internal class FakePlugin(override val id: String) : KakuPlugin {
    var emitterSet = false
    var disconnectedCalled = false
    val receivedCommands = mutableListOf<KakuCommand>()
    override fun onRegistered(emitter: KakuEmitter) { emitterSet = true }
    override fun onDisconnected() { disconnectedCalled = true }
    override fun onCommand(command: KakuCommand) { receivedCommands.add(command) }
}
