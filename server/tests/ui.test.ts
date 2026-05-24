import { describe, test, expect, mock } from "bun:test"
import { UIClientRegistry } from "../src/ui"
import type { KakuEvent } from "kaku-protocol"

describe("UIClientRegistry", () => {
  test("broadcasts to all connected clients", () => {
    const registry = new UIClientRegistry()
    const send1 = mock(() => {})
    const send2 = mock(() => {})
    const ws1 = { send: send1, readyState: 1 } as any
    const ws2 = { send: send2, readyState: 1 } as any

    registry.add(ws1)
    registry.add(ws2)

    const event: KakuEvent = {
      plugin: "network",
      type: "request",
      id: "abc",
      timestamp: 1000,
      payload: {},
    }
    registry.broadcast(event)

    expect(send1).toHaveBeenCalledWith(JSON.stringify(event))
    expect(send2).toHaveBeenCalledWith(JSON.stringify(event))
  })

  test("does not broadcast to removed clients", () => {
    const registry = new UIClientRegistry()
    const send = mock(() => {})
    const ws = { send, readyState: 1 } as any

    registry.add(ws)
    registry.remove(ws)
    registry.broadcast({ plugin: "x", type: "y", id: "z", timestamp: 0, payload: {} })

    expect(send).not.toHaveBeenCalled()
  })
})
