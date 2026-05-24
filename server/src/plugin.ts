import type { KakuMessage } from "kaku-protocol"

export type Sender = (msg: KakuMessage) => void

export interface KakuPlugin {
  id: string
  onMessage(msg: KakuMessage, send: Sender): void
  onConnect?(): void
  onDisconnect?(): void
}
