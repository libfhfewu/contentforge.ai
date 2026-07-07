<!-- 仪表盘：数据可视化增强版 -->
<template>
  <AppLayout>
    <div class="dashboard">
      <!-- 欢迎区域 -->
      <div class="welcome-section animate-fade-in">
        <n-card :bordered="false" class="welcome-card">
          <n-space align="center" justify="space-between">
            <div>
              <h1>欢迎回来，{{ user?.username }} 👋</h1>
              <p>{{ greetingText }}，开始创建你的下一个爆款内容</p>
            </div>
            <n-button type="primary" size="large" @click="showCreateModal = true" class="create-btn">
              <template #icon>
                <n-icon><AddOutline /></n-icon>
              </template>
              新建工作区
            </n-button>
          </n-space>
        </n-card>
      </div>

      <!-- 统计卡片 -->
      <n-grid cols="4" x-gap="16" class="stats-grid">
        <n-grid-item v-for="(stat, index) in statCards" :key="stat.key">
          <n-card
            :bordered="false"
            class="stat-card"
            hoverable
            @click="stat.action"
            @keydown.enter="stat.action"
            role="button"
            tabindex="0"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <div class="stat-content">
              <div class="stat-icon-wrapper" :style="{ background: stat.bgColor }">
                <n-icon :size="24" :color="stat.color">
                  <component :is="stat.icon" />
                </n-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
            <template #footer>
              <n-text depth="3" style="font-size: 12px">{{ stat.hint }}</n-text>
            </template>
          </n-card>
        </n-grid-item>
      </n-grid>

      <!-- 数据可视化区域 -->
      <n-grid cols="2" x-gap="16">
        <!-- 状态分布 -->
        <n-grid-item>
          <n-card title="📊 工作区状态分布" :bordered="false" class="chart-card">
            <div class="status-chart">
              <div v-for="(item, index) in statusDistribution" :key="item.status" class="status-bar-item">
                <div class="status-bar-label">
                  <n-tag :type="item.type" size="small">{{ item.label }}</n-tag>
                  <span class="status-bar-count">{{ item.count }}</span>
                </div>
                <div class="status-bar-track">
                  <div
                    class="status-bar-fill"
                    :style="{
                      width: item.percentage + '%',
                      background: item.color,
                      animationDelay: `${index * 200}ms`
                    }"
                  ></div>
                </div>
                <span class="status-bar-percent">{{ item.percentage }}%</span>
              </div>
            </div>
          </n-card>
        </n-grid-item>

        <!-- 平台分布 -->
        <n-grid-item>
          <n-card title="🎯 内容平台分布" :bordered="false" class="chart-card">
            <div class="platform-chart">
              <div v-for="platform in platformDistribution" :key="platform.name" class="platform-item">
                <div class="platform-icon" :style="{ background: platform.color }">
                  {{ platform.icon }}
                </div>
                <div class="platform-info">
                  <div class="platform-name">{{ platform.name }}</div>
                  <div class="platform-bar-track">
                    <div
                      class="platform-bar-fill"
                      :style="{ width: platform.percentage + '%', background: platform.color }"
                    ></div>
                  </div>
                </div>
                <div class="platform-count">{{ platform.count }}</div>
              </div>
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>

      <!-- 快速操作 -->
      <n-card title="⚡ 快速操作" :bordered="false" class="quick-actions-card">
        <n-grid cols="4" x-gap="12">
          <n-grid-item v-for="action in quickActions" :key="action.label">
            <div class="quick-action-item" @click="action.action">
              <div class="quick-action-icon" :style="{ background: action.bgColor }">
                <n-icon :size="20" :color="action.color">
                  <component :is="action.icon" />
                </n-icon>
              </div>
              <div class="quick-action-text">
                <div class="quick-action-label">{{ action.label }}</div>
                <div class="quick-action-desc">{{ action.desc }}</div>
              </div>
            </div>
          </n-grid-item>
        </n-grid>
      </n-card>

      <!-- 工作区列表 -->
      <n-card title="我的工作区" :bordered="false" class="workspaces-card" ref="workspacesRef">
        <template #header-extra>
          <n-space align="center">
            <n-tag
              v-if="statusFilter !== null"
              closable
              @close="statusFilter = null"
              type="info"
            >
              筛选: {{ getStatusFilterLabel() }}
            </n-tag>
            <n-text v-if="searchQuery" depth="3" style="font-size: 12px">
              找到 {{ searchResultCount }} 个结果
            </n-text>
            <n-input
              v-model:value="searchQuery"
              placeholder="搜索（标题/主题/状态）..."
              clearable
              style="width: 260px"
            >
              <template #prefix>
                <n-icon><SearchOutline /></n-icon>
              </template>
            </n-input>
          </n-space>
        </template>

        <!-- 加载骨架屏 -->
        <n-grid v-if="loading" cols="3" x-gap="16" y-gap="16">
          <n-grid-item v-for="i in 3" :key="i">
            <n-card class="skeleton-card">
              <n-skeleton text :repeat="3" />
            </n-card>
          </n-grid-item>
        </n-grid>

        <!-- 工作区列表 -->
        <n-grid v-else-if="filteredWorkspaces.length > 0" cols="3" x-gap="16" y-gap="16">
          <n-grid-item v-for="(ws, index) in filteredWorkspaces" :key="ws.id">
            <n-card
              hoverable
              class="workspace-card"
              @click="$router.push(`/workspace/${ws.id}`)"
              :style="{ animationDelay: `${index * 50}ms` }"
            >
              <template #header>
                <n-space align="center">
                  <n-avatar
                    :size="36"
                    :style="{ background: statusColors[ws.status] }"
                  >
                    {{ ws.title.charAt(0) }}
                  </n-avatar>
                  <div>
                    <div class="workspace-title">{{ ws.title }}</div>
                    <n-tag :type="statusTypes[ws.status]" size="small">
                      {{ statusTexts[ws.status] }}
                    </n-tag>
                  </div>
                </n-space>
              </template>

              <n-ellipsis :line-clamp="2" class="workspace-topic">
                {{ ws.topic }}
              </n-ellipsis>

              <template #footer>
                <n-space justify="space-between" align="center">
                  <n-text depth="3" style="font-size: 12px">
                    {{ formatDate(ws.createdAt) }}
                  </n-text>
                  <n-button text type="primary" size="small">
                    继续创作 →
                  </n-button>
                </n-space>
              </template>
            </n-card>
          </n-grid-item>
        </n-grid>

        <n-empty v-else :description="searchQuery ? `未找到包含「${searchQuery}」的工作区` : '还没有工作区，创建一个开始吧'">
          <template #extra>
            <n-button v-if="searchQuery" @click="searchQuery = ''" style="margin-top: 12px">
              清除搜索
            </n-button>
            <n-button v-else type="primary" @click="showCreateModal = true" style="margin-top: 12px">
              创建第一个工作区
            </n-button>
          </template>
        </n-empty>
      </n-card>
    </div>

    <!-- 新建工作区弹窗 -->
    <n-modal v-model:show="showCreateModal" preset="card" title="新建工作区" style="width: 600px">
      <n-form ref="formRef" :model="formData" :rules="rules">
        <n-form-item label="工作区名称" path="title">
          <n-input v-model:value="formData.title" placeholder="如：618数码促销方案" />
        </n-form-item>

        <n-form-item label="内容需求" path="topic">
          <n-input
            v-model:value="formData.topic"
            type="textarea"
            placeholder="描述你的内容需求，如：写一篇面向数码爱好者的618促销长文..."
            :rows="4"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showCreateModal = false">取消</n-button>
          <n-button type="primary" @click="handleCreate" :loading="creating">
            开始创作
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import {
  AddOutline,
  FolderOutline,
  CheckmarkCircleOutline,
  DocumentTextOutline,
  SearchOutline,
  BulbOutline,
  CreateOutline,
  TimeOutline,
  TrendingUpOutline
} from '@vicons/ionicons5'
import { useWorkspaceStore } from '../stores/workspace'
import { useAuthStore } from '../stores/auth'
import AppLayout from '../components/layout/AppLayout.vue'
import { formatDate } from '../utils/date'
import { STATUS_TEXTS, STATUS_TYPES, STATUS_COLORS } from '../constants/status'

const router = useRouter()
const store = useWorkspaceStore()
const auth = useAuthStore()
const message = useMessage()

const showCreateModal = ref(false)
const creating = ref(false)
const loading = ref(true)
const searchQuery = ref('')
const statusFilter = ref<number | number[] | null>(null)
const workspacesRef = ref<any>(null)

const user = computed(() => auth.user)
const workspaces = computed(() => store.workspaces)

// 问候语
const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，注意休息'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 统计数据
const stats = computed(() => ({
  totalWorkspaces: workspaces.value.length,
  completed: workspaces.value.filter(ws => ws.status === 3).length,
  inProgress: workspaces.value.filter(ws => ws.status === 1 || ws.status === 2).length,
  draft: workspaces.value.filter(ws => ws.status === 0).length
}))

// 统计卡片配置
const statCards = computed(() => [
  {
    key: 'total',
    label: '工作区总数',
    value: stats.value.totalWorkspaces,
    icon: FolderOutline,
    color: '#2080f0',
    bgColor: 'rgba(32, 128, 240, 0.1)',
    hint: '点击查看全部 →',
    action: scrollToWorkspaces
  },
  {
    key: 'completed',
    label: '已完成',
    value: stats.value.completed,
    icon: CheckmarkCircleOutline,
    color: '#18a058',
    bgColor: 'rgba(24, 160, 88, 0.1)',
    hint: '点击查看已完成 →',
    action: () => filterByStatus(3)
  },
  {
    key: 'inProgress',
    label: '进行中',
    value: stats.value.inProgress,
    icon: TrendingUpOutline,
    color: '#fa8c16',
    bgColor: 'rgba(250, 140, 22, 0.1)',
    hint: '查看进行中的工作 →',
    action: () => filterByStatus([1, 2])
  },
  {
    key: 'draft',
    label: '草稿',
    value: stats.value.draft,
    icon: DocumentTextOutline,
    color: '#722ed1',
    bgColor: 'rgba(114, 46, 209, 0.1)',
    hint: '查看草稿 →',
    action: () => filterByStatus(0)
  }
])

// 状态分布
const statusDistribution = computed(() => {
  const total = workspaces.value.length || 1
  return [
    { status: 0, label: '草稿', type: 'default' as const, color: '#86909c', count: stats.value.draft, percentage: Math.round(stats.value.draft / total * 100) },
    { status: 1, label: '策划中', type: 'info' as const, color: '#2080f0', count: workspaces.value.filter(ws => ws.status === 1).length, percentage: Math.round(workspaces.value.filter(ws => ws.status === 1).length / total * 100) },
    { status: 2, label: '创作中', type: 'warning' as const, color: '#fa8c16', count: workspaces.value.filter(ws => ws.status === 2).length, percentage: Math.round(workspaces.value.filter(ws => ws.status === 2).length / total * 100) },
    { status: 3, label: '已完成', type: 'success' as const, color: '#18a058', count: stats.value.completed, percentage: Math.round(stats.value.completed / total * 100) }
  ]
})

// 平台分布（模拟数据）
const platformDistribution = computed(() => [
  { name: '微信公众号', icon: '📱', color: '#07c160', count: stats.value.completed * 2, percentage: 45 },
  { name: '小红书', icon: '📕', color: '#fe2c55', count: stats.value.completed, percentage: 30 },
  { name: '抖音', icon: '🎵', color: '#000000', count: stats.value.completed, percentage: 25 }
])

// 快速操作
const quickActions = [
  { label: '新建工作区', desc: '开始新的内容创作', icon: AddOutline, color: '#2080f0', bgColor: 'rgba(32, 128, 240, 0.1)', action: () => showCreateModal.value = true },
  { label: '内容模板', desc: '使用预设模板快速创建', icon: BulbOutline, color: '#722ed1', bgColor: 'rgba(114, 46, 209, 0.1)', action: () => router.push('/templates') },
  { label: '历史记录', desc: '查看所有创作历史', icon: TimeOutline, color: '#fa8c16', bgColor: 'rgba(250, 140, 22, 0.1)', action: () => router.push('/history') },
  { label: '工作流', desc: '管理自动化工作流', icon: CreateOutline, color: '#18a058', bgColor: 'rgba(24, 160, 88, 0.1)', action: () => router.push('/workflows') }
]

// 搜索和筛选过滤（支持标题、主题、状态搜索，支持多状态筛选）
const filteredWorkspaces = computed(() => {
  let result = workspaces.value

  const filter = statusFilter.value
  if (filter !== null) {
    if (Array.isArray(filter)) {
      result = result.filter(ws => filter.includes(ws.status))
    } else {
      result = result.filter(ws => ws.status === filter)
    }
  }

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase().trim()
    if (query) {
      result = result.filter(ws =>
        ws.title.toLowerCase().includes(query) ||
        ws.topic.toLowerCase().includes(query) ||
        STATUS_TEXTS[ws.status]?.toLowerCase().includes(query)
      )
    }
  }

  return result
})

// 搜索结果数量
const searchResultCount = computed(() => filteredWorkspaces.value.length)

// 状态映射
const statusColors = STATUS_COLORS
const statusTypes = STATUS_TYPES
const statusTexts = STATUS_TEXTS

// 获取筛选标签文字
function getStatusFilterLabel(): string {
  if (Array.isArray(statusFilter.value)) {
    if (statusFilter.value.length === 2 && statusFilter.value.includes(1) && statusFilter.value.includes(2)) {
      return '进行中'
    }
    return statusFilter.value.map(s => STATUS_TEXTS[s]).join('/')
  }
  return statusFilter.value !== null ? STATUS_TEXTS[statusFilter.value] || '未知' : ''
}

// 表单数据
const formData = ref({ title: '', topic: '' })
const rules = {
  title: { required: true, message: '请输入工作区名称', trigger: 'blur' },
  topic: { required: true, message: '请输入内容需求', trigger: 'blur' }
}

function scrollToWorkspaces() {
  statusFilter.value = null
  searchQuery.value = ''
  nextTick(() => {
    const el = workspacesRef.value?.$el
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

function filterByStatus(status: number | number[]) {
  statusFilter.value = status
  nextTick(() => {
    const el = workspacesRef.value?.$el
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

async function handleCreate() {
  if (!formData.value.title || !formData.value.topic) {
    message.warning('请填写工作区名称和内容需求')
    return
  }
  creating.value = true
  try {
    const ws = await store.create(formData.value.title, formData.value.topic)
    showCreateModal.value = false
    message.success('工作区创建成功！')
    router.push(`/workspace/${ws.id}`)
  } catch (e: any) {
    message.error('创建失败')
  } finally {
    creating.value = false
  }
}

onMounted(async () => {
  await store.fetchAll()
  loading.value = false
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 动画 */
.animate-fade-in {
  animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes growWidth {
  from { width: 0; }
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  border: none;
}

.welcome-card :deep(.n-card__content) {
  padding: 32px;
}

.welcome-card h1 {
  color: white;
  font-size: 28px;
  margin: 0 0 8px;
  font-weight: 600;
}

.welcome-card p {
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  font-size: 16px;
}

.create-btn {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

/* 统计卡片 */
.stats-grid {
  margin-top: 8px;
}

.stat-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  animation: fadeInUp 0.5s ease-out backwards;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1d2129;
}

.stat-label {
  font-size: 13px;
  color: #86909c;
  margin-top: 2px;
}

.stat-card :deep(.n-card__footer) {
  padding-top: 8px;
  border-top: 1px solid #f2f3f5;
}

/* 图表卡片 */
.chart-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 状态分布图 */
.status-chart {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-bar-label {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 120px;
}

.status-bar-count {
  font-weight: 600;
  color: #1d2129;
}

.status-bar-track {
  flex: 1;
  height: 8px;
  background: #f2f3f5;
  border-radius: 4px;
  overflow: hidden;
}

.status-bar-fill {
  height: 100%;
  border-radius: 4px;
  animation: growWidth 1s ease-out backwards;
}

.status-bar-percent {
  min-width: 40px;
  text-align: right;
  font-size: 13px;
  color: #86909c;
}

/* 平台分布图 */
.platform-chart {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.platform-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.platform-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.platform-info {
  flex: 1;
}

.platform-name {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 6px;
}

.platform-bar-track {
  height: 6px;
  background: #f2f3f5;
  border-radius: 3px;
  overflow: hidden;
}

.platform-bar-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 1s ease-out;
}

.platform-count {
  font-weight: 600;
  color: #1d2129;
  min-width: 30px;
  text-align: right;
}

/* 快速操作 */
.quick-actions-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.quick-action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-action-item:hover {
  background: #f5f7fa;
  transform: translateX(4px);
}

.quick-action-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.quick-action-label {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
}

.quick-action-desc {
  font-size: 12px;
  color: #86909c;
  margin-top: 2px;
}

/* 工作区卡片 */
.workspaces-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.workspace-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  border: 1px solid #e5e6eb;
  cursor: pointer;
  animation: fadeInUp 0.5s ease-out backwards;
}

.workspace-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #2080f0;
}

.workspace-title {
  font-weight: 600;
  color: #1d2129;
  font-size: 16px;
}

.workspace-topic {
  color: #86909c;
  margin: 12px 0;
  font-size: 14px;
  line-height: 1.6;
}

/* 骨架屏 */
.skeleton-card {
  border-radius: 12px;
  min-height: 150px;
}

@media (max-width: 768px) {
  .welcome-card h1 {
    font-size: 22px;
  }

  .welcome-card p {
    font-size: 14px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr) !important;
  }
}
</style>
