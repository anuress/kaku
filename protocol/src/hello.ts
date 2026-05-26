export interface HelloMessage {
  type: "hello"
  platform: string
  sdkVersion: number
  plugins: string[]
}

export interface HelloAckMessage {
  type: "hello_ack"
  deviceId: string
}
