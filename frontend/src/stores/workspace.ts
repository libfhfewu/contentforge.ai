import { defineStore } from 'pinia'
import { ref } from 'vue'
import { createWorkspace, listWorkspaces } from '../api/workspace'
import type { Workspace } from '../types'

export const useWorkspaceStore = defineStore('workspace', () => {
  const workspaces = ref<Workspace[]>([])
  const loading = ref(false)

  async function fetchAll() {
    loading.value = true
    try {
      const res = await listWorkspaces()
      workspaces.value = res.data.data
    } finally {
      loading.value = false
    }
  }

  async function create(title: string, topic: string) {
    const res = await createWorkspace(title, topic)
    workspaces.value.unshift(res.data.data)
    return res.data.data as Workspace
  }

  return { workspaces, loading, fetchAll, create }
})
