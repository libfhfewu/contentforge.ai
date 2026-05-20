<template>
  <n-page-header>
    <template #title>
      <router-link to="/dashboard" style="text-decoration:none;color:inherit">AI 内容创作工作台</router-link>
    </template>
    <template #extra>
      <n-space>
        <n-button text @click="$router.push('/history')">历史记录</n-button>
        <n-button text @click="handleLogout">退出</n-button>
      </n-space>
    </template>
  </n-page-header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()

async function handleLogout() {
  await auth.doLogout()
  router.push('/login')
}
</script>
