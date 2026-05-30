import http from "http"
import { WebSocketServer, WebSocket } from "ws"
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

// Device server
const deviceServer = http.createServer((_req, res) => {
  res.writeHead(200)
  res.end("kaku device endpoint")
})
const deviceWss = new WebSocketServer({ server: deviceServer })

deviceWss.on("connection", (ws: WebSocket) => {
  console.log(`[kaku] device ws connected`)

  ws.on("message", (data) => {
    let msg: Record<string, unknown>
    try {
      msg = JSON.parse(data.toString())
    } catch {
      return
    }
    if (msg.type === "hello") {
      const deviceId = devices.handleHello(ws, msg as unknown as HelloMessage)
      ws.send(JSON.stringify({ type: "hello_ack", deviceId }))
      uiClients.sendDeviceList(devices.list())
      return
    }
    const device = devices.get(ws)
    if (!device) return
    router.dispatch(msg, device.deviceId)
  })

  ws.on("close", () => {
    devices.remove(ws)
    uiClients.sendDeviceList(devices.list())
    console.log(`[kaku] device ws disconnected`)
  })
})

deviceServer.listen(DEVICE_PORT, HOST)

// UI server
const uiServer = http.createServer((_req, res) => {
  res.writeHead(200)
  res.end("kaku ui endpoint")
})
const uiWss = new WebSocketServer({ server: uiServer })

uiWss.on("connection", (ws: WebSocket) => {
  uiClients.add(ws)
  devices.reconnectAll()
  uiClients.sendDeviceList(devices.list())
  console.log(`[kaku] UI client connected`)

  ws.on("message", (data) => {
    let msg: Record<string, unknown>
    try {
      msg = JSON.parse(data.toString())
    } catch {
      return
    }
    if (msg.deviceId && msg.plugin && msg.type && msg.id) {
      router.routeCommand(msg as unknown as KakuCommand)
    }
  })

  ws.on("close", () => {
    uiClients.remove(ws)
    console.log(`[kaku] UI client disconnected`)
  })
})

uiServer.listen(UI_PORT, HOST)

console.log(`[kaku] server running`)
console.log(`[kaku]   device  → ws://${HOST}:${DEVICE_PORT}`)
console.log(`[kaku]   UI      → ws://${HOST}:${UI_PORT}`)
