<template>
  <n-card class="creator-card" :bordered="false">
    <template #header>
      <n-space align="center">
        <n-icon size="24" color="#722ed1">
          <CreateOutline />
        </n-icon>
        <div>
          <div class="card-title">步骤2：内容创作</div>
          <div class="card-subtitle">AI 撰写高质量内容</div>
        </div>
      </n-space>
    </template>

    <template #header-extra>
      <n-space align="center">
        <n-tag v-if="streaming" type="warning" size="small">
          <template #icon><n-spin :size="14" /></template>
          生成中 {{ wordCount }}字
        </n-tag>
        <n-tag v-else-if="result" type="success" size="small">已完成</n-tag>
        <n-tag v-if="reflectionScore" :type="reflectionScore > 0.7 ? 'success' : 'warning'" size="small">
          质量: {{ (reflectionScore * 100).toFixed(0) }}%
        </n-tag>
      </n-space>
    </template>

    <div class="card-body">
      <!-- 反思过程展示 -->
      <div v-if="showReflection && reflectionSteps.length > 0" class="reflection-trace">
        <n-collapse default-expanded-names="reflection">
          <n-collapse-item title="自我反思过程" name="reflection">
            <n-timeline size="small">
              <n-timeline-item
                v-for="(step, i) in reflectionSteps"
                :key="i"
                :type="step.type"
                :title="step.title"
                :content="step.content"
                :time="step.time"
              />
            </n-timeline>
          </n-collapse-item>
        </n-collapse>
      </div>

      <!-- 流式输出展示 -->
      <div v-if="streaming" class="streaming-container">
        <div class="streaming-header">
          <n-space align="center">
            <n-spin :size="16" />
            <span>AI 正在创作中...</span>
          </n-space>
          <n-text depth="3">{{ wordCount }} 字</n-text>
        </div>
        <div class="streaming-content" ref="contentRef">
          <div class="content-text">{{ result }}</div>
        </div>
        <div class="streaming-footer">
          <n-button @click="$emit('stop')">停止生成</n-button>
        </div>
      </div>

      <!-- 完成状态 -->
      <div v-else-if="result" class="completed-container">
        <n-tabs type="line" animated>
          <n-tab-pane name="preview" tab="预览">
            <div class="content-preview">{{ result }}</div>
          </n-tab-pane>
          <n-tab-pane name="edit" tab="编辑">
            <n-input v-model:value="editableContent" type="textarea" :rows="10" />
          </n-tab-pane>
        </n-tabs>

        <n-space style="margin-top: 16px">
          <n-button type="primary" @click="$emit('confirm')">
            <template #icon><n-icon><CheckmarkOutline /></n-icon></template>
            确认内容，适配平台
          </n-button>
          <n-button @click="$emit('retry')">
            <template #icon><n-icon><RefreshOutline /></n-icon></template>
            重新生成
          </n-button>
        </n-space>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <n-icon size="48" color="#722ed1"><CreateOutline /></n-icon>
        <h3>开始内容创作</h3>
        <p>策略已确认，AI 将根据策略撰写高质量内容</p>
        <n-button type="primary" size="large" @click="$emit('execute')" :loading="loading">
          生成内容
        </n-button>
      </div>
    </div>
  </n-card>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { CreateOutline, CheckmarkOutline, RefreshOutline } from '@vicons/ionicons5'

const props = defineProps<{
  result: string | null
  loading: boolean
  streaming: boolean
}>()

const emit = defineEmits(['execute', 'confirm', 'retry', 'stop'])

const contentRef = ref<HTMLElement>()
const editableContent = ref('')
const showReflection = ref(true)
const reflectionScore = ref<number | null>(null)
const reflectionSteps = ref<Array<{type: string, title: string, content: string, time?: string}>>([])

const wordCount = computed(() => {
  return props.result ? props.result.length : 0
})

// 监听result变化
watch(() => props.result, (newVal) => {
  if (newVal && !props.streaming) {
    // 内容生成完成，清空反思状态（真实反思由后端 ReflectionAgent 处理）
    reflectionSteps.value = []
    reflectionScore.value = null
  }
})
</script>

<style scoped>
.creator-card { margin-bottom: 16px; }
.card-title { font-weight: 600; font-size: 16px; }
.card-subtitle { font-size: 12px; color: #999; }
.reflection-trace { margin-bottom: 16px; }
.streaming-container { border: 1px solid #e0e0e0; border-radius: 8px; padding: 16px; }
.streaming-header { display: flex; justify-content: space-between; margin-bottom: 12px; }
.streaming-content { max-height: 400px; overflow-y: auto; }
.content-text { white-space: pre-wrap; line-height: 1.6; }
.content-preview { white-space: pre-wrap; line-height: 1.6; }
.empty-state { text-align: center; padding: 40px 0; }
</style>
