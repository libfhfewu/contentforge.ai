<template>
  <n-card class="strategy-card" :bordered="false">
    <template #header>
      <n-space align="center">
        <n-icon size="24" color="#2080f0">
          <BulbOutline />
        </n-icon>
        <div>
          <div class="card-title">步骤1：策略规划</div>
          <div class="card-subtitle">AI 分析主题并生成内容策略</div>
        </div>
      </n-space>
    </template>

    <template #header-extra>
      <n-space align="center">
        <n-tag v-if="loading || localLoading" type="info" size="small">
          <template #icon><n-spin :size="14" /></template>
          生成中
        </n-tag>
        <n-tag v-else-if="result" type="success" size="small">已完成</n-tag>
        <n-tag v-if="qualityScore" :type="qualityScore > 0.7 ? 'success' : 'warning'" size="small">
          质量: {{ (qualityScore * 100).toFixed(0) }}%
        </n-tag>
      </n-space>
    </template>

    <div class="card-body">
      <!-- 模式选择 -->
      <div v-if="!result && !loading && !localLoading" class="mode-selector">
        <n-radio-group v-model:value="mode" size="small">
          <n-space>
            <n-radio-button value="normal">普通模式</n-radio-button>
            <n-radio-button value="react">ReAct推理</n-radio-button>
            <n-radio-button value="plan">任务规划</n-radio-button>
          </n-space>
        </n-radio-group>
      </div>

      <!-- ReAct执行过程展示 -->
      <div v-if="mode === 'react' && reactSteps.length > 0" class="react-trace">
        <n-divider>推理过程</n-divider>
        <n-timeline>
          <n-timeline-item
            v-for="(step, index) in reactSteps"
            :key="index"
            :type="getStepType(step.type)"
            :title="getStepTitle(step.type)"
            :content="step.content"
            :time="formatTime(step.timestamp)"
          />
        </n-timeline>
      </div>

      <!-- 规划执行过程展示 -->
      <div v-if="mode === 'plan' && planSteps.length > 0" class="plan-trace">
        <n-divider>任务分解</n-divider>
        <n-steps :current="currentPlanStep" size="small" vertical>
          <n-step
            v-for="step in planSteps"
            :key="step.id"
            :title="step.description"
            :description="getPlanStepDescription(step)"
          />
        </n-steps>
      </div>

      <!-- 结果展示 -->
      <div v-if="result" class="result-section">
        <n-tabs type="line" animated>
          <n-tab-pane name="preview" tab="预览">
            <div class="strategy-preview">{{ result }}</div>
          </n-tab-pane>
          <n-tab-pane name="json" tab="JSON">
            <n-code :code="formattedJson" language="json" />
          </n-tab-pane>
        </n-tabs>

        <n-space style="margin-top: 16px">
          <n-button type="primary" @click="$emit('confirm')">
            <template #icon><n-icon><CheckmarkOutline /></n-icon></template>
            确认策略，开始创作
          </n-button>
          <n-button @click="handleRetry">
            <template #icon><n-icon><RefreshOutline /></n-icon></template>
            重新生成
          </n-button>
        </n-space>
      </div>

      <!-- 生成按钮 -->
      <div v-else-if="!loading && !localLoading" class="empty-state">
        <n-button type="primary" size="large" @click="handleExecute">
          {{ mode === 'react' ? '开始ReAct推理' : mode === 'plan' ? '开始任务规划' : '生成策略' }}
        </n-button>
      </div>
    </div>
  </n-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { BulbOutline, CheckmarkOutline, RefreshOutline } from '@vicons/ionicons5'
import { executeReAct, executeWithPlanning } from '@/api/agent'

const props = defineProps<{
  result: string | null
  loading: boolean
  topic?: string
}>()

const emit = defineEmits(['execute', 'confirm', 'retry', 'update:result'])

const mode = ref('normal')
const localLoading = ref(false)
const qualityScore = ref<number | null>(null)

// ReAct 步骤
const reactSteps = ref<Array<{type: string, content: string, timestamp: number}>>([])

// Plan 步骤
const planSteps = ref<Array<{id: string, description: string, status: string, result?: string}>>([])
const planId = ref<string | null>(null)

const currentPlanStep = computed(() => {
  const idx = planSteps.value.findIndex(s => s.status === 'RUNNING' || s.status === 'PENDING')
  return idx >= 0 ? idx + 1 : planSteps.value.length
})

const formattedJson = computed(() => {
  try {
    return JSON.stringify(JSON.parse(props.result || '{}'), null, 2)
  } catch {
    return props.result || ''
  }
})

function getStepType(type: string): 'info' | 'warning' | 'success' | 'default' {
  const map: Record<string, 'info' | 'warning' | 'success' | 'default'> = {
    THOUGHT: 'info',
    ACTION: 'warning',
    OBSERVATION: 'success'
  }
  return map[type] || 'default'
}

function getStepTitle(type: string): string {
  const map: Record<string, string> = {
    THOUGHT: '💭 思考',
    ACTION: '⚡ 行动',
    OBSERVATION: '👁️ 观察'
  }
  return map[type] || type
}

function getPlanStepDescription(step: {status: string, result?: string}): string {
  const statusMap: Record<string, string> = {
    PENDING: '等待执行',
    READY: '就绪',
    RUNNING: '执行中...',
    COMPLETED: '✅ 已完成',
    FAILED: '❌ 失败'
  }
  let desc = statusMap[step.status] || step.status
  if (step.result) {
    desc += ` - ${step.result.substring(0, 50)}...`
  }
  return desc
}

function formatTime(timestamp: number): string {
  return timestamp ? new Date(timestamp).toLocaleTimeString() : ''
}

async function handleExecute() {
  if (mode.value === 'normal') {
    emit('execute')
    return
  }

  localLoading.value = true
  reactSteps.value = []
  planSteps.value = []
  qualityScore.value = null

  const sessionId = 'workspace-' + Date.now()

  try {
    if (mode.value === 'react') {
      const { data } = await executeReAct(props.topic || '内容创作', sessionId)

      if (data.code === 200 && data.data) {
        // 新格式: ReActResult { steps[], finalAnswer, iterations }
        const result = data.data

        if (result.steps && Array.isArray(result.steps)) {
          reactSteps.value = result.steps.map((s: any) => ({
            type: s.type || s.stepType || 'THOUGHT',
            content: s.content || '',
            timestamp: s.timestamp || Date.now()
          }))
        }

        if (result.finalAnswer) {
          emit('update:result', result.finalAnswer)
        }
      }
    } else if (mode.value === 'plan') {
      const { data } = await executeWithPlanning(props.topic || '内容创作', sessionId)

      if (data.code === 200 && data.data) {
        // 新格式: TaskPlan { id, goal, steps[], status, ... }
        const plan = data.data

        planId.value = plan.id

        if (plan.steps && Array.isArray(plan.steps)) {
          planSteps.value = plan.steps.map((s: any) => ({
            id: s.id,
            description: s.description || s.name || '',
            status: s.status || 'PENDING',
            result: s.result
          }))
        }

        // 收集所有步骤结果作为最终结果
        const results = plan.steps
          .filter((s: any) => s.result)
          .map((s: any) => s.result)
          .join('\n\n')

        if (results) {
          emit('update:result', results)
        }
      }
    }
  } catch (e: any) {
    console.error('执行失败', e)
  } finally {
    localLoading.value = false
  }
}

function handleRetry() {
  reactSteps.value = []
  planSteps.value = []
  qualityScore.value = null
  planId.value = null
  emit('retry')
}
</script>

<style scoped>
.strategy-card { margin-bottom: 16px; }
.card-title { font-weight: 600; font-size: 16px; }
.card-subtitle { font-size: 12px; color: #999; }
.mode-selector { margin-bottom: 16px; }
.react-trace, .plan-trace { margin: 16px 0; max-height: 400px; overflow-y: auto; }
.strategy-preview { white-space: pre-wrap; line-height: 1.6; }
.empty-state { text-align: center; padding: 40px 0; }
</style>
