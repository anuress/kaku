import type { KakuEvent } from "kaku-protocol"
import type { UIClientRegistry } from "./ui"

export class Router {
  constructor(private uiClients: UIClientRegistry) {}

  dispatch(raw: string | Buffer): void {
    let msg: Record<string, unknown>
    try {
      msg = JSON.parse(raw.toString())
    } catch {
      return
    }

    if (msg.type === "hello") return
    if (!msg.plugin || !msg.type || !msg.id) return

    this.uiClients.broadcast(msg as KakuEvent)
  }
}
