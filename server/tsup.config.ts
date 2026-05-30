import { defineConfig } from "tsup"

export default defineConfig({
  entry: ["src/index.ts"],
  format: ["cjs"],
  target: "node18",
  clean: true,
  dts: false,
  banner: { js: "#!/usr/bin/env node" },
  external: ["ws", "@anuress/kaku-protocol"],
})
