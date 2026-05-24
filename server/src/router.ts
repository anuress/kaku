import type { KakuEvent } from "@anuress/kaku-protocol"
import type { UIClientRegistry } from "./ui"

export class Router {
  constructor(private uiClients: UIClientRegistry) {}

  dispatch(msg: Record<string, unknown>): void {
    if (!msg.plugin || !msg.type || !msg.id) return
    this.uiClients.broadcast(msg as KakuEvent)
  }
}
