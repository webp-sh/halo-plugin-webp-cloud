<script lang="ts" setup>
import type { PluginConfigMapBasic } from "@/types";
import {
  consoleApiClient,
  coreApiClient,
  type ConfigMap,
} from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { computed, ref } from "vue";

const queryClient = useQueryClient();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const emit = defineEmits(["close"]);

const { data: configMap } = useQuery({
  queryKey: ["plugin-webp-se-cloud:configMap"],
  queryFn: async () => {
    const { data } = await consoleApiClient.plugin.plugin.fetchPluginConfig(
      {
        name: "plugin-webp-se-cloud",
      },
      {
        mute: true,
      },
    );
    return data;
  },
});

const storedApiKeySecret = computed(() => {
  const configMapData = JSON.parse(
    configMap.value?.data?.["basic"] || "{}",
  ) as PluginConfigMapBasic;
  return configMapData["apiKeySecret"];
});

const isSubmitting = ref(false);

async function onSubmit(data: { apiKeySecret: string }) {
  try {
    isSubmitting.value = true;

    if (!configMap.value) {
      // Create a new configMap
      const configMapToCreate: ConfigMap = {
        data: {
          basic: JSON.stringify({
            apiKeySecret: data.apiKeySecret,
            proxies: [],
          }),
        },
        apiVersion: "v1alpha1",
        kind: "ConfigMap",
        metadata: {
          name: "plugin-webp-se-cloud-configmap",
        },
      };

      await coreApiClient.configMap.createConfigMap({
        configMap: configMapToCreate,
      });
    } else {
      const { data: configMapToUpdate } =
        await consoleApiClient.plugin.plugin.fetchPluginConfig({
          name: "plugin-webp-se-cloud",
        });
      const basicConfig = JSON.parse(
        configMapToUpdate?.data?.["basic"] || "{}",
      ) as PluginConfigMapBasic;
      basicConfig.apiKeySecret = data.apiKeySecret;
      configMapToUpdate.data = {
        ...configMapToUpdate.data,
        basic: JSON.stringify(basicConfig),
      };

      await consoleApiClient.plugin.plugin.updatePluginConfig({
        name: "plugin-webp-se-cloud",
        configMap: configMapToUpdate,
      });
    }

    queryClient.invalidateQueries({
      queryKey: ["plugin-webp-se-cloud:user"],
    });
    queryClient.invalidateQueries({
      queryKey: ["plugin-webp-se-cloud:remote-proxies"],
    });
    queryClient.invalidateQueries({
      queryKey: ["plugin-webp-se-cloud:stored-proxies"],
    });

    modal.value?.close();
    Toast.success("保存成功");
  } catch (error) {
    Toast.error("保存失败，请重试");
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <VModal
    ref="modal"
    title="绑定 API Key"
    :width="600"
    height="50vh"
    mount-to-body
    @close="emit('close')"
  >
    <FormKit
      id="webp-se-api-key-binding-form"
      type="form"
      name="webp-se-api-key-binding-form"
      @submit="onSubmit"
    >
      <FormKit
        type="secret"
        required-key="apiKey"
        :model-value="storedApiKeySecret"
        name="apiKeySecret"
        label="API Key"
        help="访问 https://dashboard.webp.se/ 获取 API Key"
      ></FormKit>
    </FormKit>

    <template #footer>
      <VSpace>
        <!-- @vue-ignore -->
        <VButton
          type="secondary"
          :loading="isSubmitting"
          @click="$formkit.submit('webp-se-api-key-binding-form')"
        >
          保存
        </VButton>
        <VButton @click="modal?.close()">取消</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
