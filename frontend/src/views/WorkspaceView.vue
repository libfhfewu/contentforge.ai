<!-- 工作区核心页面 -->
<template>
  <AppLayout>
    <div v-if="workspace" class="workspace-view">
      <n-space vertical size="large">
        <n-page-header @back="() => $router.push('/dashboard')">
          <template #title>
            <n-space align="center">
              <n-icon size="20" color="#2080f0">
                <DocumentTextOutline />
              </n-icon>
              {{ workspace.title }}
            </n-space>
          </template>
          <template #subtitle>
            <n-tag :type="statusType(workspace.status)" size="small">
              {{ statusText(workspace.status) }}
            </n-tag>
            <n-tag v-if="agent.brandProfileId" type="success" size="small" style="margin-left: 8px">
              🎨 品牌风格已激活
            </n-tag>
          </template>
          <template #extra>
            <n-space>
              <n-popconfirm @positive-click="handleDelete">
                <template #trigger>
                  <n-button type="error" size="small" secondary>删除工作区</n-button>
                </template>
                确定删除这个工作区？此操作不可恢复。
              </n-popconfirm>
            </n-space>
          </template>
        </n-page-header>

        <ProgressSteps
          :step="agent.currentStep"
          :show-research="true"
          :show-multimodal="true"
        />

        <n-grid cols="1" x-gap="16">
          <n-grid-item>
            <n-space vertical size="medium">

              <!-- ═══════════════════════════════════════════ -->
              <!-- 🔧 准备阶段：品牌风格 + 竞品分析 + 知识库（可选） -->
              <!-- ═══════════════════════════════════════════ -->
              <n-collapse>
                <n-collapse-item>
                  <template #header>
                    <n-space align="center">
                      <span>🔧 准备阶段</span>
                      <n-tag v-if="agent.brandProfileId" type="success" size="tiny" closable @close="handleBrandDeselect">品牌已选</n-tag>
                      <n-tag v-if="hasResearchResult" type="info" size="tiny" closable @close="hasResearchResult = false">竞品已分析</n-tag>
                      <n-text depth="3" style="font-size: 12px">（可选，点击标签可取消）</n-text>
                    </n-space>
                  </template>
                  <n-tabs type="line" animated>
                    <n-tab-pane name="brand" tab="🎨 品牌风格">
                      <BrandProfilePanel @select-brand="handleBrandSelect" />
                    </n-tab-pane>
                    <n-tab-pane name="research" tab="🔍 竞品分析">
                      <ResearchPanel
                        :workspace-id="workspaceId"
                        @apply-strategy="handleResearchToStrategy"
                      />
                    </n-tab-pane>
                    <n-tab-pane name="knowledge" tab="📚 知识库">
                      <KnowledgePanel />
                    </n-tab-pane>
                  </n-tabs>
                </n-collapse-item>
              </n-collapse>

              <!-- ═══════════════════ -->
              <!-- ① 策略规划 -->
              <!-- ═══════════════════ -->
              <StrategyCard
                :result="agent.strategyResult"
                :loading="agent.loading"
                :topic="workspace.topic"
                @execute="handleStrategy"
                @confirm="handleStrategyConfirm"
                @retry="handleStrategy"
                @update:result="agent.strategyResult = $event"
              />

              <!-- ═══════════════════ -->
              <!-- ② 内容创作 -->
              <!-- ═══════════════════ -->
              <CreatorCard
                v-if="agent.currentStep >= 1"
                :result="agent.contentResult"
                :loading="agent.loading"
                :streaming="agent.loading && agent.currentStep === 1"
                @execute="handleContent"
                @confirm="handleContentConfirm"
                @retry="handleContent"
                @stop="handleStop"
              />

              <!-- 对话记忆（内联到内容创作下方） -->
              <n-collapse v-if="agent.currentStep >= 1" style="margin-top: 8px">
                <n-collapse-item title="💬 对话记忆" name="memory">
                  <MemoryPanel :session-id="sessionId" />
                </n-collapse-item>
              </n-collapse>

              <!-- ═══════════════════ -->
              <!-- ③ 平台适配 -->
              <!-- ═══════════════════ -->
              <PlatformCard
                v-if="agent.currentStep >= 2"
                :results="agent.platformResults"
                :loading="agent.loading"
                :title="contentTitle"
                :content="agent.contentResult"
                @execute="handlePlatform"
                @reviewed="handleReviewed"
              />

              <!-- 内容效果看板（平台适配后自动展示） -->
              <n-collapse v-if="agent.currentStep >= 3" style="margin-top: 8px">
                <n-collapse-item title="📊 内容效果追踪" name="performance">
                  <PerformancePanel
                    :workspace-id="workspaceId"
                    :current-title="contentTitle"
                    :current-content="agent.contentResult"
                    :brand-profile-id="agent.brandProfileId"
                  />
                </n-collapse-item>
              </n-collapse>

              <!-- ═══════════════════ -->
              <!-- ④ 多模态扩展 -->
              <!-- ═══════════════════ -->
              <n-collapse v-if="agent.currentStep >= 2" style="margin-top: 8px">
                <n-collapse-item>
                  <template #header>
                    <n-space align="center">
                      <span>🎨 多模态内容扩展</span>
                      <n-text depth="3" style="font-size: 12px">基于已生成内容，扩展图片/视频/播客</n-text>
                    </n-space>
                  </template>
                  <n-tabs type="line" animated>
                    <n-tab-pane name="image" tab="🖼️ 图片描述">
                      <ImageDescPanel :workspace-id="workspaceId" :default-topic="defaultMultimodalTopic" />
                    </n-tab-pane>
                    <n-tab-pane name="video" tab="🎬 视频脚本">
                      <VideoScriptPanel :workspace-id="workspaceId" :default-topic="defaultMultimodalTopic" />
                    </n-tab-pane>
                    <n-tab-pane name="podcast" tab="🎙️ 播客脚本">
                      <PodcastPanel :workspace-id="workspaceId" :default-topic="defaultMultimodalTopic" />
                    </n-tab-pane>
                  </n-tabs>
                </n-collapse-item>
              </n-collapse>

              <!-- ═══════════════════ -->
              <!-- 🛠️ 高级工具 -->
              <!-- ═══════════════════ -->
              <n-collapse>
                <n-collapse-item title="🛠️ 高级 Agent 工具" name="advanced">
                  <EnhancedAgentPanel :session-id="sessionId" />
                </n-collapse-item>
              </n-collapse>

            </n-space>
          </n-grid-item>
        </n-grid>
      </n-space>
    </div>
    <div v-else class="loading-container">
      <n-spin size="large" />
      <p class="loading-text">加载中...</p>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { DocumentTextOutline } from '@vicons/ionicons5'
import { getWorkspace, deleteWorkspace } from '../api/workspace'
import { getOutputs, addMemoryEntry } from '../api/agent'
import { useAgentStore } from '../stores/agent'
import AppLayout from '../components/layout/AppLayout.vue'
import ProgressSteps from '../components/workspace/ProgressSteps.vue'
import StrategyCard from '../components/workspace/StrategyCard.vue'
import CreatorCard from '../components/workspace/CreatorCard.vue'
import PlatformCard from '../components/workspace/PlatformCard.vue'
import MemoryPanel from '../components/agent/MemoryPanel.vue'
import EnhancedAgentPanel from '../components/agent/EnhancedAgentPanel.vue'
import ResearchPanel from '../components/agent/ResearchPanel.vue'
import ImageDescPanel from '../components/multimodal/ImageDescPanel.vue'
import VideoScriptPanel from '../components/multimodal/VideoScriptPanel.vue'
import PodcastPanel from '../components/multimodal/PodcastPanel.vue'
import PerformancePanel from '../components/performance/PerformancePanel.vue'
import BrandProfilePanel from '../components/brand/BrandProfilePanel.vue'
import KnowledgePanel from '../components/agent/KnowledgePanel.vue'
import type { Workspace, ApiResponse, ContentVersion } from '../types'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const agent = useAgentStore()
const workspace = ref<Workspace | null>(null)
const workspaceId = Number(route.params.id)
const sessionId = ref(`workspace-${workspaceId}`)
const hasResearchResult = ref(false)

// 从内容结果中提取标题
const contentTitle = computed(() => {
  if (agent.contentResult) {
    try {
      const parsed = JSON.parse(agent.contentResult)
      if (parsed.title) return parsed.title
    } catch {
      // 非 JSON 格式
    }
  }
  return workspace.value?.title || ''
})

// 多模态默认主题：优先用已生成内容的标题，否则用工作区主题
const defaultMultimodalTopic = computed(() => {
  if (agent.contentResult) {
    try {
      const parsed = JSON.parse(agent.contentResult)
      if (parsed.title) return parsed.title
    } catch {
      const firstLine = agent.contentResult.split('\n')[0]
      if (firstLine && firstLine.length > 5) return firstLine.substring(0, 50)
    }
  }
  if (workspace.value?.topic) return workspace.value.topic
  return ''
})

// 加载历史内容并恢复对话记忆
async function loadHistory() {
  try {
    const res = await getOutputs(workspaceId)
    const outputs = (res.data as ApiResponse<ContentVersion[]>).data

    if (outputs && outputs.length > 0) {
      const strategyOutput = outputs.find(o => o.platform === '通用' && o.title?.includes('策略'))
      const contentOutput = outputs.find(o => o.platform === '通用' && !o.title?.includes('策略'))
      const platformOutputs = outputs.filter(o => o.platform !== '通用')

      // 恢复对话记忆（静默失败，不影响主流程）
      const sid = sessionId.value
      try {
        if (workspace.value?.topic) {
          await addMemoryEntry(sid, 'user', `请为以下主题生成策略: ${workspace.value.topic}`)
        }
        if (strategyOutput) {
          agent.strategyResult = strategyOutput.body
          agent.currentStep = 1
          await addMemoryEntry(sid, 'assistant', `策略生成完成:\n${strategyOutput.body.substring(0, 500)}`)
        }
        if (contentOutput) {
          agent.contentResult = contentOutput.body
          agent.currentStep = 2
          await addMemoryEntry(sid, 'user', '请根据策略创作内容')
          await addMemoryEntry(sid, 'assistant', `内容创作完成，共${contentOutput.body.length}字`)
        }
        if (platformOutputs.length > 0) {
          const results: Record<string, string> = {}
          platformOutputs.forEach(o => {
            results[o.platform] = o.body
          })
          agent.platformResults = results
          agent.currentStep = 3
          await addMemoryEntry(sid, 'user', '开始平台适配')
          await addMemoryEntry(sid, 'assistant', `平台适配完成: ${platformOutputs.map(o => o.platform).join(', ')}`)
        }
      } catch (memErr) {
        console.warn('恢复对话记忆失败（不影响使用）:', memErr)
      }
    }
  } catch (err) {
    console.error('加载历史内容失败:', err)
  }
}

onMounted(async () => {
  try {
    const res = await getWorkspace(workspaceId)
    workspace.value = res.data.data
    await loadHistory()
  } catch {
    message.error('工作区不存在')
    router.push('/dashboard')
  }
})

onUnmounted(() => agent.reset())

// 步骤1：执行策略规划
async function handleStrategy() {
  if (!workspace.value) return
  await agent.runStrategy(workspaceId, workspace.value.topic, 'general')
}

// 步骤1完成：确认策略后自动开始内容创作
async function handleStrategyConfirm() {
  message.success('策略已确认，开始内容创作')
  await nextTick()
  handleContent()
}

// 步骤2：执行内容创作
async function handleContent() {
  const strategy = typeof agent.strategyResult === 'string'
    ? agent.strategyResult : JSON.stringify(agent.strategyResult)
  try {
    await agent.runContentStream(workspaceId, strategy, 'long')
  } catch {
    message.error('内容生成失败，请稍后重试')
  }
}

// 步骤2完成：确认内容后自动开始平台适配
async function handleContentConfirm() {
  message.success('内容已确认，开始平台适配')
  await nextTick()
  handlePlatform()
}

// 停止生成
function handleStop() {
  message.info('已停止生成')
}

// 步骤3：执行平台适配
async function handlePlatform() {
  const content = typeof agent.contentResult === 'string'
    ? agent.contentResult : JSON.stringify(agent.contentResult)
  await agent.runPlatform(workspaceId, content)
}

// 处理审核结果
function handleReviewed(status: string) {
  if (status === 'approved') {
    message.success('内容已批准，可以发布了！')
  } else {
    message.info('已标记需要修改，请根据反馈调整内容')
  }
}

// 删除工作区
async function handleDelete() {
  try {
    await deleteWorkspace(workspaceId)
    message.success('工作区已删除')
    router.push('/dashboard')
  } catch (e) {
    message.error('删除失败')
  }
}

// 品牌风格选择
function handleBrandSelect(brandId: number) {
  agent.brandProfileId = brandId
  message.success('品牌风格已激活，后续策略和创作将自动匹配该风格')
}

// 取消品牌风格
function handleBrandDeselect() {
  agent.brandProfileId = null
  message.info('品牌风格已取消')
}

// 竞品分析结果应用到策略
function handleResearchToStrategy(result: any) {
  if (result) {
    hasResearchResult.value = true
    const strategyContext = JSON.stringify(result, null, 2)
    agent.strategyResult = `基于竞品分析的策略参考：\n${strategyContext}`
    message.success('竞品分析结果已应用到策略，可以开始生成策略')
  }
}

function statusText(s: number) {
  const map: Record<number, string> = { 0: '草稿', 1: '规划中', 2: '创作中', 3: '已完成' }
  return map[s] || '草稿'
}

function statusType(s: number): 'default' | 'info' | 'warning' | 'success' {
  const map: Record<number, 'default' | 'info' | 'warning' | 'success'> = {
    0: 'default', 1: 'info', 2: 'warning', 3: 'success'
  }
  return map[s] || 'default'
}
</script>

<style scoped>
.workspace-view {
  padding: 16px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.loading-text {
  margin-top: 16px;
  color: #999;
}
</style>
