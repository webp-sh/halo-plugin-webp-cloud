import { definePlugin } from "@halo-dev/console-shared";
import { defineAsyncComponent } from "vue";
import "uno.css";
import { VLoading } from "@halo-dev/components";

export default definePlugin({
  components: {},
  routes: [],
  extensionPoints: {
    "plugin:self:tabs:create": () => {
      return [
        {
          id: "webp-se-settings",
          label: "WebP Cloud 服务设置",
          component: defineAsyncComponent({
            loader: () => import("./components/SettingsTab.vue"),
            loadingComponent: VLoading,
          }),
          permissions: ["*"],
        },
      ];
    },
  },
});
