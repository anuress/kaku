package com.kaku.network

import com.kaku.core.KakuEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

// Captures all headers including Authorization — do not use in production builds
class NetworkInterceptor(private val emit: (KakuEvent) -> Unit) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestId = UUID.randomUUID().toString()

        emit(
            KakuEvent(
                plugin = "network",
                type = "request",
                id = requestId,
                timestamp = System.currentTimeMillis(),
                payload = Json.encodeToJsonElement(
                    NetworkRequestPayload(
                        method = request.method,
                        url = request.url.toString(),
                        headers = request.headers.names().associateWith { request.headers[it].orEmpty() },
                    )
                ),
            )
        )

        val response = chain.proceed(request)

        emit(
            KakuEvent(
                plugin = "network",
                type = "response",
                id = requestId,
                timestamp = System.currentTimeMillis(),
                payload = Json.encodeToJsonElement(
                    NetworkResponsePayload(
                        statusCode = response.code,
                        headers = response.headers.names().associateWith { response.headers[it].orEmpty() },
                    )
                ),
            )
        )

        return response
    }
}
