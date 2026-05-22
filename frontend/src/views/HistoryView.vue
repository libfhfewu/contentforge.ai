<!-- 历史记录：展示所有工作区列表，支持跳转查看 -->
<template>
  <AppLayout>
    <n-card title="历史记录">
      <n-data-table v-if="store.workspaces.length > 0" :columns="columns" :data="store.workspaces" :row-key="(row: any) => row.id" />
      <n-empty v-else description="暂无历史记录" />
    </n-card>
  </AppLayout>
</template>

<script setup lang="ts">
import { h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NTag } from 'naive-ui'
import { useWorkspaceStore } from '../stores/workspace'
import AppLayout from '../components/layout/AppLayout.vue'

const store = useWorkspaceStore()
const router = useRouter()

const columns = [
  { title: '标题', key: 'title' },
  { title: '状态', key: 'status', render: (row: any) => {
    const texts = ['草稿','策划中','创作中','已完成']
    const types = ['default','info','warning','success'] as const
    return h(NTag, { type: types[row.status] || 'default', size: 'small' }, { default: () => texts[row.status] || '草稿' })
  }},
  { title: '更新时间', key: 'updatedAt' },
  { title: '操作', key: 'actions', render: (row: any) =>
    h(NButton, { size: 'small', onClick: () => router.push(`/workspace/${row.id}`) }, { default: () => '打开' })
  },
]

onMounted(() => store.fetchAll())
</script>
