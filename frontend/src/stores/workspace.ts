/** 工作区状态管理：工作区列表、创建 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { createWorkspace, listWorkspaces } from '../api/workspace'
import type { Workspace, ApiResponse } from '../types'

export const useWorkspaceStore = defineStore('workspace', () => {
  const workspaces = ref<Workspace[]>([])
  const loading = ref(false)

  async function fetchAll() {
    loading.value = true
    try {
      const res = await listWorkspaces()
      workspaces.value = (res.data as ApiResponse<Workspace[]>).data
    } finally {
      loading.value = false
    }
  }

  async function create(title: string, topic: string) {
    const res = await createWorkspace(title, topic)
    const workspace = (res.data as ApiResponse<Workspace>).data
    workspaces.value.unshift(workspace)
    return workspace
  }

  return { workspaces, loading, fetchAll, create }
})
