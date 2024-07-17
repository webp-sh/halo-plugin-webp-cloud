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
    <h2 class="webp-se-text-sm webp-se-font-semibold webp-se-text-gray-900">
      选择代理：
    </h2>
    <div class="webp-se-flex webp-se-mt-2 webp-se-justify-end">
      <VButton>新建</VButton>
    </div>
    <div class="webp-se-flex webp-se-space-y-2 webp-se-flex-col webp-se-mt-3">
      <label
        v-for="proxy in proxies?.data"
        :key="proxy.proxy_uuid"
        :for="proxy.proxy_uuid"
        class="webp-se-ring-1 webp-se-relative webp-se-ring-gray-100 webp-se-rounded-lg webp-se-p-3 webp-se-cursor-pointer webp-se-transition-all webp-se-flex has-[:checked]:webp-se-ring-inherit"
      >
        <div
          class="webp-se-flex-1 webp-se-flex webp-se-space-y-1.5 webp-se-flex-col webp-se-shrink webp-se-min-w-0"
        >
          <span class="webp-se-text-gray-900 webp-se-font-semibold">
            {{ proxy.proxy_name }}
          </span>
          <span class="webp-se-text-xs webp-se-text-gray-600">
            代理地址：{{ proxy.proxy_proxy_url }}
          </span>
          <span class="webp-se-text-xs webp-se-text-gray-600">
            源站地址：{{ proxy.proxy_origin_url }}
          </span>
          <span class="webp-se-text-xs webp-se-text-gray-600">
            地区：{{ proxy.proxy_region }}
          </span>
        </div>
        <div class="webp-se-flex-none">
          <input
            :id="proxy.proxy_uuid"
            v-model="selectedProxies"
            type="checkbox"
            :value="proxy.proxy_proxy_url"
          />
        </div>
      </label>
    </div>

    <div class="webp-se-mt-5">
      <VButton type="secondary" :loading="isSubmitting" @click="handleSave">
        保存
      </VButton>
    </div>
  </div>
</template>
