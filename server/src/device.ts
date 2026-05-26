import type { ServerWebSocket } from "bun"
import type { HelloMessage } from "@anuress/kaku-protocol"

interface Device {
  ws: ServerWebSocket
  platform: string
  sdkVersion: number
  plugins: string[]
  deviceId: string
}

export class DeviceRegistry {
  private devices = new Map<ServerWebSocket, Device>()
  private byId = new Map<string, Device>()

  handleHello(ws: ServerWebSocket, hello: HelloMessage): string {
    const deviceId = crypto.randomUUID()
    const device: Device = {
      ws,
      platform: hello.platform,
      sdkVersion: hello.sdkVersion,
      plugins: hello.plugins,
      deviceId,
    }
    this.devices.set(ws, device)
    this.byId.set(deviceId, device)
    console.log(`[kaku] device connected: ${hello.platform} id=${deviceId} plugins=[${hello.plugins.join(", ")}]`)
    return deviceId
  }

  remove(ws: ServerWebSocket): void {
    const device = this.devices.get(ws)
    if (device) {
      this.byId.delete(device.deviceId)
      this.devices.delete(ws)
    }
  }

  get(ws: ServerWebSocket): Device | undefined {
    return this.devices.get(ws)
  }

  getByDeviceId(deviceId: string): Device | undefined {
    return this.byId.get(deviceId)
  }

  sendTo(deviceId: string, message: string): void {
    this.byId.get(deviceId)?.ws.send(message)
  }
}
