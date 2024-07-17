import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import SettingsTab from "./components/SettingsTab.vue";
import "./styles/index.css";

export default definePlugin({
  components: {},
  routes: [],
  extensionPoints: {
    "plugin:self:tabs:create": () => {
      return [
        {
          id: "webp-se-settings",
          label: "WebP Cloud 服务设置",
          component: markRaw(SettingsTab),
          permissions: ["*"],
        },
      ];
    },
  },
});
