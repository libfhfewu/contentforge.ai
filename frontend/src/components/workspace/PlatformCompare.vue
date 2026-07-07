<!-- 三栏平台对比视图：优化后的设计 -->
<template>
  <n-grid cols="3" x-gap="16">
    <n-grid-item v-for="platform in platforms" :key="platform.key">
      <n-card :title="platform.label" size="small" class="compare-card">
        <div v-if="results[platform.key]" class="platform-content">
          <div class="content-header">
            <n-avatar :size="24" :style="{ background: platformColors[platform.key] }" :aria-label="platform.label">
              {{ platform.label.charAt(0) }}
            </n-avatar>
            <n-text depth="3" style="font-size: 12px">{{ platformDescriptions[platform.key] }}</n-text>
          </div>
          <div class="content-body">
            {{ formatResult(results[platform.key]) }}
          </div>
          <div class="content-footer">
            <n-button size="tiny" @click="copyContent(platform.key)" aria-label="复制内容">
              复制
            </n-button>
          </div>
        </div>
        <n-empty v-else description="等待生成..." />
      </n-card>
    </n-grid-item>
  </n-grid>
</template>

<script setup lang="ts">
import { useMessage } from 'naive-ui'
import type { PlatformResults } from '../../types'

const props = defineProps<{ results: PlatformResults }>()
const message = useMessage()

const platforms = [
  { key: 'wechat' as const, label: '公众号' },
  { key: 'xiaohongshu' as const, label: '小红书' },
  { key: 'douyin' as const, label: '抖音' },
]

const platformColors: Record<string, string> = {
  wechat: '#07c160',
  xiaohongshu: '#fe2c55',
  douyin: '#000000'
}

const platformDescriptions: Record<string, string> = {
  wechat: '长文深度内容',
  xiaohongshu: '图文笔记',
  douyin: '短视频脚本'
}

function formatResult(r: string | object | undefined): string {
  if (!r) return ''
  try {
    const parsed = typeof r === 'string' ? JSON.parse(r) : r
    if (typeof parsed === 'object') {
      if (parsed.body) return parsed.body
      if (parsed.content) return parsed.content
      if (parsed.text) return parsed.text
      return JSON.stringify(parsed, null, 2)
    }
    return String(parsed)
  } catch {
    return String(r)
  }
}

async function copyContent(platformKey: keyof PlatformResults) {
  const content = props.results[platformKey]
  if (content) {
    try {
      await navigator.clipboard.writeText(formatResult(content))
      message.success('已复制到剪贴板')
    } catch {
      message.error('复制失败，请手动复制')
    }
  }
}
</script>

<style scoped>
.compare-card {
  height: 100%;
  transition: all 0.2s ease;
}

.compare-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.platform-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f2f3f5;
}

.content-body {
  max-height: 300px;
  overflow-y: auto;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  color: #1d2129;
}

.content-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
  border-top: 1px solid #f2f3f5;
}
</style>
