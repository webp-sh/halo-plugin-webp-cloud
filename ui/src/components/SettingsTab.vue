<script lang="ts" setup>
import AccountSection from "./AccountSection.vue";
import ProxiesSettingSection from "./ProxiesSettingSection.vue";
</script>

<template>
  <div class="bg-white p-4 rounded-b-base max-w-md">
    <div class="divide-y divide-gray-50">
      <div class="pb-3">
        <AccountSection />
      </div>
      <div class="pt-3">
        <ProxiesSettingSection />
      </div>
    </div>
  </div>
</template>
