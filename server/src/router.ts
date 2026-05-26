import type { KakuEvent, KakuCommand } from "@anuress/kaku-protocol"
import type { UIClientRegistry } from "./ui"
import type { DeviceRegistry } from "./device"

export class Router {
  constructor(
    private uiClients: UIClientRegistry,
    private devices: DeviceRegistry,
  ) {}

  dispatch(msg: Record<string, unknown>): void {
    if (!msg.plugin || !msg.type || !msg.id) return
    this.uiClients.broadcast(msg as KakuEvent)
  }

  routeCommand(cmd: KakuCommand): void {
    this.devices.sendTo(cmd.deviceId, JSON.stringify(cmd))
  }
}
