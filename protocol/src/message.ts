export interface KakuMessage {
  plugin: string
  type: string
  id: string
  timestamp: number
  payload: unknown
}

export interface KakuEvent extends KakuMessage {}

// v2: inbound path from UI to device — not yet dispatched by server or handled by Android SDK
export interface KakuCommand extends KakuMessage {
  deviceId: string
  replyTo?: string
}
