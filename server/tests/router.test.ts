import { describe, test, expect, mock } from "bun:test"
import { Router } from "../src/router"
import { UIClientRegistry } from "../src/ui"
import { DeviceRegistry } from "../src/device"
import type { KakuCommand } from "@anuress/kaku-protocol"

function makeDevices() {
  return new DeviceRegistry()
}

describe("Router", () => {
  test("forwards valid event to UI clients", () => {
    const uiClients = new UIClientRegistry()
    const broadcast = mock(() => {})
    uiClients.broadcast = broadcast

    const router = new Router(uiClients, makeDevices())
    router.dispatch({
      plugin: "network",
      type: "request",
      id: "abc",
      timestamp: 1000,
      payload: { url: "https://example.com" },
    }, "device-1")

    expect(broadcast).toHaveBeenCalledTimes(1)
    expect(broadcast.mock.calls[0][0].plugin).toBe("network")
    expect(broadcast.mock.calls[0][0].deviceId).toBe("device-1")
  })

  test("ignores messages missing required fields", () => {
    const uiClients = new UIClientRegistry()
    const broadcast = mock(() => {})
    uiClients.broadcast = broadcast

    const router = new Router(uiClients, makeDevices())
    router.dispatch({ type: "request", id: "abc" }, "device-1") // missing plugin

    expect(broadcast).not.toHaveBeenCalled()
  })

  test("does not forward hello messages (no plugin field)", () => {
    const uiClients = new UIClientRegistry()
    const broadcast = mock(() => {})
    uiClients.broadcast = broadcast

    const router = new Router(uiClients, makeDevices())
    router.dispatch({ type: "hello", platform: "android", sdkVersion: 1, plugins: [] }, "device-1")

    expect(broadcast).not.toHaveBeenCalled()
  })

  test("routeCommand forwards command to correct device", () => {
    const uiClients = new UIClientRegistry()
    const devices = new DeviceRegistry()
    const send = mock(() => {})
    const ws = { send } as any
    const deviceId = devices.handleHello(ws, {
      type: "hello",
      platform: "android",
      sdkVersion: 1,
      plugins: [],
    })

    const router = new Router(uiClients, devices)
    const cmd: KakuCommand = {
      plugin: "network",
      type: "clear",
      id: "cmd-1",
      timestamp: 1000,
      payload: {},
      deviceId,
    }
    router.routeCommand(cmd)

    expect(send).toHaveBeenCalledTimes(1)
    const sent = JSON.parse(send.mock.calls[0][0])
    expect(sent.type).toBe("clear")
    expect(sent.plugin).toBe("network")
  })

  test("routeCommand silently drops command for unknown deviceId", () => {
    const uiClients = new UIClientRegistry()
    const router = new Router(uiClients, makeDevices())
    const cmd: KakuCommand = {
      plugin: "network",
      type: "clear",
      id: "cmd-1",
      timestamp: 1000,
      payload: {},
      deviceId: "nonexistent",
    }
    expect(() => router.routeCommand(cmd)).not.toThrow()
  })
})
