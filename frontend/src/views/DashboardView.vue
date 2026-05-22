<!-- 仪表盘：新建工作区入口 + 已创建工作区卡片列表 -->
<template>
  <AppLayout>
    <n-space vertical size="large">
      <n-card title="新建工作区">
        <n-space vertical>
          <n-input v-model:value="newTitle" placeholder="工作区名称，如：618数码促销方案" />
          <n-input v-model:value="newTopic" type="textarea" placeholder="描述你的内容需求，如：写一篇面向数码爱好者的618促销长文..." :rows="3" />
          <n-button type="primary" @click="handleCreate" :loading="creating">开始创作</n-button>
        </n-space>
      </n-card>

      <n-card title="我的工作区">
        <n-grid v-if="store.workspaces.length > 0" cols="3" x-gap="12" y-gap="12">
          <n-grid-item v-for="ws in store.workspaces" :key="ws.id">
            <n-card :title="ws.title" hoverable @click="$router.push(`/workspace/${ws.id}`)">
              <n-ellipsis :line-clamp="2">{{ ws.topic }}</n-ellipsis>
              <n-tag :type="statusType(ws.status)" size="small" style="margin-top:8px">{{ statusText(ws.status) }}</n-tag>
            </n-card>
          </n-grid-item>
        </n-grid>
        <n-empty v-else description="还没有工作区，创建一个开始吧" />
      </n-card>
    </n-space>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useWorkspaceStore } from '../stores/workspace'
import AppLayout from '../components/layout/AppLayout.vue'

const router = useRouter()
const store = useWorkspaceStore()
const message = useMessage()
const newTitle = ref('')
const newTopic = ref('')
const creating = ref(false)

onMounted(() => store.fetchAll())

async function handleCreate() {
  if (!newTitle.value || !newTopic.value) {
    message.warning('请填写工作区名称和内容需求')
    return
  }
  creating.value = true
  try {
    const ws = await store.create(newTitle.value, newTopic.value)
    router.push(`/workspace/${ws.id}`)
  } catch (e: any) {
    message.error('创建失败')
  } finally {
    creating.value = false
  }
}

function statusText(s: number) {
  return ['草稿', '策划中', '创作中', '已完成'][s] || '草稿'
}

function statusType(s: number): 'default' | 'info' | 'warning' | 'success' {
  return ['default', 'info', 'warning', 'success'][s] as any
}
</script>
