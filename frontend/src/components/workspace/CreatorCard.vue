<!-- Panel for generating, editing, and confirming AI-written content before platform adaptation. -->
<template>
  <n-card title="✍️ 内容创作者" size="small">
    <n-spin :show="loading">
      <div v-if="result">
        <pre class="result-json">{{ formatResult(result) }}</pre>
        <ContentEditor :content="result" style="margin-top:12px" />
        <n-space style="margin-top:12px">
          <n-button type="primary" @click="$emit('confirm')">确认内容，适配平台</n-button>
          <n-button @click="$emit('retry')">重新生成</n-button>
        </n-space>
      </div>
      <div v-else>
        <p style="color:#666">策略已确认，AI 内容创作者将根据策略撰写高质量内容。</p>
        <n-button type="primary" @click="$emit('execute')" :loading="loading">生成内容</n-button>
      </div>
    </n-spin>
  </n-card>
</template>

<script setup lang="ts">
import ContentEditor from './ContentEditor.vue'

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
