<!-- 策略规划师 Agent 卡片：触发策略生成、展示结果、确认/重试 -->
<template>
  <n-card title="🤖 策略规划师" size="small">
    <n-spin :show="loading">
      <div v-if="result">
        <pre class="result-json">{{ formatResult(result) }}</pre>
        <n-space style="margin-top:12px">
          <n-button type="primary" @click="$emit('confirm')">确认策略，继续创作</n-button>
          <n-button @click="$emit('retry')">重新生成</n-button>
        </n-space>
      </div>
      <div v-else>
        <p style="color:#666">基于你的主题，AI 策略规划师将分析热点趋势并生成内容策略方案。</p>
        <n-button type="primary" @click="$emit('execute')" :loading="loading">开始策划</n-button>
      </div>
    </n-spin>
  </n-card>
</template>

<script setup lang="ts">
defineProps<{ result: any; loading: boolean }>()
defineEmits<{ execute: []; confirm: []; retry: [] }>()

function formatResult(r: any): string {
  try { return JSON.stringify(typeof r === 'string' ? JSON.parse(r) : r, null, 2) }
  catch { return String(r) }
}
</script>

<style scoped>
.result-json { white-space: pre-wrap; background: #f8f8f8; padding: 12px; border-radius: 6px; max-height: 300px; overflow-y: auto; font-size: 13px; }
</style>
