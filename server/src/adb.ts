import { spawnSync } from "child_process"
import { DEVICE_PORT } from "./config"

export function setupAdbReverse(): void {
  const result = spawnSync("adb", [
    "reverse",
    `tcp:${DEVICE_PORT}`,
    `tcp:${DEVICE_PORT}`,
  ])
  if (result.status === 0) {
    console.log(`[kaku] adb reverse tcp:${DEVICE_PORT} ok`)
  } else {
    console.warn(`[kaku] adb reverse failed — is a device connected?`)
  }
}
