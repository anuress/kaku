import type { WebSocket } from "ws"
import type { HelloMessage } from "@anuress/kaku-protocol"

export interface DeviceInfo {
  id: string
  platform: string
  sdkVersion: number
  plugins: string[]
}

interface Device {
  ws: WebSocket
  platform: string
  sdkVersion: number
  plugins: string[]
  deviceId: string
}

export class DeviceRegistry {
  private devices = new Map<WebSocket, Device>()
  private byId = new Map<string, Device>()

  handleHello(ws: WebSocket, hello: HelloMessage): string {
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

  remove(ws: WebSocket): void {
    const device = this.devices.get(ws)
    if (device) {
      this.byId.delete(device.deviceId)
      this.devices.delete(ws)
    }
  }

  get(ws: WebSocket): Device | undefined {
    return this.devices.get(ws)
  }

  getByDeviceId(deviceId: string): Device | undefined {
    return this.byId.get(deviceId)
  }

  sendTo(deviceId: string, message: string): void {
    this.byId.get(deviceId)?.ws.send(message)
  }

  reconnectAll(): void {
    const msg = JSON.stringify({ type: "reconnect" })
    for (const device of this.devices.values()) {
      device.ws.send(msg)
    }
  }

  list(): DeviceInfo[] {
    return Array.from(this.byId.values()).map(({ deviceId, platform, sdkVersion, plugins }) => ({
      id: deviceId,
      platform,
      sdkVersion,
      plugins,
    }))
  }
}
