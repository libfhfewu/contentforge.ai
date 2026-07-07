/** Agent API: 策略/内容/平台执行、SSE 流式、输出管理、ReAct、Planning、Memory、Review */
import client from './client'

// ============ 原有 API ============

export function executeStrategy(workspaceId: number, topic: string, audience: string, brandProfileId?: number) {
  return client.post(`/workspaces/${workspaceId}/strategy/execute`, { topic, audience, brandProfileId })
}

export function executeContent(workspaceId: number, strategy: string, contentType: string, brandProfileId?: number) {
  return client.post(`/workspaces/${workspaceId}/content/execute`, { strategy, contentType, brandProfileId })
}

export function contentStreamUrl(workspaceId: number, strategy: string, contentType: string, brandProfileId?: number) {
  const params = new URLSearchParams({ strategy, contentType })
  if (brandProfileId) params.append('brandProfileId', String(brandProfileId))
  return `/api/workspaces/${workspaceId}/content/stream?${params.toString()}`
}

export function executePlatform(workspaceId: number, content: string) {
  return client.post(`/workspaces/${workspaceId}/platform/execute`, { content })
}

export function getOutputs(workspaceId: number) {
  return client.get(`/workspaces/${workspaceId}/outputs`)
}

export function updateOutput(workspaceId: number, outputId: number, title: string, body: string) {
  return client.put(`/workspaces/${workspaceId}/outputs/${outputId}`, { title, body })
}

export function getAgentLogs(workspaceId: number) {
  return client.get(`/workspaces/${workspaceId}/agent-logs`)
}

// ============ 新增 Agent 能力 API ============

/** ReAct 模式执行任务 */
export function executeReAct(task: string, sessionId?: string, userId?: string) {
  return client.post('/agent/react', { task, sessionId: sessionId || 'default', userId: userId || 'anonymous' })
}

/** 规划模式执行任务 */
export function executeWithPlanning(task: string, sessionId?: string, userId?: string) {
  return client.post('/agent/plan', { task, sessionId: sessionId || 'default', userId: userId || 'anonymous' })
}

/** 创建任务计划 */
export function createPlan(task: string, sessionId?: string, userId?: string) {
  return client.post('/agent/plan/create', { task, sessionId: sessionId || 'default', userId: userId || 'anonymous' })
}

/** 执行任务计划 */
export function executePlan(planId: string, sessionId?: string, userId?: string) {
  return client.post(`/agent/plan/${planId}/execute`, null, {
    params: { sessionId: sessionId || 'default', userId: userId || 'anonymous' }
  })
}

/** 获取任务计划 */
export function getPlan(planId: string) {
  return client.get(`/agent/plan/${planId}`)
}

/** 添加知识到 RAG */
export function addKnowledge(id: string, content: string, source: string) {
  return client.post('/agent/knowledge', { id, content, source })
}

/** 批量添加知识 */
export function addKnowledgeBatch(documents: Array<{id: string, content: string, source: string}>) {
  return client.post('/agent/knowledge/batch', documents)
}

/** 列出知识库文档 */
export function listKnowledge() {
  return client.get('/agent/knowledge')
}

/** 删除知识文档 */
export function deleteKnowledge(id: string) {
  return client.delete(`/agent/knowledge/${id}`)
}

/** 获取知识库统计 */
export function getKnowledgeStats() {
  return client.get('/agent/knowledge/stats')
}

/** 获取记忆历史 */
export function getMemoryHistory(sessionId: string) {
  return client.get(`/agent/memory/${sessionId}`)
}

/** 添加记忆条目 */
export function addMemoryEntry(sessionId: string, role: string, content: string) {
  return client.post(`/agent/memory/${sessionId}`, { role, content })
}

/** 并行执行竞品分析 + 策略规划 */
export function executeParallelResearch(workspaceId: number, request: {
  competitorName: string
  topic: string
  audience?: string
  platforms?: string[]
  brandProfileId?: number
}) {
  return client.post(`/workspaces/${workspaceId}/strategy/parallel-research`, request)
}

/** 清除会话记忆 */
export function clearMemory(sessionId: string) {
  return client.delete(`/agent/memory/${sessionId}`)
}

/** 提交审核请求 */
export function submitReviewRequest(sessionId: string, stepId: string, question: string, context: string) {
  return client.post('/agent/review/submit', { sessionId, stepId, question, context })
}

/** 提交审核响应 */
export function submitReviewResponse(requestId: string, decision: string, feedback?: string, modifiedContent?: string) {
  return client.post('/agent/review/respond', { requestId, decision, feedback, modifiedContent })
}

/** 获取待审核请求列表 */
export function getPendingReviews() {
  return client.get('/agent/review/pending')
}