import type { ServerWebSocket } from "bun"
import type { KakuEvent } from "kaku-protocol"

export class UIClientRegistry {
  private clients = new Set<ServerWebSocket>()

  add(ws: ServerWebSocket): void {
    this.clients.add(ws)
  }

  remove(ws: ServerWebSocket): void {
    this.clients.delete(ws)
  }

  broadcast(event: KakuEvent): void {
    const data = JSON.stringify(event)
    for (const client of this.clients) {
      client.send(data)
    }
  }
}
