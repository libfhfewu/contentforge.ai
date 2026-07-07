<!-- 顶部导航栏：优化后的设计 -->
<template>
  <n-layout-header bordered class="app-header">
    <div class="header-content">
      <div class="header-left">
        <router-link to="/dashboard" class="logo-link">
          <n-space align="center" :size="8">
            <n-icon size="24" color="#2080f0">
              <CreateOutline />
            </n-icon>
            <span class="logo-text">AI 内容创作工作台</span>
          </n-space>
        </router-link>
      </div>
      
      <div class="header-center">
        <n-space align="center" :size="4">
          <n-button text @click="$router.push('/dashboard')" :type="isActive('/dashboard') ? 'primary' : 'default'">
            <template #icon><n-icon><HomeOutline /></n-icon></template>
            首页
          </n-button>
          <n-button text @click="$router.push('/models')" :type="isActive('/models') ? 'primary' : 'default'">
            <template #icon><n-icon><HardwareChipOutline /></n-icon></template>
            模型管理
          </n-button>
          <n-button text @click="$router.push('/workflows')" :type="isActive('/workflows') ? 'primary' : 'default'">
            <template #icon><n-icon><GitNetworkOutline /></n-icon></template>
            工作流
          </n-button>
          <n-button text @click="$router.push('/templates')" :type="isActive('/templates') ? 'primary' : 'default'">
            <template #icon><n-icon><DocumentTextOutline /></n-icon></template>
            模板
          </n-button>
          <n-button text @click="$router.push('/history')" :type="isActive('/history') ? 'primary' : 'default'">
            <template #icon><n-icon><TimeOutline /></n-icon></template>
            历史
          </n-button>
        </n-space>
      </div>
      
      <div class="header-right">
        <n-space align="center" :size="16">
          <n-dropdown :options="userOptions" @select="handleUserAction">
            <n-space align="center" :size="8" style="cursor: pointer">
              <n-avatar :size="32" round :style="{ background: '#2080f0' }">
                {{ user?.username?.charAt(0) || 'U' }}
              </n-avatar>
              <n-text>{{ user?.username || '用户' }}</n-text>
            </n-space>
          </n-dropdown>
        </n-space>
      </div>
    </div>
  </n-layout-header>
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { 
  CreateOutline, 
  HomeOutline, 
  TimeOutline, 
  LogOutOutline, 
  PersonOutline,
  HardwareChipOutline,
  GitNetworkOutline,
  DocumentTextOutline
} from '@vicons/ionicons5'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const auth = useAuthStore()

const user = computed(() => auth.user)

function isActive(path: string) {
  return route.path.startsWith(path)
}

const userOptions = [
  {
    label: '个人设置',
    key: 'profile',
    icon: () => h('n-icon', null, { default: () => h(PersonOutline) })
  },
  {
    type: 'divider',
    key: 'd1'
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: () => h('n-icon', null, { default: () => h(LogOutOutline) })
  }
]

async function handleUserAction(key: string) {
  if (key === 'logout') {
    await auth.doLogout()
    message.success('已退出登录')
    router.push('/login')
  } else if (key === 'profile') {
    message.info('个人设置功能开发中')
  }
}
</script>

<style scoped>
.app-header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo-link {
  text-decoration: none;
  color: inherit;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.header-right {
  display: flex;
  align-items: center;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }
  
  .logo-text {
    font-size: 16px;
  }
  
  .header-center {
    display: none;
  }
}
</style>
