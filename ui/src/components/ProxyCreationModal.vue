<script lang="ts" setup>
import type { WebpCloudProxyFormState } from "@/types";
import { fetchApiKey } from "@/utils/fetch-api-key";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import axios, { AxiosError } from "axios";
import { ref } from "vue";

const queryClient = useQueryClient();

const emit = defineEmits(["close"]);

const modal = ref<InstanceType<typeof VModal> | null>(null);

const isSubmitting = ref(false);

async function onSubmit(data: WebpCloudProxyFormState) {
  isSubmitting.value = true;

  const apiKey = await fetchApiKey();

  if (!apiKey) {
    Toast.error("请先设置 webp.se 的 API Key");
    return;
  }

  try {
    await axios.post("https://webppt.webp.se/v1/proxy", data, {
      headers: {
        "api-key": apiKey,
      },
    });

    Toast.success("创建成功");

    queryClient.invalidateQueries({
      queryKey: ["plugin-webp-se-cloud:remote-proxies"],
    });

    modal.value?.close();
  } catch (error) {
    if (error instanceof AxiosError) {
      Toast.error(error.response?.data.messages);
    } else {
      Toast.error("创建失败，请稍后重试");
    }
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <VModal ref="modal" :width="600" title="创建代理" @close="emit('close')">
    <FormKit
      id="proxy-creation-form"
      type="form"
      name="proxy-creation-form"
      @submit="onSubmit"
    >
      <FormKit
        label="区域（Proxy Region）"
        name="proxy_region"
        type="radio"
        value="NUE"
        :options="[
          {
            label: 'Nuremberg',
            value: 'NUE',
          },
          {
            label: 'Helsinki（当前不可用）',
            value: 'HEL',
            attrs: {
              disabled: true,
            },
          },
          {
            label: 'Hillsboro,OR',
            value: 'HIO',
          },
        ]"
      ></FormKit>
      <FormKit label="名称（Proxy Name）" name="proxy_name"></FormKit>
      <FormKit
        label="源站地址（Proxy Origin URL）"
        name="proxy_origin_url"
        help="如果你的源站图片地址是 https://image.example.com/path/to/your/image.jpg，那么应该填写 https://image.example.com。针对于 Halo，这里通常应该填写外部访问地址"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <!-- @vue-ignore -->
        <VButton
          type="secondary"
          :loading="isSubmitting"
          @click="$formkit.submit('proxy-creation-form')"
        >
          创建
        </VButton>
        <VButton @click="modal?.close()">关闭</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
