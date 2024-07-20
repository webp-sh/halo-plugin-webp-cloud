<script lang="ts" setup>
import type { WebpCloudProxy } from "@/types";
import { VSpace } from "@halo-dev/components";
import { ref } from "vue";
import ProxyEditModal from "./ProxyEditModal.vue";

const props = withDefaults(defineProps<{ proxy: WebpCloudProxy }>(), {});

function openDetailPage() {
  window.open(
    `https://dashboard.webp.se/proxy/${props.proxy.proxy_uuid}`,
    "_blank",
  );
}

const proxyEditModalVisible = ref(false);
</script>

<template>
  <label
    :for="proxy.proxy_uuid"
    class="relative flex flex-col cursor-pointer rounded-lg p-3 ring-1 ring-gray-100 transition-all has-[:checked]:ring-inherit"
    @click.stop
  >
    <div class="flex flex-col space-y-1.5 w-full">
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
    <div class="flex justify-end">
      <VSpace>
        <button
          class="btn-sm btn-default btn"
          type="button"
          @click.prevent="proxyEditModalVisible = true"
        >
          <span class="btn-content"> 编辑 </span>
        </button>
        <button
          class="btn-sm btn-default btn"
          type="button"
          @click.prevent="openDetailPage"
        >
          <span class="btn-content"> 详情 </span>
        </button>
      </VSpace>
    </div>
    <div class="absolute right-3 top-3">
      <slot name="checkbox" />
    </div>

    <ProxyEditModal
      v-if="proxyEditModalVisible"
      :proxy="proxy"
      @close="proxyEditModalVisible = false"
    />
  </label>
</template>
