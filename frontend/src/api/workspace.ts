/** 工作区 API：创建、列表、详情 */
import client from './client'

export function createWorkspace(title: string, topic: string) {
  return client.post('/workspaces', { title, topic })
}

export function listWorkspaces() {
  return client.get('/workspaces')
}

export function getWorkspace(id: number) {
  return client.get(`/workspaces/${id}`)
}
