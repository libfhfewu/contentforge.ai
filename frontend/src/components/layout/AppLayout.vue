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
.app-layout { min-height: 100vh; background: #f5f5f5; }
.app-content { max-width: 1200px; margin: 0 auto; padding: 24px; }
</style>
