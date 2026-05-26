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
data class KakuCommand(
    val plugin: String,
    val type: String,
    val id: String,
    val timestamp: Long,
    val deviceId: String,
    val payload: JsonElement = JsonObject(emptyMap()),
    val replyTo: String? = null,
)

@Serializable
internal data class HelloMessage(
    val type: String = "hello",
    val platform: String,
    val sdkVersion: Int,
    val plugins: List<String>,
)

@Serializable
internal data class HelloAckMessage(
    val type: String,
    val deviceId: String,
)
