package com.kaku.core

interface KakuPlugin {
    val id: String
    fun onRegistered(emitter: KakuEmitter)
    fun onDisconnected() {}
    fun onCommand(command: KakuCommand) {}
}

interface KakuEmitter {
    fun emit(event: KakuEvent)
}
