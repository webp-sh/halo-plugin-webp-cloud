<script lang="ts" setup>
import { useFetchApiKey } from "@/hooks/use-fetch-api-key";
import { consoleApiClient } from "@halo-dev/api-client";
import { Toast, VButton } from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import axios from "axios";
import { computed, ref, watch } from "vue";

const queryClient = useQueryClient();

const { data: apiKey } = useFetchApiKey();

const { data: proxies } = useQuery({
  queryKey: ["plugin-webp-se-cloud:remote-proxies"],
  queryFn: async () => {
    // TODO: Refine type definition
    const { data } = await axios.get("https://webppt.webp.se/v1/proxy", {
      headers: {
        "api-key": apiKey.value,
      },
    });
    return data;
  },
  cacheTime: 0,
  enabled: computed(() => !!apiKey.value),
});

const selectedProxies = ref<string[]>([]);

const { data: storedProxies } = useQuery({
  queryKey: ["plugin-webp-se-cloud:stored-proxies"],
  queryFn: async () => {
    const { data: configMap } =
      await consoleApiClient.plugin.plugin.fetchPluginConfig({
        name: "plugin-webp-se-cloud",
      });

    const configMapData = JSON.parse(configMap.data?.["basic"] || "{}");

    return configMapData["proxies"] || [];
  },
});

watch(
  () => storedProxies.value,
  (value) => {
    // TODO: Refine type definition
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

  const proxiesToSave = proxies.value.data
    .filter((proxy: any) =>
      selectedProxies.value.includes(proxy.proxy_proxy_url),
    )
    .map((proxy: any) => ({
      proxy_url: proxy.proxy_proxy_url,
      origin_url: proxy.proxy_origin_url,
    }));

  const { data: configMapToUpdate } =
    await consoleApiClient.plugin.plugin.fetchPluginConfig({
      name: "plugin-webp-se-cloud",
    });

  const configMapData = JSON.parse(configMapToUpdate.data?.["basic"] || "{}");

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
  <div v-if="apiKey">
    <h2 class="text-sm font-semibold text-gray-900">选择代理：</h2>
    <div class="flex mt-2 justify-end">
      <VButton>新建</VButton>
    </div>
    <div class="flex space-y-2 flex-col mt-3">
      <label
        v-for="proxy in proxies?.data"
        :key="proxy.proxy_uuid"
        :for="proxy.proxy_uuid"
        class="ring-1 relative ring-gray-100 rounded-lg p-3 cursor-pointer transition-all flex has-[:checked]:ring-inherit"
      >
        <div class="flex-1 flex space-y-1.5 flex-col shrink min-w-0">
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
