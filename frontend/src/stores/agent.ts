/** Agent 状态管理：3 步流程状态、策略/内容/平台结果、加载态 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as api from '../api/agent'
import type { ContentVersion } from '../types'

export const useAgentStore = defineStore('agent', () => {
  const strategyResult = ref<any>(null)
  const contentResult = ref<any>(null)
  const platformResults = ref<Record<string, any>>({})
  const currentStep = ref(0)
  const loading = ref(false)
  const outputs = ref<ContentVersion[]>([])

  async function runStrategy(workspaceId: number, topic: string, audience: string) {
    loading.value = true
    try {
      const res = await api.executeStrategy(workspaceId, topic, audience)
      strategyResult.value = res.data.data.output
      currentStep.value = 1
    } finally {
      loading.value = false
    }
  }

  async function runContent(workspaceId: number, strategy: string, contentType: string) {
    loading.value = true
    try {
      const res = await api.executeContent(workspaceId, strategy, contentType)
      contentResult.value = res.data.data.output
      currentStep.value = 2
    } finally {
      loading.value = false
    }
  }

  async function runPlatform(workspaceId: number, content: string) {
    loading.value = true
    try {
      const res = await api.executePlatform(workspaceId, content)
      platformResults.value = res.data.data.output
      currentStep.value = 3
    } finally {
      loading.value = false
    }
  }

  async function fetchOutputs(workspaceId: number) {
    const res = await api.getOutputs(workspaceId)
    outputs.value = res.data.data
  }

  function reset() {
    strategyResult.value = null
    contentResult.value = null
    platformResults.value = {}
    currentStep.value = 0
    loading.value = false
    outputs.value = []
  }

  return { strategyResult, contentResult, platformResults, currentStep, loading, outputs,
           runStrategy, runContent, runPlatform, fetchOutputs, reset }
})
