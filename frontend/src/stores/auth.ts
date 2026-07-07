/** 认证状态管理：用户信息、登录/注册/登出操作、页面刷新恢复 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, register as apiRegister, logout as apiLogout, checkAuth } from '../api/auth'
import type { User, ApiResponse } from '../types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const authChecked = ref(false)
  let authCheckPromise: Promise<void> | null = null

  const isAuthenticated = computed(() => user.value !== null)

  /** 初始化认证状态：页面刷新时从服务器恢复用户信息 */
  async function initAuth() {
    // 避免重复检查
    if (authChecked.value) return
    if (authCheckPromise) return authCheckPromise

    const userId = localStorage.getItem('userId')
    if (!userId) {
      authChecked.value = true
      return
    }

    authCheckPromise = (async () => {
      try {
        const res = await checkAuth()
        const data = res.data as ApiResponse<{ authenticated: boolean; userId: number; username: string; email: string }>
        if (data.data?.authenticated) {
          user.value = { id: data.data.userId, username: data.data.username, email: data.data.email || '' }
        } else {
          localStorage.removeItem('userId')
        }
      } catch {
        localStorage.removeItem('userId')
      } finally {
        authChecked.value = true
        authCheckPromise = null
      }
    })()

    return authCheckPromise
  }

  async function doLogin(email: string, password: string) {
    const res = await apiLogin(email, password)
    const data = (res.data as ApiResponse<User>).data
    user.value = data
    localStorage.setItem('userId', String(data.id))
    authChecked.value = true
  }

  async function doRegister(username: string, email: string, password: string) {
    const res = await apiRegister(username, email, password)
    const data = (res.data as ApiResponse<User>).data
    user.value = data
    localStorage.setItem('userId', String(data.id))
    authChecked.value = true
  }

  async function doLogout() {
    await apiLogout()
    user.value = null
    authChecked.value = false
    localStorage.removeItem('userId')
  }

  return { user, isAuthenticated, authChecked, initAuth, doLogin, doRegister, doLogout }
})
