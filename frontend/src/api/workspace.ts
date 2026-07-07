/** 工作区 API：创建、列表、详情 */
import client from './client'
import type { ApiResponse, Workspace } from '../types'

export function createWorkspace(title: string, topic: string) {
  return client.post<ApiResponse<Workspace>>('/workspaces', { title, topic })
}

export function listWorkspaces() {
  return client.get<ApiResponse<Workspace[]>>('/workspaces')
}

export function getWorkspace(id: number) {
  return client.get<ApiResponse<Workspace>>(`/workspaces/${id}`)
}

export function deleteWorkspace(id: number) {
  return client.delete<ApiResponse<null>>(`/workspaces/${id}`)
}
