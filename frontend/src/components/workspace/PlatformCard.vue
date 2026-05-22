<!-- 平台优化师 Agent 卡片：触发三平台并发适配 -->
<template>
  <n-card title="📱 平台优化师" size="small">
    <n-spin :show="loading">
      <div v-if="hasResults">
        <PlatformCompare :results="results" />
      </div>
      <div v-else>
        <p style="color:#666">内容已确认，AI 平台优化师将并发适配到三个平台。</p>
        <n-button type="primary" @click="$emit('execute')" :loading="loading">开始适配</n-button>
      </div>
    </n-spin>
  </n-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PlatformCompare from './PlatformCompare.vue'

const props = defineProps<{ results: Record<string, any>; loading: boolean }>()
defineEmits<{ execute: [] }>()
const hasResults = computed(() => props.results && Object.keys(props.results).length > 0)
</script>
