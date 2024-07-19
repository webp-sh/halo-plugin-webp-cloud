<script lang="ts" setup>
import type {
  PluginConfigMapBasic,
  PluginConfigMapProxy,
  WebpCloudProxy,
  WebpCloudResponse,
} from "@/types";
import { fetchApiKey } from "@/utils/fetch-api-key";
import { consoleApiClient } from "@halo-dev/api-client";
import { Toast, VButton } from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import axios from "axios";
import { ref, watch } from "vue";

const queryClient = useQueryClient();

const { data: proxies, suspense } = useQuery({
  queryKey: ["plugin-webp-se-cloud:remote-proxies"],
  queryFn: async () => {
    const apiKey = await fetchApiKey();

    if (!apiKey) {
      return null;
    }

    const { data } = await axios.get<WebpCloudResponse<WebpCloudProxy[]>>(
      "https://webppt.webp.se/v1/proxy",
      {
        headers: {
          "api-key": apiKey,
        },
      },
    );
    return data;
  },
  cacheTime: 0,
});

await suspense();

const selectedProxies = ref<string[]>([]);

const { data: storedProxies } = useQuery({
  queryKey: ["plugin-webp-se-cloud:stored-proxies"],
  queryFn: async () => {
    const { data: configMap } =
      await consoleApiClient.plugin.plugin.fetchPluginConfig({
        name: "plugin-webp-se-cloud",
      });

    const configMapData = JSON.parse(
      configMap.data?.["basic"] || "{}",
    ) as PluginConfigMapBasic;

    return configMapData["proxies"] || [];
  },
});

watch(
  () => storedProxies.value,
  (value) => {
    const proxyUrls = value?.map((proxy: any) => proxy.proxy_url);

    selectedProxies.value = proxyUrls || [];
  },
  {
    immediate: true,
  },
);

const isSubmitting = ref(false);

async function handleSave() {
  isSubmitting.value = true;

  const proxiesToSave = proxies.value?.data
    .filter((proxy: WebpCloudProxy) =>
      selectedProxies.value.includes(proxy.proxy_proxy_url),
    )
    .map((proxy: WebpCloudProxy) => ({
      proxy_url: proxy.proxy_proxy_url,
      origin_url: proxy.proxy_origin_url,
    })) as PluginConfigMapProxy[];

  const { data: configMapToUpdate } =
    await consoleApiClient.plugin.plugin.fetchPluginConfig({
      name: "plugin-webp-se-cloud",
    });

  const configMapData = JSON.parse(
    configMapToUpdate.data?.["basic"] || "{}",
  ) as PluginConfigMapBasic;

  configMapData["proxies"] = proxiesToSave;

  await consoleApiClient.plugin.plugin.updatePluginConfig({
    name: "plugin-webp-se-cloud",
    configMap: {
      ...configMapToUpdate,
      data: {
        ...configMapToUpdate.data,
        basic: JSON.stringify(configMapData),
      },
    },
  });

  queryClient.invalidateQueries({
    queryKey: ["plugin-webp-se-cloud:stored-proxies"],
  });

  Toast.success("保存成功");

  isSubmitting.value = false;
}
</script>

<template>
  <div v-if="proxies" class="mt-3 border-t border-gray-50 pt-3">
    <h2 class="text-sm text-gray-900 font-semibold">选择代理：</h2>
    <div class="mt-2 flex justify-end">
      <VButton>新建</VButton>
    </div>
    <div class="mt-3 flex flex-col space-y-2">
      <label
        v-for="proxy in proxies?.data"
        :key="proxy.proxy_uuid"
        :for="proxy.proxy_uuid"
        class="relative flex cursor-pointer rounded-lg p-3 ring-1 ring-gray-100 transition-all has-[:checked]:ring-inherit"
      >
        <div class="min-w-0 flex flex-1 shrink flex-col space-y-1.5">
          <span class="text-gray-900 font-semibold">
            {{ proxy.proxy_name }}
          </span>
          <span class="text-xs text-gray-600">
            代理地址：{{ proxy.proxy_proxy_url }}
          </span>
          <span class="text-xs text-gray-600">
            源站地址：{{ proxy.proxy_origin_url }}
          </span>
          <span class="text-xs text-gray-600">
            地区：{{ proxy.proxy_region }}
          </span>
        </div>
        <div class="flex-none">
          <input
            :id="proxy.proxy_uuid"
            v-model="selectedProxies"
            type="checkbox"
            :value="proxy.proxy_proxy_url"
          />
        </div>
      </label>
    </div>

    <div class="mt-5">
      <VButton type="secondary" :loading="isSubmitting" @click="handleSave">
        保存
      </VButton>
    </div>
  </div>
</template>
