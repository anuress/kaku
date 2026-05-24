import type { ServerWebSocket } from "bun"
import type { HelloMessage } from "kaku-protocol"

interface Device {
  ws: ServerWebSocket
  platform: string
  sdkVersion: number
  plugins: string[]
}

export class DeviceRegistry {
  private devices = new Map<ServerWebSocket, Device>()

  handleHello(ws: ServerWebSocket, hello: HelloMessage): void {
    this.devices.set(ws, {
      ws,
      platform: hello.platform,
      sdkVersion: hello.sdkVersion,
      plugins: hello.plugins,
    })
    console.log(`[kaku] device connected: ${hello.platform} plugins=[${hello.plugins.join(", ")}]`)
  }

  remove(ws: ServerWebSocket): void {
    this.devices.delete(ws)
  }

  // Reserved for future per-device command routing (v2)
  get(ws: ServerWebSocket): Device | undefined {
    return this.devices.get(ws)
  }
}
