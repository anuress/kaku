import { describe, test, expect, mock } from "bun:test"
import { Router } from "../src/router"
import { UIClientRegistry } from "../src/ui"

describe("Router", () => {
  test("forwards valid event to UI clients", () => {
    const uiClients = new UIClientRegistry()
    const broadcast = mock(() => {})
    uiClients.broadcast = broadcast

    const router = new Router(uiClients)
    const raw = JSON.stringify({
      plugin: "network",
      type: "request",
      id: "abc",
      timestamp: 1000,
      payload: { url: "https://example.com" },
    })
    router.dispatch(raw)

    expect(broadcast).toHaveBeenCalledTimes(1)
    expect(broadcast.mock.calls[0][0].plugin).toBe("network")
  })

  test("ignores hello messages (not forwarded to UI)", () => {
    const uiClients = new UIClientRegistry()
    const broadcast = mock(() => {})
    uiClients.broadcast = broadcast

    const router = new Router(uiClients)
    router.dispatch(JSON.stringify({ type: "hello", platform: "android", sdkVersion: 1, plugins: [] }))

    expect(broadcast).not.toHaveBeenCalled()
  })

  test("ignores malformed messages", () => {
    const uiClients = new UIClientRegistry()
    const broadcast = mock(() => {})
    uiClients.broadcast = broadcast

    const router = new Router(uiClients)
    router.dispatch("not-json")

    expect(broadcast).not.toHaveBeenCalled()
  })
})
