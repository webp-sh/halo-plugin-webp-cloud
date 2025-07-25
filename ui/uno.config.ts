import { presetWind3, transformerCompileClass } from "unocss";
import { defineConfig } from "unocss/vite";

export default defineConfig({
  presets: [presetWind3()],
  transformers: [transformerCompileClass()],
});
