package com.kaku.network

import com.kaku.core.KakuEmitter
import com.kaku.core.KakuEvent
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkPluginTest {
    @Test
    fun `interceptor emits request event`() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("ok"))
        server.start()

        val plugin = NetworkPlugin()
        val emitter = mockk<KakuEmitter>(relaxed = true)
        plugin.onRegistered(emitter)

        val client = OkHttpClient.Builder()
            .addInterceptor(plugin.interceptor as okhttp3.Interceptor)
            .build()

        val request = Request.Builder()
            .url(server.url("/test"))
            .build()
        client.newCall(request).execute().close()

        val capturedEvents = mutableListOf<KakuEvent>()
        verify(atLeast = 1) { emitter.emit(capture(capturedEvents)) }

        val requestEvent = capturedEvents.first { it.type == "request" }
        assertEquals("network", requestEvent.plugin)
        assertEquals("request", requestEvent.type)

        server.shutdown()
    }
}
