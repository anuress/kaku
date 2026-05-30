# kaku-network

[![kaku-android](https://github.com/anuress/kaku/actions/workflows/publish-android.yml/badge.svg)](https://github.com/anuress/kaku/actions/workflows/publish-android.yml) [![kaku-android version](https://jitpack.io/v/anuress/kaku.svg)](https://jitpack.io/#anuress/kaku)

OkHttp network interceptor plugin for [kaku](https://github.com/anuress/kaku). Captures request and response pairs — method, URL, headers, and body — and streams them to any connected kaku UI client.

## Installation

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
implementation("com.github.anuress.kaku:kaku-network:VERSION")
```

## Setup

```kotlin
class MyApp : Application() {
    val networkPlugin = NetworkPlugin()

    override fun onCreate() {
        super.onCreate()

        Kaku.init {
            register(networkPlugin)
        }

        val okHttp = OkHttpClient.Builder()
            .addInterceptor(networkPlugin.interceptor)
            .build()
    }
}
```

## Events

All events are emitted under `plugin = "network"`. Request and response events share the same `id` for correlation.

### `request`

Emitted before the request is sent.

| Field | Type | Description |
|-------|------|-------------|
| `method` | `String` | HTTP method (`GET`, `POST`, …) |
| `url` | `String` | Full request URL |
| `headers` | `Map<String, String>` | Request headers |
| `body` | `String?` | Request body — text content types only, `null` for binary or absent body |

### `response`

Emitted after the response is received.

| Field | Type | Description |
|-------|------|-------------|
| `statusCode` | `Int` | HTTP status code |
| `headers` | `Map<String, String>` | Response headers |
| `body` | `String?` | Response body — text content types only, `null` for binary |

## Body capture behaviour

| Content type | Captured |
|---|---|
| `application/json`, `application/xml` | Yes |
| `text/*` | Yes |
| `application/x-www-form-urlencoded` | Yes |
| `image/*`, `video/*`, other binary | No — `body` is `null` |
| Body > 256 KB | Truncated — `[truncated: N bytes]` |
| One-shot request body | No — `body` is `null` |

## Security note

All headers are captured, including `Authorization` and `Cookie`. Do not include this interceptor in production builds.

```kotlin
val okHttp = OkHttpClient.Builder()
    .apply {
        if (BuildConfig.DEBUG) addInterceptor(networkPlugin.interceptor)
    }
    .build()
```
