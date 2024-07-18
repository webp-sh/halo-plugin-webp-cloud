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
    class="ring-gray-100 relative flex-wrap gap-2 flex justify-between items-center ring-1 p-2 hover:ring-inherit transition-all rounded-lg"
  >
    <div
      v-if="user?.data"
      class="absolute -top-3 -right-3 bg-indigo-500 rounded-xl px-2 text-xs py-0.5 text-white"
    >
      {{ user.data.user_plan.toUpperCase() }}
    </div>
    <div class="flex gap-3 items-center flex-wrap">
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
