<!-- Side-by-side comparison of content adapted for WeChat, Xiaohongshu, and Twitter. -->
<template>
  <n-grid cols="3" x-gap="12">
    <n-grid-item v-for="platform in platforms" :key="platform.key">
      <n-card :title="platform.label" size="small">
        <div v-if="results[platform.key]">
          <pre class="platform-content">{{ formatResult(results[platform.key]) }}</pre>
        </div>
        <n-empty v-else description="等待生成..." />
      </n-card>
    </n-grid-item>
  </n-grid>
</template>

<script setup lang="ts">
defineProps<{ results: Record<string, any> }>()

const platforms = [
  { key: 'wechat', label: '公众号' },
  { key: 'xiaohongshu', label: '小红书' },
  { key: 'twitter', label: '推特' },
]

function formatResult(r: any): string {
  try { return JSON.stringify(typeof r === 'string' ? JSON.parse(r) : r, null, 2) }
  catch { return String(r) }
}
</script>

<style scoped>
.platform-content { white-space: pre-wrap; background: #f8f8f8; padding: 12px; border-radius: 6px; max-height: 400px; overflow-y: auto; font-size: 12px; }
</style>
