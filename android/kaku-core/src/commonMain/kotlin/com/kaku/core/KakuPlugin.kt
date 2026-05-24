package com.kaku.core

interface KakuPlugin {
    val id: String
    fun onRegistered(emitter: KakuEmitter)
    fun onDisconnected() {}
}

interface KakuEmitter {
    fun emit(event: KakuEvent)
}
