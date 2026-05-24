# kaku (核)

A plugin-based debug engine for mobile apps. kaku runs a local WebSocket server that receives structured events from an Android SDK and broadcasts them to any connected UI client — web, TUI, Electron, or anything that speaks WebSocket.

kaku is an engine, not a tool. You build tools on top of it.

---

## How it works

```
Android app                 kaku server              UI client
───────────                 ───────────              ─────────
SDK + plugins  ──ws:8765──► event broker ──ws:8766──► web / TUI / Electron
```

- **`:8765`** — device port. The Android SDK connects here and pushes events.
- **`:8766`** — UI port. Any client connects here and receives the event stream.
- **`adb reverse`** — run automatically on server start so Android devices connect over USB without network config.

Events are typed JSON envelopes with a `plugin` namespace, `type`, correlation `id`, `timestamp`, and a `payload` defined by each plugin.

---

## Getting started

### Server

Requires [Bun](https://bun.sh).

```bash
cd server
bun install
bun run src/index.ts
```

```
[kaku] server running
[kaku]   device  → ws://127.0.0.1:8765
[kaku]   UI      → ws://127.0.0.1:8766
```

### Android SDK

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

```kotlin
// build.gradle.kts
implementation("com.github.anuress.kaku:kaku-core:VERSION")
implementation("com.github.anuress.kaku:kaku-network:VERSION") // optional
```

Initialize in your `Application`:

```kotlin
Kaku.init {
    register(networkPlugin)
    // serverUrl = "ws://10.0.2.2:8765"  // emulator override
}
```

See [kaku-network](android/kaku-network/README.md) for full network plugin setup and event reference.

---

## Plugins

### Built-in: [`kaku-network`](android/kaku-network/README.md)

OkHttp interceptor that captures request/response pairs — method, URL, headers, and body. See its README for installation and event schema.

### Writing your own

A plugin is a named event source. It holds an emitter and pushes `KakuEvent` objects through it.

```kotlin
class MyPlugin : KakuPlugin {
    override val id = "my-plugin"
    private var emitter: KakuEmitter? = null

    val hook = MyHook { data ->
        emitter?.emit(
            KakuEvent(
                plugin = id,
                type = "my-event",
                id = data.correlationId,
                timestamp = System.currentTimeMillis(),
                payload = Json.encodeToJsonElement(MyPayload(data)),
            )
        )
    }

    override fun onRegistered(emitter: KakuEmitter) { this.emitter = emitter }
    override fun onDisconnected() { emitter = null }
}
```

The plugin vends the hook; the app developer wires it manually. For a UI client: connect to `ws://localhost:8766` and filter events by `plugin` field.

---

## Project structure

```
kaku/
├── protocol/          # Shared TypeScript types (KakuEvent, KakuCommand)
├── server/            # Bun WebSocket broker
│   ├── src/
│   │   ├── index.ts   # Server entry point
│   │   ├── router.ts  # Event dispatch to UI clients
│   │   ├── device.ts  # Connected device registry
│   │   └── ui.ts      # UI client registry + broadcast
│   └── tests/
└── android/
    ├── kaku-core/     # KMP SDK — transport, client, plugin contract
    └── kaku-network/  # OkHttp interceptor plugin
```

---

## Tech stack

| Layer | Technology |
|-------|-----------|
| Server | TypeScript + [Bun](https://bun.sh) |
| Android SDK | Kotlin Multiplatform (KMP) |
| Transport | OkHttp WebSocket (Android) |
| Protocol | JSON over WebSocket |
| Serialization | kotlinx.serialization (Android), native JSON (server) |

---

## Roadmap

- **KakuCommand** — inbound command path from UI to device, enabling interactive debugging beyond observability
- **iOS SDK** — KMP transport, Swift entry point
