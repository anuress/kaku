export interface KakuMessage {
  plugin: string
  type: string
  id: string
  timestamp: number
  payload: unknown
}

export interface KakuEvent extends KakuMessage {}

export interface KakuCommand extends KakuMessage {
  replyTo?: string
}
