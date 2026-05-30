import { describe, test, expect, mock } from "bun:test"
import { DeviceRegistry } from "../src/device"
import type { HelloMessage } from "@anuress/kaku-protocol"

const hello: HelloMessage = {
  type: "hello",
  platform: "android",
  sdkVersion: 1,
  plugins: ["network"],
}

describe("DeviceRegistry", () => {
  test("registers device on hello", () => {
    const registry = new DeviceRegistry()
    const ws = {} as any
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

  test("assigns a deviceId on hello", () => {
    const registry = new DeviceRegistry()
    const ws = {} as any
    const deviceId = registry.handleHello(ws, hello)
    expect(typeof deviceId).toBe("string")
    expect(deviceId.length).toBeGreaterThan(0)
  })

  test("looks up device by deviceId", () => {
    const registry = new DeviceRegistry()
    const ws = {} as any
    const deviceId = registry.handleHello(ws, hello)
    expect(registry.getByDeviceId(deviceId)?.platform).toBe("android")
  })

  test("getByDeviceId returns undefined for unknown id", () => {
    const registry = new DeviceRegistry()
    expect(registry.getByDeviceId("nonexistent")).toBeUndefined()
  })

  test("sendTo forwards message to the device WebSocket", () => {
    const registry = new DeviceRegistry()
    const send = mock(() => {})
    const ws = { send } as any
    const deviceId = registry.handleHello(ws, hello)
    registry.sendTo(deviceId, '{"hello":"world"}')
    expect(send).toHaveBeenCalledWith('{"hello":"world"}')
  })

  test("sendTo does nothing for unknown deviceId", () => {
    const registry = new DeviceRegistry()
    expect(() => registry.sendTo("unknown", "msg")).not.toThrow()
  })

  test("remove also cleans up byId index", () => {
    const registry = new DeviceRegistry()
    const ws = {} as any
    const deviceId = registry.handleHello(ws, hello)
    registry.remove(ws)
    expect(registry.getByDeviceId(deviceId)).toBeUndefined()
  })

  test("reconnectAll sends reconnect to all registered devices", () => {
    const registry = new DeviceRegistry()
    const send1 = mock(() => {})
    const send2 = mock(() => {})
    const ws1 = { send: send1 } as any
    const ws2 = { send: send2 } as any
    registry.handleHello(ws1, hello)
    registry.handleHello(ws2, hello)

    registry.reconnectAll()

    expect(send1).toHaveBeenCalledWith('{"type":"reconnect"}')
    expect(send2).toHaveBeenCalledWith('{"type":"reconnect"}')
  })

  test("reconnectAll does nothing when no devices are registered", () => {
    const registry = new DeviceRegistry()
    expect(() => registry.reconnectAll()).not.toThrow()
  })

  test("list returns empty array when no devices", () => {
    const registry = new DeviceRegistry()
    expect(registry.list()).toEqual([])
  })

  test("list returns devices with id field not deviceId", () => {
    const registry = new DeviceRegistry()
    const ws = {} as any
    const deviceId = registry.handleHello(ws, hello)
    const [device] = registry.list()
    expect(device.id).toBe(deviceId)
    expect("deviceId" in device).toBe(false)
  })

  test("list includes platform, sdkVersion, plugins", () => {
    const registry = new DeviceRegistry()
    const ws = {} as any
    registry.handleHello(ws, hello)
    const [device] = registry.list()
    expect(device.platform).toBe("android")
    expect(device.sdkVersion).toBe(1)
    expect(device.plugins).toEqual(["network"])
  })

  test("list reflects disconnects", () => {
    const registry = new DeviceRegistry()
    const ws = {} as any
    registry.handleHello(ws, hello)
    expect(registry.list()).toHaveLength(1)
    registry.remove(ws)
    expect(registry.list()).toHaveLength(0)
  })
})
