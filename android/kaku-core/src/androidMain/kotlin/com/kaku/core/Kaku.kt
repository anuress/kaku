package com.kaku.core

object Kaku {
    @Volatile private var client: KakuClient? = null

    fun init(block: KakuBuilder.() -> Unit) {
        val builder = KakuBuilder()
        builder.block()
        val instance = KakuClient()
        builder.plugins.forEach { instance.register(it) }
        client = instance
        instance.start("ws://localhost:8765")
    }
}

class KakuBuilder {
    internal val plugins = mutableListOf<KakuPlugin>()
    fun register(plugin: KakuPlugin) { plugins.add(plugin) }
}
