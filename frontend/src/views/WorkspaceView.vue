<!-- 工作台核心页面：3 步 Agent 交互流程（策略→创作→平台适配） -->
<template>
  <AppLayout>
    <div v-if="workspace">
      <n-space vertical size="large">
        <n-page-header @back="() => $router.push('/dashboard')">
          <template #title>{{ workspace.title }}</template>
          <template #subtitle>{{ statusText(workspace.status) }}</template>
        </n-page-header>

        <ProgressSteps :step="agent.currentStep" />

        <n-grid cols="1" x-gap="16">
          <n-grid-item>
            <n-space vertical size="medium">
              <StrategyCard
                :result="agent.strategyResult"
                :loading="agent.loading"
                @execute="handleStrategy"
                @confirm="handleStrategyConfirm"
                @retry="handleStrategy"
              />

              <CreatorCard
                v-if="agent.currentStep >= 1"
                :result="agent.contentResult"
                :loading="agent.loading"
                @execute="handleContent"
                @confirm="handleContentConfirm"
                @retry="handleContent"
              />

              <PlatformCard
                v-if="agent.currentStep >= 2"
                :results="agent.platformResults"
                :loading="agent.loading"
                @execute="handlePlatform"
              />
            </n-space>
          </n-grid-item>
        </n-grid>
      </n-space>
    </div>
    <n-spin v-else size="large" />
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { getWorkspace } from '../api/workspace'
import { useAgentStore } from '../stores/agent'
import AppLayout from '../components/layout/AppLayout.vue'
import ProgressSteps from '../components/workspace/ProgressSteps.vue'
import StrategyCard from '../components/workspace/StrategyCard.vue'
import CreatorCard from '../components/workspace/CreatorCard.vue'
import PlatformCard from '../components/workspace/PlatformCard.vue'
import type { Workspace } from '../types'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const agent = useAgentStore()
const workspace = ref<Workspace | null>(null)
const workspaceId = Number(route.params.id)

onMounted(async () => {
  try {
    const res = await getWorkspace(workspaceId)
    workspace.value = res.data.data
  } catch {
    message.error('工作区不存在')
    router.push('/dashboard')
  }
})

onUnmounted(() => agent.reset())

async function handleStrategy() {
  if (!workspace.value) return
  await agent.runStrategy(workspaceId, workspace.value.topic, 'general')
}

function handleStrategyConfirm() {
  message.success('策略已确认，开始内容创作')
}

async function handleContent() {
  const strategy = typeof agent.strategyResult === 'string'
    ? agent.strategyResult : JSON.stringify(agent.strategyResult)
  await agent.runContent(workspaceId, strategy, 'long')
}

function handleContentConfirm() {
  message.success('内容已确认，开始平台适配')
}

async function handlePlatform() {
  const content = typeof agent.contentResult === 'string'
    ? agent.contentResult : JSON.stringify(agent.contentResult)
  await agent.runPlatform(workspaceId, content)
}

function statusText(s: number) {
  return ['草稿', '策划中', '创作中', '已完成'][s] || '草稿'
}
</script>
