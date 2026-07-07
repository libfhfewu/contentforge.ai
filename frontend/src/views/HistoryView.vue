<!-- 历史记录：优化后的设计 -->
<template>
  <AppLayout>
    <div class="history-view">
      <n-card title="历史记录" :bordered="false" class="history-card">
        <template #header-extra>
          <n-input
            v-model:value="searchQuery"
            placeholder="搜索工作区..."
            clearable
            style="width: 240px"
          >
            <template #prefix>
              <n-icon><SearchOutline /></n-icon>
            </template>
          </n-input>
        </template>
        
        <n-data-table 
          v-if="filteredWorkspaces.length > 0" 
          :columns="columns" 
          :data="filteredWorkspaces" 
          :row-key="(row: Workspace) => row.id"
          :bordered="false"
          :single-line="false"
          class="history-table"
        />
        <n-empty v-else description="暂无历史记录" />
      </n-card>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NTag, NIcon } from 'naive-ui'
import { SearchOutline, OpenOutline } from '@vicons/ionicons5'
import type { DataTableColumns } from 'naive-ui'
import { useWorkspaceStore } from '../stores/workspace'
import AppLayout from '../components/layout/AppLayout.vue'
import { formatDate } from '../utils/date'
import { STATUS_TEXTS, STATUS_TYPES } from '../constants/status'
import type { Workspace } from '../types'

const store = useWorkspaceStore()
const router = useRouter()
const searchQuery = ref('')

const statusTexts = STATUS_TEXTS
const statusTypes = STATUS_TYPES

// 搜索过滤
const filteredWorkspaces = computed(() => {
  if (!searchQuery.value) return store.workspaces
  const query = searchQuery.value.toLowerCase()
  return store.workspaces.filter(ws => 
    ws.title.toLowerCase().includes(query) || 
    ws.topic.toLowerCase().includes(query)
  )
})

const columns: DataTableColumns<Workspace> = [
  { 
    title: '标题', 
    key: 'title',
    render: (row) => h('div', { style: 'font-weight: 500; color: #1d2129;' }, row.title)
  },
  {
    title: '状态',
    key: 'status',
    render: (row) => {
      return h(NTag, { 
        type: statusTypes[row.status] || 'default', 
        size: 'small',
        round: true
      }, { default: () => statusTexts[row.status] || '草稿' })
    }
  },
  { 
    title: '主题', 
    key: 'topic',
    ellipsis: { tooltip: true }
  },
  { 
    title: '更新时间', 
    key: 'updatedAt',
    render: (row) => h('span', { style: 'color: #86909c; font-size: 13px;' }, formatDate(row.updatedAt, true))
  },
  {
    title: '操作',
    key: 'actions',
    render: (row) => h(NButton, { 
      size: 'small', 
      type: 'primary',
      secondary: true,
      onClick: () => router.push(`/workspace/${row.id}`) 
    }, { 
      default: () => '打开',
      icon: () => h(NIcon, null, { default: () => h(OpenOutline) })
    })
  },
]

onMounted(() => store.fetchAll())
</script>

<style scoped>
.history-view {
  /* 样式 */
}

.history-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.history-table {
  margin-top: 16px;
}

:deep(.n-data-table-th) {
  background: #fafafa;
  font-weight: 600;
}

:deep(.n-data-table-td) {
  padding: 12px 16px;
}

:deep(.n-data-table-tr:hover .n-data-table-td) {
  background: #f6f8fa;
}
</style>
