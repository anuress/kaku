import { describe, test, expect } from "bun:test"
import { DeviceRegistry } from "../src/device"
import type { HelloMessage } from "kaku-protocol"

describe("DeviceRegistry", () => {
  test("registers device on hello", () => {
    const registry = new DeviceRegistry()
    const ws = {} as any
    const hello: HelloMessage = {
      type: "hello",
      platform: "android",
      sdkVersion: 1,
      plugins: ["network"],
    }
    registry.handleHello(ws, hello)
    expect(registry.get(ws)?.platform).toBe("android")
    expect(registry.get(ws)?.plugins).toEqual(["network"])
  })

  test("removes device on disconnect", () => {
    const registry = new DeviceRegistry()
    const ws = {} as any
    registry.handleHello(ws, { type: "hello", platform: "android", sdkVersion: 1, plugins: [] })
    registry.remove(ws)
    expect(registry.get(ws)).toBeUndefined()
  })
})
