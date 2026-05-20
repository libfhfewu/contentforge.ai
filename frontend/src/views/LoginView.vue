<template>
  <n-layout class="auth-layout">
    <n-card title="登录 AI 内容创作工作台" class="auth-card">
      <n-form ref="formRef" :model="form" :rules="rules">
        <n-form-item path="email" label="邮箱">
          <n-input v-model:value="form.email" placeholder="your@email.com" />
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input v-model:value="form.password" type="password" placeholder="密码" />
        </n-form-item>
        <n-button type="primary" block @click="handleLogin" :loading="loading">登录</n-button>
      </n-form>
      <p class="auth-switch">没有账号？<router-link to="/register">注册</router-link></p>
    </n-card>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '../stores/auth'
import type { FormInst, FormRules } from 'naive-ui'

const form = reactive({ email: '', password: '' })
const loading = ref(false)
const router = useRouter()
const auth = useAuthStore()
const message = useMessage()

const rules: FormRules = {
  email: [{ required: true, message: '请输入邮箱' }, { type: 'email', message: '邮箱格式不正确' }],
  password: [{ required: true, message: '请输入密码' }],
}

async function handleLogin() {
  loading.value = true
  try {
    await auth.doLogin(form.email, form.password)
    router.push('/dashboard')
  } catch (e: any) {
    message.error(e.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-layout { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f5f5f5; }
.auth-card { width: 420px; }
.auth-switch { text-align: center; margin-top: 16px; }
</style>
