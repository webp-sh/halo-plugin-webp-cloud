<script lang="ts" setup>
import AccountSection from "./AccountSection.vue";
import ProxiesSettingSection from "./ProxiesSettingSection.vue";
</script>

<template>
  <div class="webp-se-bg-white webp-se-p-4 rounded-b-base webp-se-max-w-md">
    <div class="webp-se-divide-y webp-se-divide-gray-50">
      <div class="webp-se-pb-3">
        <AccountSection />
      </div>
      <div class="webp-se-pt-3">
        <ProxiesSettingSection />
      </div>
    </div>
  </div>
</template>
