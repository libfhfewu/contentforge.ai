<!-- 页面布局容器：优化后的布局 -->
<template>
  <n-layout class="app-layout">
    <AppHeader />
    <n-layout-content class="app-content">
      <slot />
    </n-layout-content>
  </n-layout>
</template>

<script setup lang="ts">
import AppHeader from './AppHeader.vue'
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.app-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px;
}

@media (max-width: 768px) {
  .app-content {
    padding: 16px;
  }
}
</style>
