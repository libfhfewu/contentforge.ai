<!-- 进度指示器：支持可选步骤 -->
<template>
  <div class="progress-steps">
    <div
      v-for="(stepItem, index) in visibleSteps"
      :key="index"
      class="step-item"
      :class="{
        'step-completed': index < adjustedStep,
        'step-active': index === adjustedStep,
        'step-pending': index > adjustedStep,
        'step-optional': stepItem.optional
      }"
    >
      <div class="step-indicator">
        <n-icon v-if="index < adjustedStep" size="20" color="#fff">
          <CheckmarkOutline />
        </n-icon>
        <n-spin v-else-if="index === adjustedStep" :size="16" />
        <span v-else>{{ index + 1 }}</span>
      </div>

      <div class="step-content">
        <div class="step-title">
          {{ stepItem.title }}
          <n-tag v-if="stepItem.optional" size="tiny" type="default">可选</n-tag>
        </div>
        <div class="step-description">{{ stepItem.description }}</div>
      </div>

      <div v-if="index < visibleSteps.length - 1" class="step-connector">
        <div class="connector-line"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { CheckmarkOutline } from '@vicons/ionicons5'

const props = defineProps<{
  step: number
  showResearch?: boolean
  showMultimodal?: boolean
}>()

interface StepItem {
  title: string
  description: string
  optional?: boolean
  stepValue: number
}

const allSteps: StepItem[] = [
  { title: '策略规划', description: 'AI 分析选题策略', stepValue: 0 },
  { title: '内容创作', description: 'AI 撰写内容', stepValue: 1 },
  { title: '平台适配', description: '适配三个平台', stepValue: 2 },
  { title: '内容效果', description: '追踪发布数据', stepValue: 3, optional: true },
  { title: '多模态', description: '图片/视频/播客', stepValue: 2, optional: true }
]

const visibleSteps = computed(() => {
  return allSteps.filter(s => {
    if (s.title === '内容效果' && !props.showMultimodal) return false
    if (s.title === '多模态' && !props.showMultimodal) return false
    return true
  })
})

// 将 currentStep (0-3) 映射到可见步骤的索引
const adjustedStep = computed(() => {
  for (let i = visibleSteps.value.length - 1; i >= 0; i--) {
    if (props.step >= visibleSteps.value[i].stepValue) {
      return i
    }
  }
  return 0
})
</script>

<style scoped>
.progress-steps {
  display: flex;
  align-items: flex-start;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.step-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.step-indicator {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.step-completed .step-indicator {
  background: #18a058;
  color: white;
}

.step-active .step-indicator {
  background: #2080f0;
  color: white;
  box-shadow: 0 0 0 4px rgba(32, 128, 240, 0.2);
}

.step-pending .step-indicator {
  background: #f2f3f5;
  color: #86909c;
}

.step-optional .step-indicator {
  border: 2px dashed #c9cdd4;
  background: transparent;
}

.step-content {
  text-align: center;
}

.step-title {
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.step-description {
  font-size: 12px;
  color: #86909c;
}

.step-connector {
  position: absolute;
  top: 20px;
  left: 60%;
  right: -40%;
  height: 2px;
  background: #e5e6eb;
}

.step-completed + .step-connector {
  background: #18a058;
}

@media (max-width: 768px) {
  .progress-steps {
    padding: 16px;
  }

  .step-indicator {
    width: 32px;
    height: 32px;
    font-size: 14px;
  }

  .step-title {
    font-size: 12px;
  }

  .step-description {
    font-size: 10px;
  }
}
</style>
