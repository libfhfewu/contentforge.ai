<template>
  <n-card class="platform-card" :bordered="false">
    <template #header>
      <n-space align="center">
        <n-icon size="24" color="#fa8c16">
          <ShareSocialOutline />
        </n-icon>
        <div>
          <div class="card-title">步骤3：平台适配 & 发布</div>
          <div class="card-subtitle">将内容适配到不同平台并发布</div>
        </div>
      </n-space>
    </template>

    <template #header-extra>
      <n-space align="center">
        <n-tag v-if="loading" type="info" size="small">
          <template #icon><n-spin :size="14" /></template>
          适配中
        </n-tag>
        <n-tag v-else-if="hasResults" type="success" size="small">已完成</n-tag>
      </n-space>
    </template>

    <div class="card-body">
      <!-- 适配结果 -->
      <div v-if="hasResults" class="results-container">
        <n-tabs v-model:value="activePlatform" type="line" animated>
          <n-tab-pane v-for="(content, platform) in results" :key="platform" :name="platform" :tab="getPlatformName(platform)">
            <div class="platform-content">
              <n-code :code="formatContent(content)" language="json" />
            </div>
          </n-tab-pane>
        </n-tabs>

        <!-- 人工审核区域 -->
        <div class="review-section">
          <n-divider>人工审核</n-divider>
          
          <div v-if="showReviewInput" style="margin-bottom: 16px">
            <n-input 
              v-model:value="reviewFeedback" 
              type="textarea" 
              placeholder="请输入修改意见..."
              :rows="3"
            />
            <n-space style="margin-top: 8px">
              <n-button type="primary" size="small" @click="submitFeedback">提交反馈</n-button>
              <n-button size="small" @click="showReviewInput = false">取消</n-button>
            </n-space>
          </div>

          <n-space>
            <n-button type="success" @click="approve">
              <template #icon><n-icon><CheckmarkOutline /></n-icon></template>
              批准发布
            </n-button>
            <n-button type="warning" @click="showReviewInput = true">
              <template #icon><n-icon><CreateOutline /></n-icon></template>
              需要修改
            </n-button>
            <n-button @click="$emit('execute')">
              <template #icon><n-icon><RefreshOutline /></n-icon></template>
              重新适配
            </n-button>
          </n-space>
        </div>

        <!-- 一键发布面板 -->
        <PublishPanel 
          v-if="reviewStatus === 'approved'"
          :title="title ?? ''"
          :content="mainContent ?? ''"
          :cover-url="coverUrl"
        />
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading" class="empty-state">
        <n-icon size="48" color="#fa8c16"><ShareSocialOutline /></n-icon>
        <h3>开始平台适配</h3>
        <p>内容已确认，AI 将适配到各平台风格</p>
        <n-button type="primary" size="large" @click="$emit('execute')" :loading="loading">
          开始适配
        </n-button>
      </div>
    </div>
  </n-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ShareSocialOutline, CheckmarkOutline, CreateOutline, RefreshOutline } from '@vicons/ionicons5'
import { useMessage } from 'naive-ui'
import PublishPanel from './PublishPanel.vue'
import type { PlatformResults } from '../../types'

const message = useMessage()

const props = defineProps<{
  results: PlatformResults | null
  loading: boolean
  title?: string
  content?: string
}>()

const emit = defineEmits(['execute', 'reviewed'])

const activePlatform = ref('wechat')
const reviewStatus = ref<string | null>(null)
const showReviewInput = ref(false)
const reviewFeedback = ref('')

const hasResults = computed(() => {
  return props.results && Object.keys(props.results).length > 0
})

// 获取主要内容（用于发布）
const mainContent = computed(() => {
  if (!props.content) return ''
  try {
    const parsed = JSON.parse(props.content)
    return parsed.body || props.content
  } catch {
    return props.content
  }
})

const coverUrl = ref('')

function getPlatformName(platform: string) {
  const names: Record<string, string> = {
    wechat: '公众号',
    xiaohongshu: '小红书',
    douyin: '抖音'
  }
  return names[platform] || platform
}

// 格式化内容为字符串（处理对象和字符串）
function formatContent(content: any): string {
  if (typeof content === 'string') {
    try {
      const parsed = JSON.parse(content)
      return JSON.stringify(parsed, null, 2)
    } catch {
      return content
    }
  }
  return JSON.stringify(content, null, 2)
}

async function approve() {
  try {
    reviewStatus.value = 'approved'
    message.success('内容已批准！现在可以一键发布了')
    emit('reviewed', 'approved')
  } catch (e) {
    console.error('批准失败', e)
  }
}

async function submitFeedback() {
  if (!reviewFeedback.value.trim()) {
    message.warning('请输入修改意见')
    return
  }
  
  reviewStatus.value = 'revision'
  showReviewInput.value = false
  message.info('已提交修改意见')
  emit('reviewed', 'revision')
}
</script>

<style scoped>
.platform-card { margin-bottom: 16px; }
.card-title { font-weight: 600; font-size: 16px; }
.card-subtitle { font-size: 12px; color: #999; }
.results-container { margin-top: 16px; }
.platform-content { padding: 16px; background: #f5f5f5; border-radius: 8px; }
.review-section { margin-top: 24px; padding-top: 16px; border-top: 1px solid #e0e0e0; }
.empty-state { text-align: center; padding: 40px 0; }
</style>
