export interface HelloMessage {
  type: "hello"
  platform: string
  sdkVersion: number
  plugins: string[]
}
