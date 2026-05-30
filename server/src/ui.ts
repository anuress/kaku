import type { WebSocket } from "ws"
import type { KakuEvent } from "@anuress/kaku-protocol"
import type { DeviceInfo } from "./device"

export class UIClientRegistry {
  private clients = new Set<WebSocket>()

  add(ws: WebSocket): void {
    this.clients.add(ws)
  }

  remove(ws: WebSocket): void {
    this.clients.delete(ws)
  }

  broadcast(event: KakuEvent): void {
    const data = JSON.stringify(event)
    for (const client of this.clients) {
      client.send(data)
    }
  }

  sendDeviceList(devices: DeviceInfo[]): void {
    const data = JSON.stringify({ type: "device-list", devices })
    for (const client of this.clients) {
      client.send(data)
    }
  }
}
