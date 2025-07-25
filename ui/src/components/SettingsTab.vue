<script lang="ts" setup>
import { VLoading } from "@halo-dev/components";
import AccountSection from "./AccountSection.vue";
import ProxiesSettingSection from "./ProxiesSettingSection.vue";
</script>

<template>
  <div class=":uno: rounded-b-base max-w-md bg-white p-4">
    <Suspense>
      <template #default>
        <AccountSection />
      </template>
      <template #fallback>
        <VLoading />
      </template>
    </Suspense>
    <Suspense>
      <template #default>
        <ProxiesSettingSection />
      </template>
    </Suspense>
  </div>
</template>
