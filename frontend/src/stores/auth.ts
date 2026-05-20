import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin, register as apiRegister, logout as apiLogout } from '../api/auth'
import type { User } from '../types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)

  async function doLogin(email: string, password: string) {
    const res = await apiLogin(email, password)
    user.value = res.data.data
    localStorage.setItem('userId', String(res.data.data.id))
  }

  async function doRegister(username: string, email: string, password: string) {
    const res = await apiRegister(username, email, password)
    user.value = res.data.data
    localStorage.setItem('userId', String(res.data.data.id))
  }

  async function doLogout() {
    await apiLogout()
    user.value = null
    localStorage.removeItem('userId')
  }

  return { user, doLogin, doRegister, doLogout }
})
