<template>
  <n-layout class="auth-layout">
    <n-card title="注册" class="auth-card">
      <n-form ref="formRef" :model="form" :rules="rules">
        <n-form-item path="username" label="用户名">
          <n-input v-model:value="form.username" placeholder="用户名（至少2位）" />
        </n-form-item>
        <n-form-item path="email" label="邮箱">
          <n-input v-model:value="form.email" placeholder="your@email.com" />
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input v-model:value="form.password" type="password" placeholder="至少6位" />
        </n-form-item>
        <n-button type="primary" block @click="handleRegister" :loading="loading">注册</n-button>
      </n-form>
      <p class="auth-switch">已有账号？<router-link to="/login">登录</router-link></p>
    </n-card>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '../stores/auth'
import type { FormRules } from 'naive-ui'

const form = reactive({ username: '', email: '', password: '' })
const loading = ref(false)
const router = useRouter()
const auth = useAuthStore()
const message = useMessage()

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名' }, { min: 2, message: '用户名至少2位' }],
  email: [{ required: true, message: '请输入邮箱' }, { type: 'email', message: '邮箱格式不正确' }],
  password: [{ required: true, message: '请输入密码' }, { min: 6, message: '密码至少6位' }],
}

async function handleRegister() {
  loading.value = true
  try {
    await auth.doRegister(form.username, form.email, form.password)
    router.push('/dashboard')
  } catch (e: any) {
    message.error(e.response?.data?.message || '注册失败')
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
