package com.kaku.network

import com.kaku.core.KakuEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.util.UUID

private const val MAX_BODY_BYTES = 256 * 1024L

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
                        body = captureRequestBody(request),
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
                        body = captureResponseBody(response),
                    )
                ),
            )
        )

        return response
    }

    private fun captureRequestBody(request: Request): String? {
        val body = request.body ?: return null
        if (body.isOneShot()) return null
        if (!isTextContent(body.contentType())) return null
        val buffer = Buffer()
        body.writeTo(buffer)
        if (buffer.size > MAX_BODY_BYTES) return "[truncated: ${buffer.size} bytes]"
        return buffer.readUtf8()
    }

    private fun captureResponseBody(response: Response): String? {
        val body = response.body ?: return null
        if (!isTextContent(body.contentType())) return null
        return response.peekBody(MAX_BODY_BYTES).string()
    }

    private fun isTextContent(contentType: MediaType?): Boolean {
        if (contentType == null) return false
        return contentType.type == "text" ||
            contentType.subtype.contains("json") ||
            contentType.subtype.contains("xml") ||
            contentType.subtype == "x-www-form-urlencoded"
    }
}
