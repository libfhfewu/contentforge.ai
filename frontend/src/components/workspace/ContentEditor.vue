<!-- 内容编辑器：标题 + Markdown 正文实时编辑 -->
<template>
  <n-card title="编辑内容" size="small">
    <n-form-item label="标题">
      <n-input v-model:value="editTitle" />
    </n-form-item>
    <n-form-item label="正文（Markdown）">
      <n-input v-model:value="editBody" type="textarea" :rows="10" />
    </n-form-item>
  </n-card>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{ content: any }>()

const editTitle = ref('')
const editBody = ref('')

watch(() => props.content, (c) => {
  if (c) {
    const parsed = typeof c === 'string' ? tryParse(c) : c
    editTitle.value = parsed?.title || ''
    editBody.value = parsed?.body || ''
  }
}, { immediate: true })

function tryParse(s: string) {
  try { return JSON.parse(s) } catch { return {} }
}
</script>
