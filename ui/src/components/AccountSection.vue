<script lang="ts" setup>
import { useFetchApiKey } from "@/hooks/use-fetch-api-key";
import { VAvatar, VButton, VLoading } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";
import { computed, ref } from "vue";
import ApiKeyBindingModal from "./ApiKeyBindingModal.vue";

const tokenBindingModalVisible = ref(false);

const { data: apiKey, isLoading } = useFetchApiKey();

const { data: user, isFetching } = useQuery({
  queryKey: ["plugin-webp-se-cloud:user"],
  queryFn: async () => {
    const { data } = await axios.get("https://webppt.webp.se/v1/user/info", {
      headers: {
        "api-key": apiKey.value,
      },
    });
    return data;
  },
  cacheTime: 0,
  enabled: computed(() => !!apiKey.value),
});
</script>

<template>
  <VLoading v-if="isLoading || isFetching" />
  <div
    v-else
    class="webp-se-ring-gray-100 webp-se-relative webp-se-flex-wrap webp-se-gap-2 webp-se-flex webp-se-justify-between webp-se-items-center webp-se-ring-1 webp-se-p-2 hover:webp-se-ring-inherit webp-se-transition-all webp-se-rounded-lg"
  >
    <div
      v-if="user?.data"
      class="webp-se-absolute -webp-se-top-3 -webp-se-right-3 webp-se-bg-indigo-500 webp-se-rounded-xl webp-se-px-2 webp-se-text-xs webp-se-py-0.5 webp-se-text-white"
    >
      {{ user.data.user_plan.toUpperCase() }}
    </div>
    <div
      class="webp-se-flex webp-se-gap-3 webp-se-items-center webp-se-flex-wrap"
    >
      <VAvatar :src="user?.data?.avatar_url" size="lg" :alt="user?.data.name" />
      <div class="webp-se-flex webp-se-flex-col webp-se-space-y-1">
        <span
          class="webp-se-text-sm webp-se-text-gray-900 webp-se-font-semibold"
        >
          {{ user?.data.name || "请先绑定 webp.se 的帐号" }}
        </span>
        <span class="webp-se-text-xs webp-se-text-gray-600">
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
