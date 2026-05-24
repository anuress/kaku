package com.kaku.network

import com.kaku.core.KakuEmitter
import com.kaku.core.KakuPlugin

class NetworkPlugin : KakuPlugin {
    override val id = "network"
    private var emitter: KakuEmitter? = null

    val interceptor = NetworkInterceptor { event -> emitter?.emit(event) }

    override fun onRegistered(emitter: KakuEmitter) {
        this.emitter = emitter
    }

    override fun onDisconnected() {
        emitter = null
    }
}
