<!-- 内容编辑器：优化后的设计 -->
<template>
  <n-card class="editor-card" :bordered="false">
    <template #header>
      <n-space align="center">
        <n-icon size="20" color="#722ed1">
          <CreateOutline />
        </n-icon>
        <span>编辑内容</span>
      </n-space>
    </template>
    
    <n-form label-placement="top">
      <n-form-item label="标题">
        <n-input 
          v-model:value="editTitle" 
          placeholder="请输入标题"
          size="large"
        />
      </n-form-item>
      
      <n-form-item label="正文内容">
        <n-input 
          v-model:value="editBody" 
          type="textarea" 
          :rows="12"
          placeholder="请输入正文内容（支持 Markdown 格式）"
          show-count
          :maxlength="10000"
        />
      </n-form-item>
    </n-form>
  </n-card>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { CreateOutline } from '@vicons/ionicons5'
import type { ContentResult } from '../../types'

const props = defineProps<{ content: string }>()

const editTitle = ref('')
const editBody = ref('')

watch(() => props.content, (c) => {
  if (c) {
    const parsed = tryParse(c)
    editTitle.value = parsed?.title || ''
    editBody.value = parsed?.body || c
  }
}, { immediate: true })

function tryParse(s: string): ContentResult | null {
  try { return JSON.parse(s) as ContentResult } catch { return null }
}
</script>

<style scoped>
.editor-card {
  border-radius: 8px;
  border: 1px solid #e5e6eb;
}
</style>
