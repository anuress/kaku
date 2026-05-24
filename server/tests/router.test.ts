import { describe, test, expect, mock } from "bun:test"
import { Router } from "../src/router"
import { UIClientRegistry } from "../src/ui"

describe("Router", () => {
  test("forwards valid event to UI clients", () => {
    const uiClients = new UIClientRegistry()
    const broadcast = mock(() => {})
    uiClients.broadcast = broadcast

    const router = new Router(uiClients)
    router.dispatch({
      plugin: "network",
      type: "request",
      id: "abc",
      timestamp: 1000,
      payload: { url: "https://example.com" },
    })

    expect(broadcast).toHaveBeenCalledTimes(1)
    expect(broadcast.mock.calls[0][0].plugin).toBe("network")
  })

  test("ignores messages missing required fields", () => {
    const uiClients = new UIClientRegistry()
    const broadcast = mock(() => {})
    uiClients.broadcast = broadcast

    const router = new Router(uiClients)
    router.dispatch({ type: "request", id: "abc" }) // missing plugin

    expect(broadcast).not.toHaveBeenCalled()
  })
})
