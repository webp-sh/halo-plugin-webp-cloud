<script lang="ts" setup>
import type { WebpCloudResponse, WebpCloudUser } from "@/types";
import { fetchApiKey } from "@/utils/fetch-api-key";
import { VAvatar, VButton } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";
import { ref } from "vue";
import ApiKeyBindingModal from "./ApiKeyBindingModal.vue";

const tokenBindingModalVisible = ref(false);

const { data: user, suspense } = useQuery({
  queryKey: ["plugin-webp-se-cloud:user"],
  queryFn: async () => {
    const apiKey = await fetchApiKey();

    if (!apiKey) {
      return null;
    }

    const { data } = await axios.get<WebpCloudResponse<WebpCloudUser>>(
      "https://webppt.webp.se/v1/user/info",
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
</script>

<template>
  <div
    class="relative flex flex-wrap items-center justify-between gap-2 rounded-lg p-2 ring-1 ring-gray-100 transition-all hover:ring-inherit"
  >
    <div
      v-if="user?.data"
      class="absolute rounded-xl bg-indigo-500 px-2 py-0.5 text-xs text-white -right-3 -top-3"
    >
      {{ user.data.user_plan.toUpperCase() }}
    </div>
    <div class="flex flex-wrap items-center gap-3">
      <VAvatar :src="user?.data?.avatar_url" size="lg" :alt="user?.data.name" />
      <div class="flex flex-col space-y-1">
        <span class="text-sm text-gray-900 font-semibold">
          {{ user?.data.name || "请先绑定 webp.se 的帐号" }}
        </span>
        <span class="text-xs text-gray-600">
          {{ user?.data.user_uuid || "--" }}
        </span>
      </div>
    </div>
    <div>
      <VButton size="sm" @click="tokenBindingModalVisible = true">
        {{ !!user ? "设置" : "绑定" }}
      </VButton>
    </div>
  </div>

  <ApiKeyBindingModal
    v-if="tokenBindingModalVisible"
    @close="tokenBindingModalVisible = false"
  />
</template>
