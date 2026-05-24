package com.kaku.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class KakuEvent(
    val plugin: String,
    val type: String,
    val id: String,
    val timestamp: Long,
    val payload: JsonElement = JsonObject(emptyMap()),
)

@Serializable
internal data class HelloMessage(
    val type: String = "hello",
    val platform: String,
    val sdkVersion: Int,
    val plugins: List<String>,
)
