/** Agent 状态管理：3 步流程状态、策略/内容/平台结果、加载态 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as api from '../api/agent'
import type { ContentVersion, PlatformResults, ApiResponse, AgentResponse } from '../types'

export const useAgentStore = defineStore('agent', () => {
  const strategyResult = ref<string | null>(null)
  const contentResult = ref<string>('')
  const platformResults = ref<PlatformResults>({})
  const currentStep = ref(0)
  const loading = ref(false)
  const outputs = ref<ContentVersion[]>([])
  const brandProfileId = ref<number | null>(null)

  async function runStrategy(workspaceId: number, topic: string, audience: string) {
    loading.value = true
    try {
      const res = await api.executeStrategy(workspaceId, topic, audience, brandProfileId.value || undefined)
      const data = res.data as ApiResponse<AgentResponse>
      strategyResult.value = typeof data.data.output === 'string' ? data.data.output : JSON.stringify(data.data.output)
      currentStep.value = 1
    } finally {
      loading.value = false
    }
  }

  async function runContent(workspaceId: number, strategy: string, contentType: string) {
    loading.value = true
    try {
      const res = await api.executeContent(workspaceId, strategy, contentType, brandProfileId.value || undefined)
      const data = res.data as ApiResponse<AgentResponse>
      contentResult.value = typeof data.data.output === 'string' ? data.data.output : JSON.stringify(data.data.output)
      currentStep.value = 2
    } finally {
      loading.value = false
    }
  }

  function runContentStream(workspaceId: number, strategy: string, contentType: string): Promise<void> {
    loading.value = true
    contentResult.value = ''

    return new Promise<void>((resolve, reject) => {
      const source = new EventSource(api.contentStreamUrl(workspaceId, strategy, contentType, brandProfileId.value || undefined), {
        withCredentials: true,
      })

      source.onmessage = (event) => {
        try {
          const payload = JSON.parse(event.data)
          contentResult.value += payload.chunk ?? ''
        } catch {
          contentResult.value += event.data
        }
      }

      source.onerror = () => {
        source.close()
        loading.value = false
        reject(new Error('内容流式生成失败'))
      }

      source.addEventListener('done', () => {
        source.close()
        currentStep.value = 2
        loading.value = false
        resolve()
      })
    })
  }

  async function runPlatform(workspaceId: number, content: string) {
    loading.value = true
    try {
      const res = await api.executePlatform(workspaceId, content)
      const data = res.data as ApiResponse<AgentResponse>
      platformResults.value = typeof data.data.output === 'string'
        ? JSON.parse(data.data.output) as PlatformResults
        : data.data.output as PlatformResults
      currentStep.value = 3
    } finally {
      loading.value = false
    }
  }

  async function fetchOutputs(workspaceId: number) {
    const res = await api.getOutputs(workspaceId)
    outputs.value = (res.data as ApiResponse<ContentVersion[]>).data
  }

  function reset() {
    strategyResult.value = null
    contentResult.value = ''
    platformResults.value = {}
    currentStep.value = 0
    loading.value = false
    outputs.value = []
    brandProfileId.value = null
  }

  return {
    strategyResult, contentResult, platformResults, currentStep, loading, outputs, brandProfileId,
    runStrategy, runContent, runContentStream, runPlatform, fetchOutputs, reset
  }
})
