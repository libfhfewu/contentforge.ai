/**
 * Agent pipeline API call wrappers for executing strategy, content, and platform steps.
 */
import client from './client'

export function executeStrategy(workspaceId: number, topic: string, audience: string) {
  return client.post(`/workspaces/${workspaceId}/strategy/execute`, { topic, audience })
}

export function executeContent(workspaceId: number, strategy: string, contentType: string) {
  return client.post(`/workspaces/${workspaceId}/content/execute`, { strategy, contentType })
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
