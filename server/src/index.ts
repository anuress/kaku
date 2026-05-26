import { HOST, DEVICE_PORT, UI_PORT } from "./config"
import { DeviceRegistry } from "./device"
import { UIClientRegistry } from "./ui"
import { Router } from "./router"
import { setupAdbReverse } from "./adb"
import type { HelloMessage, KakuCommand } from "@anuress/kaku-protocol"

const devices = new DeviceRegistry()
const uiClients = new UIClientRegistry()
const router = new Router(uiClients, devices)

setupAdbReverse()

Bun.serve({
  port: DEVICE_PORT,
  hostname: HOST,
  fetch(req, server) {
    if (!server.upgrade(req)) {
      return new Response("kaku device endpoint", { status: 200 })
    }
  },
  websocket: {
    open(ws) {
      console.log(`[kaku] device ws connected`)
    },
    message(ws, data) {
      let msg: Record<string, unknown>
      try {
        msg = JSON.parse(data.toString())
      } catch {
        return
      }
      if (msg.type === "hello") {
        const deviceId = devices.handleHello(ws, msg as unknown as HelloMessage)
        ws.send(JSON.stringify({ type: "hello_ack", deviceId }))
        return
      }
      router.dispatch(msg)
    },
    close(ws) {
      devices.remove(ws)
      console.log(`[kaku] device ws disconnected`)
    },
  },
})

Bun.serve({
  port: UI_PORT,
  hostname: HOST,
  fetch(req, server) {
    if (!server.upgrade(req)) {
      return new Response("kaku ui endpoint", { status: 200 })
    }
  },
  websocket: {
    open(ws) {
      uiClients.add(ws)
      console.log(`[kaku] UI client connected`)
    },
    message(ws, data) {
      let msg: Record<string, unknown>
      try {
        msg = JSON.parse(data.toString())
      } catch {
        return
      }
      if (msg.deviceId && msg.plugin && msg.type && msg.id) {
        router.routeCommand(msg as unknown as KakuCommand)
      }
    },
    close(ws) {
      uiClients.remove(ws)
      console.log(`[kaku] UI client disconnected`)
    },
  },
})

console.log(`[kaku] server running`)
console.log(`[kaku]   device  → ws://${HOST}:${DEVICE_PORT}`)
console.log(`[kaku]   UI      → ws://${HOST}:${UI_PORT}`)
