package com.kaku.core

object Kaku {
    @Volatile private var client: KakuClient? = null

    fun init(block: KakuBuilder.() -> Unit) {
        val builder = KakuBuilder()
        builder.block()
        client?.close()
        val instance = KakuClient()
        builder.plugins.forEach { instance.register(it) }
        client = instance
        instance.start(builder.serverUrl)
    }
}

class KakuBuilder {
    internal val plugins = mutableListOf<KakuPlugin>()
    var serverUrl: String = "ws://localhost:8765"
    fun register(plugin: KakuPlugin) { plugins.add(plugin) }
}
