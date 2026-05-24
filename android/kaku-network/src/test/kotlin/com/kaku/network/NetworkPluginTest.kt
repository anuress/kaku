package com.kaku.network

import com.kaku.core.KakuEmitter
import com.kaku.core.KakuEvent
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NetworkPluginTest {
    private fun runRequest(block: (OkHttpClient, MockWebServer) -> Unit): List<KakuEvent> {
        val server = MockWebServer()
        server.start()
        val plugin = NetworkPlugin()
        val emitter = mockk<KakuEmitter>(relaxed = true)
        plugin.onRegistered(emitter)
        val client = OkHttpClient.Builder()
            .addInterceptor(plugin.interceptor as okhttp3.Interceptor)
            .build()
        block(client, server)
        val events = mutableListOf<KakuEvent>()
        verify(atLeast = 1) { emitter.emit(capture(events)) }
        server.shutdown()
        return events
    }

    @Test
    fun `emits request and response events`() {
        val events = runRequest { client, server ->
            server.enqueue(MockResponse().setBody("ok"))
            client.newCall(Request.Builder().url(server.url("/test")).build()).execute().close()
        }
        assertEquals("network", events.first { it.type == "request" }.plugin)
        assertEquals("network", events.first { it.type == "response" }.plugin)
    }

    @Test
    fun `captures JSON response body`() {
        val events = runRequest { client, server ->
            server.enqueue(
                MockResponse()
                    .setBody("""{"status":"ok"}""")
                    .addHeader("Content-Type", "application/json")
            )
            client.newCall(Request.Builder().url(server.url("/test")).build()).execute().close()
        }
        val payload = Json.decodeFromJsonElement<NetworkResponsePayload>(
            events.first { it.type == "response" }.payload
        )
        assertEquals("""{"status":"ok"}""", payload.body)
    }

    @Test
    fun `captures JSON request body`() {
        val events = runRequest { client, server ->
            server.enqueue(MockResponse())
            val body = """{"key":"value"}""".toRequestBody("application/json".toMediaType())
            client.newCall(
                Request.Builder().url(server.url("/test")).post(body).build()
            ).execute().close()
        }
        val payload = Json.decodeFromJsonElement<NetworkRequestPayload>(
            events.first { it.type == "request" }.payload
        )
        assertEquals("""{"key":"value"}""", payload.body)
    }

    @Test
    fun `skips body for binary content type`() {
        val events = runRequest { client, server ->
            server.enqueue(
                MockResponse()
                    .setBody("binary")
                    .addHeader("Content-Type", "image/png")
            )
            client.newCall(Request.Builder().url(server.url("/test")).build()).execute().close()
        }
        val payload = Json.decodeFromJsonElement<NetworkResponsePayload>(
            events.first { it.type == "response" }.payload
        )
        assertNull(payload.body)
    }
}
