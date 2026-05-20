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
