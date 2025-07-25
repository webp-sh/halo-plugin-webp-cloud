<script lang="ts" setup>
import type {
  WebpCloudProxy,
  WebpCloudProxyEditFormState,
  WebpCloudProxyStats,
  WebpCloudResponse,
} from "@/types";
import { fetchApiKey } from "@/utils/fetch-api-key";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import axios, { AxiosError } from "axios";
import { ref, toRefs } from "vue";

const queryClient = useQueryClient();

const props = withDefaults(defineProps<{ proxy: WebpCloudProxy }>(), {});

const { proxy } = toRefs(props);

const emit = defineEmits(["close"]);

const modal = ref<InstanceType<typeof VModal> | null>(null);

const { data: proxyStats, isLoading } = useQuery({
  queryKey: ["plugin-webp-se-cloud:proxy-stats", proxy],
  queryFn: async () => {
    const apiKey = await fetchApiKey();
    const { data } = await axios.get<WebpCloudResponse<WebpCloudProxyStats>>(
      `https://webppt.webp.se/v1/proxy/${props.proxy.proxy_uuid}/stats`,
      {
        headers: {
          "api-key": apiKey,
        },
      },
    );
    return data;
  },
});

const isSubmitting = ref(false);

async function onSubmit(data: WebpCloudProxyEditFormState) {
  isSubmitting.value = true;

  const apiKey = await fetchApiKey();

  if (!apiKey) {
    Toast.error("请先设置 webp.se 的 API Key");
    return;
  }

  try {
    await axios.put(`https://webppt.webp.se/v1/proxy/${props.proxy.proxy_uuid}`, data, {
      headers: {
        "api-key": apiKey,
      },
    });

    Toast.success("保存成功");

    queryClient.invalidateQueries({
      queryKey: ["plugin-webp-se-cloud:remote-proxies"],
    });

    modal.value?.close();
  } catch (error) {
    if (error instanceof AxiosError) {
      Toast.error(error.response?.data.messages);
    } else {
      Toast.error("保存失败，请稍后重试");
    }
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <VModal ref="modal" mount-to-body :width="600" title="编辑代理" @close="emit('close')">
    <FormKit
      id="proxy-edit-form"
      v-slot="{ value }"
      type="form"
      name="proxy-edit-form"
      :disabled="isLoading"
      @submit="onSubmit"
    >
      <FormKit
        label="名称（Proxy Name）"
        :model-value="proxyStats?.data.proxy_name"
        name="proxy_name"
      ></FormKit>
      <FormKit
        label="Proxy User Agent"
        name="proxy_ua"
        :model-value="proxyStats?.data.proxy_ua"
      ></FormKit>
      <FormKit
        label="Proxy CORS Header"
        name="proxy_cors_header"
        :model-value="proxyStats?.data.proxy_cors_header"
      ></FormKit>
      <FormKit
        label="Proxy Allowed Referer"
        name="proxy_allowed_referer"
        :model-value="proxyStats?.data.proxy_allowed_referer"
      ></FormKit>
      <FormKit
        label="图片质量（Current Quality）"
        name="proxy_quality"
        type="range"
        :value="80"
        :max="100"
        :min="10"
        number
        :help="`${value.proxy_quality}%`"
        :model-value="proxyStats?.data.proxy_quality"
      >
      </FormKit>
      <FormKit
        type="repeater"
        label="额外源站请求头（Extra Origin Fetch Headers）"
        name="proxy_extra_headers"
        :model-value="proxyStats?.data.proxy_extra_headers"
        :max="5"
      >
        <FormKit
          label="Header key"
          name="key"
          validation="required|length:1,100|not:host,referer,origin"
        ></FormKit>
        <FormKit label="Header value" name="value" validation="required|length:1,100"> </FormKit>
      </FormKit>
      <FormKit
        help="当代理被禁用时（手动或因为你的配额已用完），你可以指定后续请求处理方式"
        :model-value="proxyStats?.data.proxy_operation_on_disabled"
        name="proxy_operation_on_disabled"
        label="不可用时的处理方式"
        type="select"
        value="redirect"
        :options="[
          {
            value: 'redirect',
            label: '重定向至源图片',
          },
          {
            value: 'deny',
            label: '阻止访问，返回 403',
          },
          {
            value: 'placeholder',
            label: '占位图',
          },
        ]"
      ></FormKit>

      <FormKit
        label="自适应图片大小"
        name="proxy_adaptive_resize"
        :value="false"
        :model-value="proxyStats?.data.proxy_adaptive_resize"
        type="checkbox"
      ></FormKit>

      <FormKit
        v-if="value.proxy_adaptive_resize"
        label="桌面端最大宽度"
        help="单位为像素（px）"
        name="proxy_adaptive_resize_desktop_width"
        :value="1600"
        :model-value="proxyStats?.data.proxy_adaptive_resize_desktop_width"
        type="number"
        number
        validation="required"
      ></FormKit>

      <FormKit
        v-if="value.proxy_adaptive_resize"
        label="移动端最大宽度"
        help="单位为像素（px）"
        name="proxy_adaptive_resize_mobile_width"
        :value="800"
        :model-value="proxyStats?.data.proxy_adaptive_resize_mobile_width"
        type="number"
        number
        validation="required"
      ></FormKit>
      <!-- @unocss-skip-start -->
      <FormKit type="hidden" name="proxy_enabled" :value="true"></FormKit>
      <!-- @unocss-skip-end -->
    </FormKit>
    <template #footer>
      <VSpace>
        <!-- @vue-ignore -->
        <VButton
          type="secondary"
          :loading="isSubmitting"
          :disabled="isLoading"
          @click="$formkit.submit('proxy-edit-form')"
        >
          保存
        </VButton>
        <VButton @click="modal?.close()">关闭</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
