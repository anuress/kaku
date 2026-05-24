package com.kaku.network

import kotlinx.serialization.Serializable

@Serializable
data class NetworkRequestPayload(
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val body: String? = null,
)

@Serializable
data class NetworkResponsePayload(
    val statusCode: Int,
    val headers: Map<String, String>,
    val body: String? = null,
)
