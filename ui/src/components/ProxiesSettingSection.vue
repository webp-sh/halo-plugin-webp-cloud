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
import ProxyCreationModal from "./ProxyCreationModal.vue";
import ProxyListItem from "./ProxyListItem.vue";

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

// Proxy creation
const proxyCreationModalVisible = ref(false);
</script>

<template>
  <div v-if="proxies" class="mt-3 border-t border-gray-50 pt-3">
    <h2 class="text-sm text-gray-900 font-semibold">选择代理：</h2>
    <div class="mt-2 flex justify-end">
      <VButton @click="proxyCreationModalVisible = true">新建</VButton>
    </div>
    <div class="mt-3 flex flex-col space-y-2">
      <ProxyListItem
        v-for="proxy in proxies?.data"
        :key="proxy.proxy_uuid"
        :proxy="proxy"
      >
        <template #checkbox>
          <input
            :id="proxy.proxy_uuid"
            v-model="selectedProxies"
            type="checkbox"
            :value="proxy.proxy_proxy_url"
          />
        </template>
      </ProxyListItem>
    </div>

    <div class="mt-5">
      <VButton type="secondary" :loading="isSubmitting" @click="handleSave">
        保存
      </VButton>
    </div>

    <ProxyCreationModal
      v-if="proxyCreationModalVisible"
      @close="proxyCreationModalVisible = false"
    />
  </div>
</template>
